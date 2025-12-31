package com.stukit.codebench.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Service tiện ích cho việc đọc/ghi file.
 * Tách biệt logic IO ra khỏi business logic.
 */
public class FileIOService {

    private static final int DEFAULT_PREVIEW_LIMIT = 20 * 1024; // 20KB

    public String readString(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return "";
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public void writeString(Path path, String content) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Đọc một phần đầu của file để hiển thị (Lazy Loading Preview).
     */
    public String readPreview(Path path, int limitChars) {
        if (path == null || !Files.exists(path)) {
            return "[File not found]";
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buffer = new char[limitChars];
            int numRead = reader.read(buffer);

            if (numRead == -1) return "";

            String content = new String(buffer, 0, numRead);
            if (reader.ready() || numRead == limitChars) {
                content += "\n... [Output truncated. Only showing first " + limitChars + " chars]";
            }
            return content;
        } catch (IOException e) {
            return "[Error reading file: " + e.getMessage() + "]";
        }
    }

    public String readPreview(Path path) {
        return readPreview(path, DEFAULT_PREVIEW_LIMIT);
    }

    public void deletePath(Path path) {
        if (path == null || !Files.exists(path)) return;
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        } catch (IOException e) {
            System.err.println("Warning: Failed to delete path " + path);
        }
    }
}