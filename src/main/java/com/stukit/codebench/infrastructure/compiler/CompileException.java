package com.stukit.codebench.infrastructure.compiler;

import java.io.Serial;

/**
 * Exception checked được ném ra khi quá trình biên dịch thất bại.
 * (Do code sai cú pháp hoặc lỗi cấu hình trình biên dịch).
 */
public class CompileException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public CompileException(String message) {
        super(message);
    }

    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
}