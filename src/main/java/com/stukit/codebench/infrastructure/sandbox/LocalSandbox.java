package com.stukit.codebench.infrastructure.sandbox;

import java.nio.file.Path;
import java.util.Map;

/**
 * Sandbox chạy local trên máy người dùng.
 *
 * <p>KHÔNG phải sandbox bảo mật tuyệt đối.
 * Chỉ dùng cho môi trường desktop / offline.
 */
public class LocalSandbox implements Sandbox{
    private final Path workspaceRoot;
    private final long timeLimitMs;
    private final long memoryLimitBytes;

    public LocalSandbox(Path workspaceRoot, long timeLimitMs, long memoryLimitBytes) {
        this.workspaceRoot = workspaceRoot;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitBytes = memoryLimitBytes;
    }

    @Override
    public Path workspaceRoot() {
        return workspaceRoot;
    }

    @Override
    public long timeLimits() {
        return timeLimitMs;
    }

    @Override
    public long memoryLimitBytes() {
        return memoryLimitBytes;
    }

    @Override
    public void apply(ProcessBuilder processBuilder) {
        processBuilder.directory(workspaceRoot.toFile());
        Map<String, String> env = processBuilder.environment();
        env.clear();
    }
}
