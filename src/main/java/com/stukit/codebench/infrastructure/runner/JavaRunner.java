package com.stukit.codebench.infrastructure.runner;

import com.stukit.codebench.domain.RunResult;
import com.stukit.codebench.infrastructure.sandbox.Sandbox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Class chịu trách nhiệm thực thi file .class đã biên dịch.
 * <p>
 * Luồng xử lý:
 * 1. Cấu hình lệnh chạy (java Solution).
 * 2. Áp dụng Sandbox (giới hạn Ram, thư mục).
 * 3. Redirect Input từ file test case.
 * 4. Redirect Output/Error ra file tạm trong workspace.
 * 5. Chờ process kết thúc hoặc kill nếu timeout.
 */
public class JavaRunner {

    /**
     * Chạy code java trong môi trường sandbox.
     *
     * @param inputPath Đường dẫn đến file input (testcase.in)
     * @param sandbox   Cấu hình sandbox (chứa workspace root, timeout...)
     * @return Kết quả chạy (RunResult)
     * @throws RunnerException nếu lỗi hệ thống (không phải lỗi code người dùng)
     */
    public RunResult run(Path inputPath, Sandbox sandbox) throws RunnerException {
        // 1. Xác định file Output/Error sẽ nằm ở đâu
        // Đặt tên theo input để dễ debug: test1.in -> test1.in_out.txt
        String baseName = inputPath.getFileName().toString();
        Path stdoutPath = sandbox.getWorkspaceRoot().resolve(baseName + "_out.txt");
        Path stderrPath = sandbox.getWorkspaceRoot().resolve(baseName + "_err.txt");

        // 2. Cấu hình ProcessBuilder
        // Command: java -Dfile.encoding=UTF-8 -cp . Solution
        ProcessBuilder processBuilder = new ProcessBuilder(
                resolveJavaExecutor(),
                "-Dfile.encoding=UTF-8", // Quan trọng: Để in được tiếng Việt/kí tự đặc biệt
                "-cp", ".",              // Classpath là thư mục hiện tại
                "Solution"               // Tên class
        );

        // 3. Áp dụng Sandbox (Set working dir, Memory limit...)
        sandbox.apply(processBuilder);

        // 4. Cấu hình Redirection (IO)
        // Thay vì đọc stream thủ công, ta nhờ OS bắn thẳng vào file -> Hiệu năng cao hơn
        processBuilder.redirectInput(inputPath.toFile());
        processBuilder.redirectOutput(stdoutPath.toFile());
        processBuilder.redirectError(stderrPath.toFile());

        Process process = null;
        ProcessMemoryMonitor memoryMonitor = null;

        try {
            long startTime = System.currentTimeMillis();

            // Bắt đầu chạy
            process = processBuilder.start();

            // Bắt đầu theo dõi bộ nhớ
            memoryMonitor = new ProcessMemoryMonitor(process);
            memoryMonitor.start();
            // Chờ đợi với Timeout
            boolean isFinished = process.waitFor(sandbox.getTimeLimitMs(), TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            // Kết thúc theo dõi bộ nhớ
            memoryMonitor.stopMonitoring();

            long duration = endTime - startTime;

            // Xử lý Timeout
            if (!isFinished) {
                process.destroyForcibly(); // Kill process ngay lập tức
                return RunResult.timeout(sandbox.getTimeLimitMs(), stdoutPath, stderrPath);
            }

            int exitCode = process.exitValue();

            // Trả về kết quả (thành công hoặc lỗi runtime đều gói vào đây)
            return new RunResult(
                    false, // Not timeout
                    exitCode,
                    duration,
                    memoryMonitor.getPeakMemoryBytes(),
                    stdoutPath,
                    stderrPath
            );

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RunnerException("Lỗi khi thực thi process: " + e.getMessage(), e);
        } finally {
            // Đảm bảo process luôn được dọn dẹp
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Tìm đường dẫn java.exe hoặc java (tương thích đa nền tảng)
     */
    private String resolveJavaExecutor() {
        String os = System.getProperty("os.name").toLowerCase();
        String binaryName = os.contains("win") ? "java.exe" : "java";

        // Ưu tiên dùng chính Java đang chạy app này để đảm bảo phiên bản tương thích
        Path javaHomeBin = Path.of(System.getProperty("java.home"), "bin", binaryName);

        if (javaHomeBin.toFile().exists()) {
            return javaHomeBin.toString();
        }
        return binaryName; // Fallback về biến môi trường
    }
}