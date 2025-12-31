package com.stukit.codebench.domain;

import java.nio.file.Path;

public class RunResult {
    // Thay vì lưu nội dung (String), ta lưu đường dẫn đến file (Path)
    private final Path stdoutPath;
    private final Path stderrPath;

    private final long runTime;
    private final boolean timeout;
    private final int exitCode;

    /**
     * Constructor chính
     * @param timeout: Có bị quá giờ không
     * @param exitCode: Mã thoát (0 là thành công)
     * @param runTime: Thời gian chạy (ms)
     * @param stdoutPath: Đường dẫn file chứa Standard Output
     * @param stderrPath: Đường dẫn file chứa Standard Error
     */
    public RunResult(boolean timeout, int exitCode, long runTime, Path stdoutPath, Path stderrPath) {
        this.timeout = timeout;
        this.exitCode = exitCode;
        this.runTime = runTime;
        this.stdoutPath = stdoutPath;
        this.stderrPath = stderrPath;
    }

    /**
     * Factory method cho trường hợp Timeout.
     * Khi timeout, thường ta không quan tâm output, hoặc process chưa kịp ghi xong.
     * Truyền null vào Path để biểu thị không có kết quả hợp lệ.
     */
    public static RunResult timeout(long limitMs) {
        return new RunResult(true, -1, limitMs, null, null);
    }

    /**
     * (Optional) Factory method cho Timeout nhưng vẫn muốn giữ lại file output
     * (để xem code in ra cái gì trước khi bị kill).
     */
    public static RunResult timeout(long limitMs, Path stdoutPath, Path stderrPath) {
        return new RunResult(true, -1, limitMs, stdoutPath, stderrPath);
    }

    // Logic kiểm tra Runtime Error: Không timeout VÀ exitCode khác 0
    public boolean hasRuntimeError() {
        return !timeout && exitCode != 0;
    }

    // --- Getters ---
    public Path getStdoutPath() { return stdoutPath; }
    public Path getStderrPath() { return stderrPath; }
    public long getRunTime() { return runTime; }
    public boolean isTimeout() { return timeout; }
    public int getExitCode() { return exitCode; }
}