package com.stukit.codebench.infrastructure.fs;

import java.io.Serial;

/**
 * Exception unchecked dùng cho các lỗi IO/Filesystem.
 * Giúp code nghiệp vụ sạch hơn, không bị dính checked IOException.
 */
public class FsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FsException(String message, Throwable cause) {
        super(message, cause);
    }
}