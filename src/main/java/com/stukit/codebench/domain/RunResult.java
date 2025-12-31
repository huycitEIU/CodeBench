package com.stukit.codebench.domain;

import java.nio.file.Path;

public record RunResult(boolean isTimeout, int exitCode, long runTimeMs,long memoryBytes, Path stdoutPath, Path stderrPath) {

    /**
     * Factory method tạo kết quả Timeout.
     */
    public static RunResult timeout(long limitMs, Path stdoutPath, Path stderrPath) {
        // Exit code -1 ám chỉ process bị kill hoặc lỗi
        return new RunResult(true, -1, limitMs, 0, stdoutPath, stderrPath);
    }

    /**
     * Logic kiểm tra Runtime Error: Không timeout VÀ exitCode khác 0.
     */
    public boolean hasRuntimeError() {
        return !isTimeout && exitCode != 0;
    }
}