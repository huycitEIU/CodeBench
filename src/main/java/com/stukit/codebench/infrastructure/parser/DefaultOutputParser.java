package com.stukit.codebench.infrastructure.parser;

public class DefaultOutputParser implements OutputParser{
    @Override
    public String normalize(String raw) {
        if (raw == null) return "";

        String normalized =
                raw.replace("\r\n", "\n").replace("\r", "\n");

        while (normalized.endsWith("\n")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
