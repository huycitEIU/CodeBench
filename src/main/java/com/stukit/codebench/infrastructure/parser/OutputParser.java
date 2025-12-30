package com.stukit.codebench.infrastructure.parser;

/**
 * Chuẩn hoá output cho chương trình và expected output
 * trước khi so sánh.
 */
public interface OutputParser {
    /**
     *
     * @param raw output gốc từ chương trình
     * @return output đã được chuẩn hoá
     */
    String normalize(String raw);
}
