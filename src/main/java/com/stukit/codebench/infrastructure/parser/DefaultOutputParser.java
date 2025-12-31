package com.stukit.codebench.infrastructure.parser;

/**
 * Parser mặc định: So sánh chính xác (Strict Mode).
 * * <p>Quy tắc:
 * <ul>
 * <li>Chuẩn hoá mọi kiểu xuống dòng (\r\n, \r) thành \n</li>
 * <li>Xoá khoảng trắng và dòng trống dư thừa Ở CUỐI file (Trailing whitespaces)</li>
 * <li>GIỮ NGUYÊN khoảng trắng ở đầu dòng hoặc giữa các từ</li>
 * </ul>
 */
public class DefaultOutputParser implements OutputParser {
    @Override
    public String normalize(String raw) {
        if (raw == null) return "";

        // 1. stripTrailing(): Xoá space/newline thừa ở CUỐI file (Java 11+)
        // 2. replaceAll("\\R", "\n"): Chuẩn hoá mọi loại xuống dòng về \n
        return raw.stripTrailing().replaceAll("\\R", "\n");
    }
}