package com.stukit.codebench.infrastructure.sandbox;

import java.nio.file.Path;

/**
 * Interface định nghĩa môi trường thực thi giới hạn (Sandbox).
 * <p>
 * Sandbox có trách nhiệm cấu hình:
 * <ul>
 * <li>Thư mục làm việc (Working Directory)</li>
 * <li>Biến môi trường (Environment Variables)</li>
 * <li>Giới hạn tài nguyên (RAM, Time)</li>
 * </ul>
 */
public interface Sandbox {

    /**
     * Lấy thư mục gốc mà process được phép chạy.
     */
    Path getWorkspaceRoot();

    /**
     * Lấy giới hạn thời gian (ms).
     */
    long getTimeLimitMs();

    /**
     * Lấy giới hạn bộ nhớ (Bytes).
     */
    long getMemoryLimitBytes();

    /**
     * Áp dụng các cấu hình sandbox vào ProcessBuilder.
     * <p>
     * Hàm này sẽ:
     * 1. Đặt thư mục làm việc.
     * 2. Xóa sạch biến môi trường (để bảo mật).
     * 3. Chèn tham số giới hạn bộ nhớ (nếu là Java process).
     *
     * @param processBuilder đối tượng chuẩn bị chạy code.
     */
    void apply(ProcessBuilder processBuilder);
}