package com.stukit.codebench.domain;

import java.nio.file.Path;

/**
 * Kết quả chấm bài cuối cùng cho một Test Case.
 * Chứa Verdict, thời gian chạy và đường dẫn output thực tế (nếu có).
 */
public class JudgeResult {
    private final String name;
    private final Verdict verdict;
    private final long runTimeMs;
    private final long memoryBytes;

    // Output files (chỉ có khi chạy code thành công/thất bại/RTE)
    private final Path actualOutputPath;
    private final Path errorOutputPath;

    // Message (chỉ dùng cho CE, SE hoặc khi không có file output)
    private final String message;

    // Private constructor để bắt buộc dùng Static Factory Methods
    private JudgeResult(String name, Verdict verdict, long runTimeMs, long memoryBytes, Path actualOutput, Path errorOutput, String message) {
        this.name = name;
        this.verdict = verdict;
        this.runTimeMs = runTimeMs;
        this.memoryBytes = memoryBytes;
        this.actualOutputPath = actualOutput;
        this.errorOutputPath = errorOutput;
        this.message = message;
    }

    /**
     * Factory: Tạo kết quả từ việc chạy code (Passed, Failed, TLE, RTE).
     */
    public static JudgeResult fromExecution(String name, Verdict verdict, long runTimeMs, long memoryBytes, Path actualOutput, Path errorOutput) {
        return new JudgeResult(name, verdict, runTimeMs, memoryBytes, actualOutput, errorOutput, null);
    }

    /**
     * Factory: Tạo kết quả từ lỗi biên dịch hoặc hệ thống (Compile Error, System Error).
     */
    public static JudgeResult fromError(String name, Verdict verdict, String message) {
        return new JudgeResult(name, verdict, 0, 0, null, null, message);
    }

    /**
     * Kiểm tra xem kết quả có phải là PASSED (AC) hay không.
     */
    public boolean isPassed() {
        return Verdict.PASSED == verdict;
    }

    // --- Getters ---
    public String getName() { return name; }
    public Verdict getVerdict() { return verdict; }
    public long getRunTimeMs() { return runTimeMs; }
    public long getMemoryBytes() { return  memoryBytes; }
    public Path getActualOutputPath() { return actualOutputPath; }
    public Path getErrorOutputPath() { return errorOutputPath; }
    public String getMessage() { return message; }

    // Lấy status string trực tiếp từ Verdict (Code gọn hơn)
    public String getStatus() {
        return verdict != null ? verdict.getAbbreviation() : "UN";
    }
}