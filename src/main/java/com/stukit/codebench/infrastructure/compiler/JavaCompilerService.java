package com.stukit.codebench.infrastructure.compiler;

import com.stukit.codebench.infrastructure.fs.Workspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Service chịu trách nhiệm biên dịch source code Java.
 * Sử dụng javac của môi trường hiện tại (Java Home).
 */
public class JavaCompilerService {

    private static final String CLASS_NAME = "Solution";
    private static final String FILE_NAME = "Solution.java";

    /**
     * Biên dịch source code Java trong workspace.
     *
     * @param workspace Workspace chứa mã nguồn
     * @param sourceCode Nội dung code (Class Solution)
     * @throws CompileException nếu code sai cú pháp hoặc lỗi hệ thống
     */
    public void compile(Workspace workspace, String sourceCode) throws CompileException {
        // 1. Chuẩn bị file nguồn
        workspace.write(FILE_NAME, sourceCode);

        // 2. Xác định đường dẫn javac
        String javacCmd = resolveJavacPath();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    javacCmd,
                    "-encoding", "UTF-8",   // Hỗ trợ comment/string tiếng Việt
                    "--release", "17",      // Đảm bảo tương thích bytecode Java 17
                    FILE_NAME
            );

            processBuilder.directory(workspace.getRoot().toFile());
            processBuilder.redirectErrorStream(true); // Gộp stderr vào stdout để đọc 1 lần

            Process process = processBuilder.start();

            // --- JAVA 17 FEATURE ---
            // Dùng inputReader() để đọc luồng output một cách an toàn.
            // Phải đọc HẾT output trước khi process kết thúc để tránh buffer deadlock.
            String compilerOutput;
            try (BufferedReader reader = process.inputReader(StandardCharsets.UTF_8)) {
                compilerOutput = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            // 3. Xử lý kết quả
            if (exitCode != 0) {
                // Nếu output rỗng mà exitCode != 0, có thể do không gọi được javac
                if (compilerOutput.isBlank()) {
                    compilerOutput = "Compilation failed with exit code " + exitCode +
                            ". Check if 'javac' is correctly installed/configured.";
                }
                throw new CompileException(compilerOutput);
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new CompileException("System Error: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm đường dẫn file thực thi javac.
     * Ưu tiên tìm trong java.home, fallback về biến môi trường PATH.
     */
    private String resolveJavacPath() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        String binaryName = isWindows ? "javac.exe" : "javac";

        // 1. Tìm trong Java Home (Folder đang chạy ứng dụng này)
        Path javaHomeBin = Path.of(System.getProperty("java.home"), "bin", binaryName);
        if (javaHomeBin.toFile().exists()) {
            return javaHomeBin.toString();
        }

        // 2. Fallback: Gọi lệnh 'javac' trực tiếp (nếu đã set trong Environment Variables)
        return "javac";
    }
}