package com.stukit.codebench.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileIOService {

    private static final int DEFAULT_PREVIEW_LIMIT = 20 * 1024; // 20KB

    /**
     * Đọc toàn bộ nội dung file (Dùng cho file code nhỏ, file config)
     */
    public String readString(Path path) throws IOException {
        if (!Files.exists(path)) return "";
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Ghi nội dung vào file (Tạo mới hoặc ghi đè)
     */
    public void writeString(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent()); // Đảm bảo thư mục cha tồn tại
        Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Đọc file an toàn cho UI (Lazy Loading Preview).
     * Chỉ đọc tối đa limitChars ký tự để tránh tràn bộ nhớ.
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

            // Nếu file vẫn còn dữ liệu chưa đọc hết -> Thêm thông báo
            if (reader.ready() || numRead == limitChars) {
                content += String.format("\n... [Output truncated. Only showing first %d chars]", limitChars);
            }
            return content;
        } catch (IOException e) {
            return "[Error reading file: " + e.getMessage() + "]";
        }
    }

    // Overload mặc định
    public String readPreview(Path path) {
        return readPreview(path, DEFAULT_PREVIEW_LIMIT);
    }

    /**
     * Xóa file hoặc thư mục (Xóa đệ quy nếu là thư mục)
     * Dùng để dọn dẹp workspace sau khi chạy xong.
     */
    public void deletePath(Path path) {
        if (path == null || !Files.exists(path)) return;
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()) // Xóa con trước, cha sau
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        } catch (IOException e) {
            System.err.println("Failed to delete path: " + path + " - " + e.getMessage());
        }
    }
}