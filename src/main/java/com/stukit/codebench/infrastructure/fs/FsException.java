package com.stukit.codebench.infrastructure.fs;

/**
 * Exception cho các lỗi liên quan đến filesystem.
 *
 * <p>Đây là RuntimeException vì:
 * <ul>
 *     <li>Lỗi FS thường không recover được</li>
 *     <li>Không muốn làm bẩn domain layer bằng IOException</li>
 * </ul>
 */
public class FsException extends RuntimeException{
    public FsException(String message, Throwable cause) {
        super(message, cause);
    }
}
