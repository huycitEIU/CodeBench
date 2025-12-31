package com.stukit.codebench.infrastructure.parser;

/**
 * Interface chuẩn hoá output để so sánh.
 * Áp dụng mô hình Strategy Pattern.
 */
@FunctionalInterface
public interface OutputParser {
    /**
     * Chuẩn hoá chuỗi đầu vào.
     * @param raw chuỗi gốc (có thể null)
     * @return chuỗi đã chuẩn hoá (không bao giờ null)
     */
    String normalize(String raw);
}