package com.stukit.codebench.infrastructure.fs;

import java.nio.file.Path;

/**
 * Đại diện cho một không gian làm việc độc lập trên hệ thống file.
 *
 * <p>Mỗi Workspace tương ứng với một lần chấm bài / chạy code.
 * Workspace chịu trách nhiệm:
 * <ul>
 *     <li>Quản lý thư mục gốc riêng biệt</li>
 *     <li>Ghi / đọc file phục vụ compile & run</li>
 *     <li>Dọn dẹp toàn bộ tài nguyên khi kết thúc</li>
 * </ul>
 *
 * <p>Workspace là ranh giới giữa domain logic và hệ điều hành.
 * Compiler / Runner không được tự ý thao tác file ngoài Workspace.
 */
public interface Workspace extends AutoCloseable {

    /**
     *
     * @return đường dẫn đến thư mục gốc của workspace
     */
    Path getRoot();

    /**
     * Resolve đường dẫn tương đối trong workspace
     *
     * @param relativePath đường dẫn tương đối
     * @return Path tuyệt đối đã được normalize
     */
    Path resolve(String relativePath);

    /**
     * Ghi nội dung vào file trong workspace
     * Nếu file hoặc thư mục cha chưa tồn tại thì sẽ tạo một cái mới.
     *
     * @param relativePath  đường dẫn tương đối
     * @param content nội dung cần ghi
     */
    void write(String relativePath, String content);

    /**
     * kiểm tra xem có tồn tại file này trong workspace hay không
     *
     * @param relativePath đường dẫn tương đối
     * @return true nếu tồn tại
     */
    boolean exist(String relativePath);

    /**
     * Xoá file trong workspace nếu nó tồn tại
     * @param relativePath
     */
    void delete(String relativePath);

    /**
     * Dọn dẹp toàn bộ workspace
     * Được gọi tự động khi dùng try-with-resources.
     */
    @Override
    void close();
}
