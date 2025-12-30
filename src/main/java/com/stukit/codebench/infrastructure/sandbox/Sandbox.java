package com.stukit.codebench.infrastructure.sandbox;

import com.stukit.codebench.infrastructure.fs.Workspace;

import java.nio.file.Path;

/**
 * Sandbox kiểm soát môi trường thực thi chương trình người dùng.
 *
 * <p>Sandbox chịu trách nhiệm:
 * <ul>
 *     <li>Giới hạn thời gian chạy</li>
 *     <li>Giới hạn tài nguyên (mở rộng sau)</li>
 *     <li>Cô lập thư mục làm việc</li>
 * </ul>
 *
 * <p>Runner KHÔNG được tự ý thiết lập giới hạn,
 * mọi cấu hình phải đi qua Sandbox.
 */
public interface Sandbox {
    /**
     *
     * @return thư mục workspace được phép truy cập
     */
    Path workspaceRoot();

    /**
     *
     * @return thời gian chạy tối đa (millisecond)
     */
    long timeLimits();

    /**
     * Hiện tại chưa sử dụng
     * @return bộ nhớ tối đa
     */
    long memoryLimitBytes();

    /**
     * Áp dụng sandbox vào ProccessBuilder trước khi start().
     * @param processBuilder chuẩn bị chạy code
     */
    void apply(ProcessBuilder processBuilder);
}
