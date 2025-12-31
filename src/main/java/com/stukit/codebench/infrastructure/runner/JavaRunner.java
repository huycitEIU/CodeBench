package com.stukit.codebench.infrastructure.runner;

import com.stukit.codebench.domain.RunResult;
import com.stukit.codebench.infrastructure.sandbox.Sandbox;
import com.stukit.codebench.service.FileIOService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class JavaRunner {

    private final FileIOService fileIOService;

    public JavaRunner() {
        this.fileIOService = new FileIOService();
    }

    // --- HÀM HELPER ĐỂ GHI LOG RA DESKTOP ---
    private void logToDesktop(String message) {
        try {
            String desktopPath = System.getProperty("user.home") + "/Desktop/debug_runner.txt";
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(desktopPath, true)))) {
                out.println(message);
            }
        } catch (IOException ignored) { }
    }

    public RunResult run(File workspaceRoot, Path inputPath, Sandbox sandbox) throws RunnerException {
        logToDesktop("========== BẮT ĐẦU CHẠY JAVARUNNER ==========");

        // 1. Debug đường dẫn Java
        String javaPath = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        logToDesktop("Java Path: " + javaPath);

        // 2. Cấu hình ProcessBuilder
        var processBuilder = new ProcessBuilder(
                javaPath,
                "-Dfile.encoding=UTF-8",
                "-cp", ".",
                "Solution"
        );

        processBuilder.directory(workspaceRoot);
        logToDesktop("Working Dir: " + workspaceRoot.getAbsolutePath());

        // Kiểm tra file Solution.class có tồn tại không
        File classFile = new File(workspaceRoot, "Solution.class");
        logToDesktop("Solution.class exists? " + classFile.exists());

        File stdoutFile = new File(workspaceRoot, inputPath.getFileName().toString() + "_out.txt");
        File stderrFile = new File(workspaceRoot, inputPath.getFileName().toString() + "_err.txt");

        processBuilder.redirectOutput(stdoutFile);
        processBuilder.redirectError(stderrFile);
        processBuilder.redirectInput(inputPath.toFile());

        Process process = null;

        try {
            long startTime = System.currentTimeMillis();
            logToDesktop("Đang gọi process.start()...");

            process = processBuilder.start();

            logToDesktop("Đang chờ process chạy (TimeLimit: " + sandbox.timeLimits() + "ms)...");
            boolean isFinished = process.waitFor(sandbox.timeLimits(), TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();

            // 6. Xử lý Timeout
            if (!isFinished) {
                logToDesktop("!!! KẾT QUẢ: TIMEOUT !!!"); // <--- LOG QUAN TRỌNG
                process.destroyForcibly();
                return RunResult.timeout(sandbox.timeLimits());
            }

            // 7. Lấy kết quả Exit Code
            int exitCode = process.exitValue();
            logToDesktop("!!! KẾT QUẢ: DONE. Exit Code = " + exitCode); // <--- LOG QUAN TRỌNG

            return new RunResult(
                    false,
                    exitCode,
                    endTime - startTime,
                    stdoutFile.toPath(),
                    stderrFile.toPath()
            );

        } catch (InterruptedException e) {
            logToDesktop("ERROR: Interrupted - " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new RunnerException("Interrupted", e);
        } catch (IOException e) {
            logToDesktop("ERROR: IOException - " + e.getMessage());
            e.printStackTrace(); // Vẫn in ra console cho chắc
            throw new RunnerException("IO Error", e);
        } catch (Exception e) {
            logToDesktop("ERROR: Exception lạ - " + e.getMessage());
            throw new RunnerException("Error", e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            logToDesktop("========== KẾT THÚC JAVARUNNER ==========\n");
        }
    }
}