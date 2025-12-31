package com.stukit.codebench.service;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.domain.Verdict;
import com.stukit.codebench.infrastructure.compiler.CompileException;
import com.stukit.codebench.infrastructure.compiler.JavaCompilerService;
import com.stukit.codebench.infrastructure.fs.TempWorkspace;
import com.stukit.codebench.infrastructure.fs.Workspace;
import com.stukit.codebench.infrastructure.fs.WorkspaceFactory;
import com.stukit.codebench.infrastructure.parser.OutputParser;
import com.stukit.codebench.infrastructure.runner.JavaRunner;
import com.stukit.codebench.domain.RunResult;
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
            JavaRunner javaRunner,
            OutputParser parser
    ) {
        this.workspaceFactory = workspaceFactory;
        this.compilerService = compilerService;
        this.runner = javaRunner;
        this.parser = parser;
        this.fileIOService = new FileIOService();
    }

    public JudgeSession createSession(String sourceCode, long timeLimitMs) throws IOException {
        Workspace workspace = workspaceFactory.create();
        return new JudgeSession(workspace, sourceCode, timeLimitMs);
    }

    // =================================================================================
    // INNER CLASS
    // =================================================================================
    public class JudgeSession implements Closeable {
        private final Workspace workspace;
        private final String sourceCode;
        private final long timeLimitMs;
        private Sandbox sandbox;

        public JudgeSession(Workspace workspace, String sourceCode, long timeLimitMs) {
            this.workspace = workspace;
            this.sourceCode = sourceCode;
            this.timeLimitMs = timeLimitMs;
        }

        public void compile() throws CompileException {
            compilerService.compile(workspace, sourceCode);
            this.sandbox = new LocalSandbox(workspace.getRoot(), timeLimitMs, 0);
        }

        public JudgeResult runTestCase(TestCase testCase) {
            if (this.sandbox == null) {
                return new JudgeResult(
                        testCase.getName(),
                        Verdict.SYSTEM_ERROR,
                        "Session not compiled");
            }

            try {
                // 1. Chạy code (JavaRunner tự ghi ra file stdout.txt trong workspace)
                RunResult runResult = runner.run(
                        workspace.getRoot().toFile(),
                        testCase.getInputPath(), // Đọc input từ file của TestCase
                        sandbox
                );


                // 2. Kiểm tra các lỗi Runtime / Timeout trước
                if (runResult.isTimeout()) {
                    return new JudgeResult(
                            testCase.getName(),
                            Verdict.TIME_LIMIT_EXCEEDED,
                            runResult.getRunTime(),
                            runResult.getStdoutPath(), // Path stdout
                            runResult.getStderrPath() // Path stderr
                    );
                }

                if (runResult.hasRuntimeError()) {
                    return new JudgeResult(
                            testCase.getName(),
                            Verdict.RUNTIME_ERROR,
                            runResult.getRunTime(),
                            runResult.getStdoutPath(), // Path stdout
                            runResult.getStderrPath()  // Path stderr
                    );
                }

                // 3. Logic so sánh Output (Parser)
                // Đọc nội dung file output thực tế để chấm (fileIOService đọc file an toàn)
                // Lưu ý: Nếu output quá lớn, parser.normalize có thể tốn RAM.
                // Tốt nhất là đọc file, normalize rồi so sánh, hoặc dùng stream comparison.
                String actualContent = fileIOService.readString(runResult.getStdoutPath());
                String expectedContent = fileIOService.readString(testCase.getExpectedOutputPath());

                String normalizedActual = parser.normalize(actualContent);
                String normalizedExpected = parser.normalize(expectedContent);

                Verdict verdict = normalizedActual.equals(normalizedExpected) ? Verdict.PASSED : Verdict.FAILED;

                // 4. Trả về kết quả kèm đường dẫn file để UI load sau (Lazy Loading)
                return new JudgeResult(
                        testCase.getName(),
                        verdict,
                        runResult.getRunTime(),
                        runResult.getStdoutPath(), // Path stdout
                        runResult.getStderrPath()  // Path stderr
                );

            } catch (RunnerException e) {
                return new JudgeResult(
                        testCase.getName(),
                        Verdict.RUNTIME_ERROR,
                        e.getMessage());
            } catch (IOException e) {
                return new JudgeResult(
                        testCase.getName(),
                        Verdict.SYSTEM_ERROR,
                        "IO Error: " + e.getMessage());
            }
        }

        public void cleanupWorkspace() {
            if (workspace instanceof TempWorkspace) {
                ((TempWorkspace) workspace).cleanup();
            }
        }

        @Override
        public void close() throws IOException {
            if (workspace != null) {
                // Chỉ đóng các kết nối (nếu có), KHÔNG XÓA file
                workspace.close();
            }
        }
    }
}