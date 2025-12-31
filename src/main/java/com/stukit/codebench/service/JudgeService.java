package com.stukit.codebench.service;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.RunResult;
import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.domain.Verdict;
import com.stukit.codebench.infrastructure.compiler.CompileException;
import com.stukit.codebench.infrastructure.compiler.JavaCompilerService;
import com.stukit.codebench.infrastructure.fs.Workspace;
import com.stukit.codebench.infrastructure.fs.WorkspaceFactory;
import com.stukit.codebench.infrastructure.parser.OutputParser;
import com.stukit.codebench.infrastructure.runner.JavaRunner;
import com.stukit.codebench.infrastructure.runner.RunnerException;
import com.stukit.codebench.infrastructure.sandbox.LocalSandbox;
import com.stukit.codebench.infrastructure.sandbox.Sandbox;

import java.io.Closeable;
import java.io.IOException;

public class JudgeService {
    private final WorkspaceFactory workspaceFactory;
    private final JavaCompilerService compilerService;
    private final JavaRunner runner;
    private final OutputParser parser;
    private final FileIOService fileIOService;

    public JudgeService(
            WorkspaceFactory workspaceFactory,
            JavaCompilerService compilerService,
            JavaRunner runner,
            OutputParser parser
    ) {
        this.workspaceFactory = workspaceFactory;
        this.compilerService = compilerService;
        this.runner = runner;
        this.parser = parser;
        this.fileIOService = new FileIOService();
    }

    /**
     * Tạo một phiên chấm bài mới (Session).
     * Session sẽ tự quản lý vòng đời của Workspace.
     */
    public JudgeSession createSession(String sourceCode, long timeLimitMs, long memoryLimitBytes) {
        Workspace workspace = workspaceFactory.create();
        return new JudgeSession(workspace, sourceCode, timeLimitMs, memoryLimitBytes);
    }

    // =================================================================================
    // INNER CLASS: Quản lý trạng thái chấm của 1 bài nộp
    // =================================================================================
    public class JudgeSession implements Closeable {
        private final Workspace workspace;
        private final String sourceCode;
        private final long timeLimitMs;
        private final long memoryLimitBytes;
        private Sandbox sandbox;
        private boolean isCompiled = false;

        public JudgeSession(Workspace workspace, String sourceCode, long timeLimitMs, long memoryLimitBytes) {
            this.workspace = workspace;
            this.sourceCode = sourceCode;
            this.timeLimitMs = timeLimitMs;
            this.memoryLimitBytes = memoryLimitBytes;
        }

        /**
         * Bước 1: Biên dịch code.
         * Nếu lỗi sẽ ném ra CompileException để UI bắt và hiển thị.
         */
        public void compile() throws CompileException {
            compilerService.compile(workspace, sourceCode);
            // Khởi tạo Sandbox sau khi compile thành công
            this.sandbox = new LocalSandbox(workspace.getRoot(), timeLimitMs, memoryLimitBytes);
            this.isCompiled = true;
        }

        /**
         * Bước 2: Chạy từng test case.
         */
        public JudgeResult runTestCase(TestCase testCase) {
            if (!isCompiled || sandbox == null) {
                return JudgeResult.fromError(testCase.name(), Verdict.SYSTEM_ERROR, "Code chưa được biên dịch!");
            }

            try {
                // A. Chạy code (Runner)
                // Lưu ý: record TestCase dùng inputPath() thay vì getInputPath()
                RunResult runResult = runner.run(testCase.inputPath(), sandbox);

                // B. Kiểm tra Timeout / Runtime Error
                if (runResult.isTimeout()) {
                    return JudgeResult.fromExecution(
                            testCase.name(), Verdict.TIME_LIMIT_EXCEEDED,
                            runResult.runTimeMs(), runResult.memoryBytes(), runResult.stdoutPath(), runResult.stderrPath()
                    );
                }

                if (runResult.hasRuntimeError()) {
                    return JudgeResult.fromExecution(
                            testCase.name(), Verdict.RUNTIME_ERROR,
                            runResult.runTimeMs(), runResult.memoryBytes(), runResult.stdoutPath(), runResult.stderrPath()
                    );
                }

                // C. So sánh Output (Parser)
                String actualRaw = fileIOService.readString(runResult.stdoutPath());
                String expectedRaw = fileIOService.readString(testCase.expectedOutputPath());

                String actual = parser.normalize(actualRaw);
                String expected = parser.normalize(expectedRaw);

                Verdict verdict = actual.equals(expected) ? Verdict.PASSED : Verdict.FAILED;

                return JudgeResult.fromExecution(
                        testCase.name(), verdict,
                        runResult.runTimeMs(), runResult.memoryBytes(), runResult.stdoutPath(), runResult.stderrPath()
                );

            } catch (RunnerException | IOException e) {
                return JudgeResult.fromError(testCase.name(), Verdict.SYSTEM_ERROR, e.getMessage());
            }
        }

        /**
         * Đóng session và xóa toàn bộ file tạm.
         */
        @Override
        public void close() {
            // Workspace.close() đã bao gồm logic cleanup (xóa thư mục)
            // do ta đã fix bug ở TempWorkspace trước đó.
            if (workspace != null) {
                workspace.close();
            }
        }
    }
}