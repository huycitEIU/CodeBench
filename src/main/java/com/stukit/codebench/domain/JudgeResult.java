package com.stukit.codebench.domain;

public class JudgeResult {
    private final String name;
    private final Verdict verdict;
    private final long runTimeMs;
    private final String output;
    private final String error;
    private String status = "UN";

    public JudgeResult(String name, Verdict verdict, long runTimeMs, String output, String error) {
        this.name = name;
        this.verdict = verdict;
        this.runTimeMs = runTimeMs;
        this.output = output;
        this.error = error;
        if (verdict == Verdict.PASSED) this.status = "AC";
        if (verdict == Verdict.FAILED) this.status = "WA";
        if (verdict == Verdict.RUNTIME_ERROR) this.status = "RUNTIME";
        if (verdict == Verdict.TIME_LIMIT_EXCEEDED) this.status = "TLE";
        if (verdict == Verdict.COMPILE_ERROR) this.status = "CE";
        if (verdict == Verdict.SYSTEM_ERROR) this.status = "SE";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPassed() {
        return Verdict.PASSED == verdict;
    }

    public String getError() {
        return error;
    }

    public String getOutput() {
        return output;
    }

    public long getRunTimeMs() {
        return runTimeMs;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public String getName() {
        return name;
    }
}
