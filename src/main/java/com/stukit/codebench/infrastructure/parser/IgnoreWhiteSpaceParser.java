package com.stukit.codebench.infrastructure.parser;

import java.util.regex.Pattern;

/**
 * Parser bỏ qua mọi khác biệt về khoảng trắng (Relaxed Mode).
 * Thường dùng khi đề bài không yêu cầu định dạng output chặt chẽ.
 * * <p>Quy tắc:
 * <ul>
 * <li>Coi mọi ký tự khoảng trắng (space, tab, newline) là như nhau.</li>
 * <li>Gộp nhiều khoảng trắng liên tiếp thành 1 dấu cách duy nhất.</li>
 * <li>Trim 2 đầu.</li>
 * </ul>
 * Ví dụ: "1   \n 2" sẽ thành "1 2"
 */
public class IgnoreWhiteSpaceParser implements OutputParser {

    // Pre-compile Regex để tăng hiệu năng
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public String normalize(String raw) {
        if (raw == null) return "";

        // Thay thế toàn bộ cụm khoảng trắng bất kỳ thành 1 dấu cách đơn
        return WHITESPACE_PATTERN.matcher(raw).replaceAll(" ").trim();
    }
}