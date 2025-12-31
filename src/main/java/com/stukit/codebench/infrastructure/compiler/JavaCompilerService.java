package com.stukit.codebench.infrastructure.compiler;

import com.stukit.codebench.infrastructure.fs.Workspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Service chịu trách nhiệm biên dịch source code Java.
 */
public class JavaCompilerService {

    /**
     * Biên dịch source code Java trong workspace hiện tại
     *
     * @param workspace workspace đã được tạo sẵn
     * @param sourceCode mã nguồn Java (class Solution)
     * @throws CompileException nếu có lỗi biên dịch
     */
    public void compile(Workspace workspace, String sourceCode) throws CompileException {
        // 1. Ghi code ra file Solution.java
        workspace.write("Solution.java", sourceCode);

        try {
            // --- SỬA ĐỔI QUAN TRỌNG ---

            // A. Xác định vị trí javac.exe dựa trên java.home (Nơi đang chạy app)
            // Trong bản Portable, nó sẽ trỏ vào folder /runtime/bin/
            String javacPath = Path.of(System.getProperty("java.home"), "bin", "javac.exe").toString();

            // Kiểm tra xem javac có tồn tại không (để debug)
            File compilerFile = new File(javacPath);
            if (!compilerFile.exists()) {
                // Fallback: Nếu không tìm thấy file cụ thể, thử gọi lệnh hệ thống (dành cho lúc chạy trong IDE)
                javacPath = "javac";
            }

            // B. Cấu hình lệnh biên dịch với cờ --release 17
            ProcessBuilder processBuilder = new ProcessBuilder(
                    javacPath,
                    "-encoding", "UTF-8",   // Đảm bảo không lỗi tiếng Việt
                    "--release", "17",      // QUAN TRỌNG: Ép version bytecode về Java 17 (Fix lỗi 69.0 vs 61.0)
                    "Solution.java"
            );

            processBuilder.directory(workspace.getRoot().toFile());
            processBuilder.redirectErrorStream(true); // Gộp lỗi vào luồng output để đọc

            // 2. Chạy trình biên dịch
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // 3. Nếu compile thất bại (Exit Code khác 0)
            if (exitCode != 0) {
                StringBuilder errorMessage = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMessage.append(line).append("\n");
                    }
                }

                String errorStr = errorMessage.toString().trim();

                // Gợi ý lỗi rõ ràng hơn nếu thiếu javac
                if (errorStr.isEmpty() && !compilerFile.exists() && !"javac".equals(javacPath)) {
                    throw new CompileException("Lỗi hệ thống: Không tìm thấy trình biên dịch (javac.exe) tại " + javacPath);
                }

                throw new CompileException(errorStr);
            }

        } catch (CompileException e) {
            throw e;
        } catch (Exception e) {
            throw new CompileException("Lỗi hệ thống khi gọi trình biên dịch: " + e.getMessage(), e);
        }
    }
}