package com.stukit.codebench.infrastructure.fs;

import java.nio.file.Path;

/**
 * Đại diện cho một không gian làm việc (sandbox thư mục) trên ổ đĩa.
 * <p>
 * Implement AutoCloseable để hỗ trợ try-with-resources:
 * <pre>
 * try (Workspace ws = factory.create()) {
 * // làm việc với file...
 * } // tự động xóa thư mục khi xong
 * </pre>
 */
public interface Workspace extends AutoCloseable {

    /**
     * Lấy đường dẫn tuyệt đối tới thư mục gốc của workspace.
     */
    Path getRoot();

    /**
     * Lấy đường dẫn tuyệt đối của một file bên trong workspace.
     * @param relativePath đường dẫn tương đối (vd: "main.cpp")
     */
    Path resolve(String relativePath);

    /**
     * Ghi nội dung text vào file (overwrite nếu đã tồn tại).
     * Tự động tạo các thư mục cha nếu chưa có.
     */
    void write(String relativePath, String content);

    /**
     * Kiểm tra file có tồn tại không.
     */
    boolean exist(String relativePath);

    /**
     * Xóa một file cụ thể (nếu tồn tại).
     */
    void delete(String relativePath);

    /**
     * Xóa toàn bộ workspace và giải phóng tài nguyên.
     */
    @Override
    void close();
}