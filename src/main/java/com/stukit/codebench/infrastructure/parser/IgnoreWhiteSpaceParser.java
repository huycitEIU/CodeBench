package com.stukit.codebench.infrastructure.parser;

public class IgnoreWhiteSpaceParser implements OutputParser{
    @Override
    public String normalize(String raw) {
        if (raw == null) return "";
        return raw.replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
