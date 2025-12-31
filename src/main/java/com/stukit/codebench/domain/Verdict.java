package com.stukit.codebench.domain;

/**
 * Định nghĩa các kết quả phán quyết (Verdict) của hệ thống chấm.
 */
public enum Verdict {
    PASSED("AC"),
    FAILED("WA"),
    COMPILE_ERROR("CE"),
    RUNTIME_ERROR("RTE"),
    TIME_LIMIT_EXCEEDED("TLE"),
    SYSTEM_ERROR("SE"),
    UNKNOWN("UN");

    private final String abbreviation;

    Verdict(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Lấy tên viết tắt (dùng cho UI).
     * @return chuỗi viết tắt (VD: AC, WA, TLE...)
     */
    public String getAbbreviation() {
        return abbreviation;
    }
}