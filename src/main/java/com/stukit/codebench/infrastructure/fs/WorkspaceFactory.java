package com.stukit.codebench.infrastructure.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Factory tạo Workspace theo cấu hình chung của ứng dụng.
 *
 * <p>Mục đích:
 * <ul>
 *     <li>Tránh tạo workspace rải rác khắp nơi</li>
 *     <li>Dễ thay đổi chiến lược lưu trữ (local / sandbox / docker)</li>
 * </ul>
 */
public class WorkspaceFactory {
    private final Path baseDir;

    public WorkspaceFactory(Path baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Tạo workspace mới vơi thư mục riêng biệt.
     * @return Workspace sẵn sàng sử dụng
     */
    public Workspace create() {
        try {
            Files.createDirectories(baseDir);
            Path tempDir = Files.createTempDirectory(baseDir, "codebench-");
            return new TempWorkspace(tempDir);
        } catch (IOException e) {
            throw new FsException("Không thể tạo workspace", e);
        }
    }
}
