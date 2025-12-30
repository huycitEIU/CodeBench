package com.stukit.codebench.infrastructure.runner;

import com.stukit.codebench.infrastructure.sandbox.Sandbox;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.*;

public class JavaRunner {

    public RunResult run(File workspaceRoot, String input, Sandbox sandbox) throws RunnerException {

        // Tìm đường dẫn java (đảm bảo đa nền tảng)
        String javaPath = Path.of(System.getProperty("java.home"), "bin", "java").toString();

        // Thêm tham số -Xmx để giới hạn bộ nhớ nếu cần (lấy từ sandbox)
        // Ví dụ: processBuilder.command(javaPath, "-Xmx256m", "-cp", ".", "Solution");
        ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-Dfile.encoding=UTF-8", "-cp", ".", "Solution");
        processBuilder.directory(workspaceRoot);
        processBuilder.redirectErrorStream(false); // Tách riêng stdout và stderr

        // Tạo ThreadPool (phải shutdown ở finally)
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Process process = null;
        try {
            long startTime = System.currentTimeMillis();
            process = processBuilder.start();

            // --- QUAN TRỌNG: Ghi Input vào Process (Có xử lý lỗi Pipe ended) ---
            // Nếu process chết ngay lập tức, việc ghi input sẽ ném IOException.
            // Ta phải catch nó để không làm crash Judge, mà để code chạy tiếp xuống dưới đọc stderr.
            final Process finalProcess = process;
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(finalProcess.getOutputStream(), StandardCharsets.UTF_8))) {
                if (input != null && !input.isEmpty()) {
                    writer.write(input);
                    writer.flush();
                }
            } catch (IOException e) {
                // "The pipe has been ended" thường xảy ra ở đây.
                // Nghĩa là chương trình con đã đóng hoặc crash trước khi nhận hết input.
                // Ta LỜ ĐI lỗi này để xuống dưới đọc nguyên nhân lỗi từ getErrorStream().
            }

            // Đọc Output bất đồng bộ để tránh Deadlock
            // Lưu ý: Dù process đã chết, Stream vẫn còn đọng lại dữ liệu trong buffer của OS, nên vẫn đọc được.
            Future<String> stdoutFuture = executor.submit(() -> readStream(finalProcess.getInputStream()));
            Future<String> stderrFuture = executor.submit(() -> readStream(finalProcess.getErrorStream()));

            // Chờ kết quả hoặc Timeout
            boolean isFinished = process.waitFor(sandbox.timeLimits(), TimeUnit.MILLISECONDS);

            if (!isFinished) {
                process.destroyForcibly();
                return RunResult.timeout(sandbox.timeLimits());
            }

            long endTime = System.currentTimeMillis();
            int exitCode = process.exitValue();

            // Lấy kết quả từ Future
            String stdout = stdoutFuture.get();
            String stderr = stderrFuture.get();

            // Xác định Runtime Error dựa trên Exit Code
            boolean isRuntimeError = (exitCode != 0);

            return new RunResult(
                    isRuntimeError,     // True nếu exitCode != 0
                    exitCode,
                    endTime - startTime,
                    stdout,
                    stderr              // Nếu crash, stderr sẽ chứa stacktrace
            );

        } catch (InterruptedException | ExecutionException e) {
            throw new RunnerException("Lỗi hệ thống khi chạy code: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RunnerException("Lỗi IO khi khởi tạo process: " + e.getMessage(), e);
        } finally {
            // Dọn dẹp
            executor.shutdownNow();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString().trim();
    }
}