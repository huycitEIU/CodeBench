package com.stukit.codebench.infrastructure.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Factory để tạo ra các Workspace biệt lập.
 */
public class WorkspaceFactory {
    private final Path baseDir;

    public WorkspaceFactory(Path baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Tạo một workspace tạm thời với tên ngẫu nhiên (codebench-xxx).
     */
    public Workspace create() {
        try {
            // Đảm bảo thư mục gốc tồn tại
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            Path tempDir = Files.createTempDirectory(baseDir, "codebench-");
            return new TempWorkspace(tempDir);
        } catch (IOException e) {
            throw new FsException("Không thể tạo workspace tại: " + baseDir, e);
        }
    }
}