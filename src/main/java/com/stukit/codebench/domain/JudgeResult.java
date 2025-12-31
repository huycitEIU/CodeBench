package com.stukit.codebench.domain;

import java.nio.file.Path;

public class JudgeResult {
    private final String name;
    private final Verdict verdict;
    private final long runTimeMs;

    // Thay đổi: Lưu Path thay vì String nội dung
    private final Path actualOutputPath;
    private final Path errorOutputPath;

    // Dùng cho Compile Error hoặc System Error (những lỗi không sinh ra file output chuẩn)
    private final String message;

    private String status;

    /**
     * Constructor 1: Dùng cho trường hợp chạy Code xong (có Output file hoặc Error file)
     * (AC, WA, TLE, RTE)
     */
    public JudgeResult(String name, Verdict verdict, long runTimeMs, Path actualOutputPath, Path errorOutputPath) {
        this.name = name;
        this.verdict = verdict;
        this.runTimeMs = runTimeMs;
        this.actualOutputPath = actualOutputPath;
        this.errorOutputPath = errorOutputPath;
        this.message = null; // Không dùng message string
        this.status = mapVerdictToStatus(verdict);
    }

    /**
     * Constructor 2: Dùng cho trường hợp Lỗi biên dịch hoặc Lỗi hệ thống (Chỉ có message)
     * (CE, SE)
     */
    public JudgeResult(String name, Verdict verdict, String message) {
        this.name = name;
        this.verdict = verdict;
        this.runTimeMs = 0;
        this.actualOutputPath = null;
        this.errorOutputPath = null;
        this.message = message;
        this.status = mapVerdictToStatus(verdict);
    }

    // Helper để map Enum sang String (Java 17 Switch expression)
    private String mapVerdictToStatus(Verdict verdict) {
        if (verdict == null) return "UN";
        return switch (verdict) {
            case PASSED -> "AC";
            case FAILED -> "WA";
            case RUNTIME_ERROR -> "RTE"; // Sửa lại cho ngắn gọn
            case TIME_LIMIT_EXCEEDED -> "TLE";
            case COMPILE_ERROR -> "CE";
            case SYSTEM_ERROR -> "SE";
            default -> "UN";
        };
    }

    public boolean isPassed() {
        return Verdict.PASSED == verdict;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public long getRunTimeMs() {
        return runTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public Path getActualOutputPath() {
        return actualOutputPath;
    }

    public Path getErrorOutputPath() {
        return errorOutputPath;
    }

    public String getMessage() {
        return message;
    }
}