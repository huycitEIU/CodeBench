package com.stukit.codebench.infrastructure.compiler;

import com.stukit.codebench.infrastructure.fs.Workspace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Service chịu trách nhiệm biên dịch source code Java.
 *
 * <p>LƯU Ý:
 * <ul>
 *     <li>KHÔNG tạo thư mục</li>
 *     <li>KHÔNG quản lý filesystem</li>
 *     <li>MỌI thao tác file phải thông qua Workspace</li>
 * </ul>
 */
public class JavaCompilerService {

    /**
     * Biên dịch source code Java trong workspace hiện tại
     *
     * @param workspace workspace đã được tạo sẵn
     * @param sourceCode mã nguồn Java (class Solution)
     * @return thư mục workspace chứa file .class
     * @throws CompileException nếu có lỗi biên dịch
     */
    public void compile(Workspace workspace, String sourceCode) throws CompileException {
        // 1. Ghi code ra file
        workspace.write("Solution.java", sourceCode);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("javac", "Solution.java");
            processBuilder.directory(workspace.getRoot().toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // 2. Nếu compile thất bại
            if (exitCode != 0) {
                StringBuilder errorMessage = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMessage.append(line).append("\n");
                    }
                }
                // Ném lỗi ra ngoài try-catch để không bị bắt lại bên dưới
                throw new CompileException(errorMessage.toString().trim());
            }

        } catch (CompileException e) {
            // Nếu là lỗi biên dịch đã ném ở trên, thì ném tiếp ra ngoài
            throw e;
        } catch (Exception e) {
            // Chỉ bắt các lỗi hệ thống (IOException, InterruptedException...)
            throw new CompileException("Lỗi hệ thống khi gọi trình biên dịch", e);
        }
    }

}
