package com.stukit.codebench.infrastructure.fs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

/**
 * Triển khai Workspace dựa trên thư mục tạm của hệ điều hành.
 *
 * <p>Mỗi TempWorkspace tương ứng với một thư mục vật lý riêng biệt,
 * được xoá hoàn toàn khi close().
 *
 * <p>Lớp này KHÔNG biết gì về compiler, runner hay sandbox.
 * Nó chỉ làm đúng một việc: quản lý filesystem.
 */
public class TempWorkspace implements Workspace{

    private final Path root;

    /**
     *
     * @param root thư mục gốc đã được tạo sẵn
     */
    public TempWorkspace(Path root) {
        this.root = root;
    }

    @Override
    public Path getRoot() {
        return root;
    }

    @Override
    public Path resolve(String relativePath) {
        return root.resolve(relativePath);
    }

    @Override
    public void write(String relativePath, String content) {
        try {
            Path file = resolve(relativePath);
            Files.createDirectories(file.getParent());
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
     * Xoá toànbojoj nội dung workspace theo thứ tự ngược (file -> thư mục)
     */
    @Override
    public void close() {
        try {
            if (!Files.exists(root)) return;

            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ingored) {}
                    });
        } catch (IOException e) {
            throw new FsException("Không thể cleanup workspace", e);
        }
    }
}
