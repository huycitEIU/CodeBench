package com.stukit.codebench.infrastructure.fs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Implementation của Workspace sử dụng thư mục tạm.
 * Tự động xóa toàn bộ thư mục khi gọi close().
 */
public class TempWorkspace implements Workspace {

    private final Path root;

    public TempWorkspace(Path root) {
        this.root = root;
    }

    @Override
    public Path getRoot() {
        return root;
    }

    @Override
    public Path resolve(String relativePath) {
        // Path.resolve xử lý nối chuỗi đường dẫn an toàn
        return root.resolve(relativePath).toAbsolutePath();
    }

    @Override
    public void write(String relativePath, String content) {
        try {
            Path file = resolve(relativePath);
            Path parent = file.getParent();

            // Tạo thư mục cha nếu chưa có (và nếu cha không phải là null)
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    file,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new FsException("Không thể ghi file: " + relativePath, e);
        }
    }

    @Override
    public boolean exist(String relativePath) {
        return Files.exists(resolve(relativePath));
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            throw new FsException("Không thể xoá file: " + relativePath, e);
        }
    }

    /**
     * Đây là method quan trọng nhất để dọn rác.
     * Được gọi tự động bởi try-with-resources thông qua close().
     */
    @Override
    public void close() {
        if (!Files.exists(root)) return;

        // Dùng try-with-resources cho Stream để tránh leak file handle
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()) // Quan trọng: Xóa con trước, cha sau
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        } catch (IOException e) {
            // Chỉ log warning, không throw exception để tránh làm gián đoạn luồng chính
            // (Ví dụ: file đang bị lock bởi tiến trình khác)
            System.err.printf("[Warning] Không thể dọn dẹp workspace %s: %s%n", root, e.getMessage());
        }
    }
}