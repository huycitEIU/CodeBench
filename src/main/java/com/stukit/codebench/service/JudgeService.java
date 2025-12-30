package com.stukit.codebench.service;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.domain.Verdict;
import com.stukit.codebench.infrastructure.compiler.CompileException;
import com.stukit.codebench.infrastructure.compiler.JavaCompilerService;
import com.stukit.codebench.infrastructure.fs.Workspace;
import com.stukit.codebench.infrastructure.fs.WorkspaceFactory;
import com.stukit.codebench.infrastructure.parser.OutputParser;
import com.stukit.codebench.infrastructure.runner.JavaRunner;
import com.stukit.codebench.infrastructure.runner.RunResult;
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

    // Constructor: Chỉ nhận dependencies, không nhận source code
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
    }

    /**
     * Bắt đầu một phiên chấm bài mới.
     * Tạo Workspace và trả về đối tượng JudgeSession để điều khiển việc chấm từng bài.
     */
    public JudgeSession createSession(String sourceCode, long timeLimitMs) throws IOException {
        Workspace workspace = workspaceFactory.create();
        return new JudgeSession(workspace, sourceCode, timeLimitMs);
    }

    // =================================================================================
    // INNER CLASS: JudgeSession
    // Giữ trạng thái của Workspace để chấm nhiều test case liên tiếp
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

        /**
         * Bước 1: Biên dịch code.
         * Nên gọi hàm này trước khi chạy loop test case.
         */
        public void compile() throws CompileException {
            compilerService.compile(workspace, sourceCode);
            // Sau khi compile thành công, setup sandbox 1 lần
            this.sandbox = new LocalSandbox(workspace.getRoot(), timeLimitMs, 0);
        }

        /**
         * Bước 2: Chạy một test case cụ thể.
         * Hàm này sẽ được gọi trong vòng lặp của Controller.
         */
        public JudgeResult runTestCase(TestCase testCase) {
            // Nếu chưa khởi tạo sandbox (do chưa compile hoặc quên gọi compile), trả về lỗi hệ thống
            if (this.sandbox == null) {
                return new JudgeResult(testCase.getName(), Verdict.SYSTEM_ERROR, 0, "", "JudgeSession chưa được biên dịch.");
            }

            try {
                RunResult runResult = runner.run(
                        workspace.getRoot().toFile(),
                        testCase.getInput(),
                        sandbox
                );

                if (runResult.isTimeout()) {
                    return new JudgeResult(
                            testCase.getName(),
                            Verdict.TIME_LIMIT_EXCEEDED,
                            sandbox.timeLimits(),
                            "",
                            "Time limit exceeded"
                    );
                }

                if (runResult.hasRuntimeError()) {
                    return new JudgeResult(
                            testCase.getName(),
                            Verdict.RUNTIME_ERROR,
                            runResult.getRunTime(),
                            runResult.getStderr(),
                            runResult.getStderr()
                    );
                }

                String actual = parser.normalize(runResult.getStdout());
                String expected = parser.normalize(testCase.getExpectedOutput());

                if (actual.equals(expected)) {
                    return new JudgeResult(testCase.getName(), Verdict.PASSED, runResult.getRunTime(), actual, "");
                } else {
                    return new JudgeResult(testCase.getName(), Verdict.FAILED, runResult.getRunTime(), actual, "");
                }

            } catch (RunnerException e) {
                System.err.println(e.getMessage());
                return new JudgeResult(testCase.getName(), Verdict.SYSTEM_ERROR, 0, "", e.getMessage());
            }
        }

        /**
         * Bước 3: Dọn dẹp Workspace.
         * Được gọi tự động nếu dùng try-with-resources.
         */
        @Override
        public void close() throws IOException {
            if (workspace != null) {
                workspace.close();
            }
        }
    }
}