package com.stukit.codebench.infrastructure.runner;

public class RunResult {
    private final String stdout;
    private final String stderr;
    private final long runTime;
    private final boolean timeout;
    private final int exitCode;

    public RunResult(boolean timeout, int exitCode, long runTime, String stdout, String stderr) {
        this.timeout = timeout;
        this.exitCode = exitCode;
        this.runTime = runTime;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public static RunResult runtimeError(int exitCode, String stdout, String stderr, int i) {
        return new RunResult(false, -1, 0, stdout, stderr);
    }

    public static RunResult success(String stdout, int i) {
        return new RunResult(false, i, 0, stdout, "");
    }

    public boolean hasRuntimeError() {
        // Nếu không timeout mà exitCode khác 0 -> Chắc chắn là Runtime Error
        return !timeout && exitCode != 0;
    }

    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public long getRunTime() { return runTime; }
    public boolean isTimeout() { return timeout; }

    // Factory method cho trường hợp Timeout
    public static RunResult timeout(long limitMs) {
        // exitCode là -1 hoặc tuỳ ý vì timeout quan trọng hơn
        return new RunResult(true, -1, limitMs, "", "Time limit exceeded");
    }
}