package com.stukit.codebench.infrastructure.sandbox;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sandbox chạy local, sử dụng ProcessBuilder của Java.
 * <p>
 * <b>Cơ chế giới hạn bộ nhớ:</b>
 * Vì Java không thể trực tiếp giới hạn RAM của process con qua OS (trừ khi dùng JNI/Docker),
 * nên class này sử dụng kỹ thuật "Argument Injection":
 * Tự động chèn cờ {@code -Xmx} nếu phát hiện đang chạy lệnh Java.
 */
public class LocalSandbox implements Sandbox {
    private final Path workspaceRoot;
    private final long timeLimitMs;
    private final long memoryLimitBytes;

    public LocalSandbox(Path workspaceRoot, long timeLimitMs, long memoryLimitBytes) {
        this.workspaceRoot = workspaceRoot;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitBytes = memoryLimitBytes;
    }

    @Override
    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }

    @Override
    public long getTimeLimitMs() {
        return timeLimitMs;
    }

    @Override
    public long getMemoryLimitBytes() {
        return memoryLimitBytes;
    }

    @Override
    public void apply(ProcessBuilder processBuilder) {
        // 1. Thiết lập thư mục làm việc (Quan trọng để code đọc/ghi file đúng chỗ)
        processBuilder.directory(workspaceRoot.toFile());

        // 2. Bảo mật: Xóa sạch biến môi trường của máy chủ
        // Tránh việc code người dùng đọc được PATH, USERNAME, v.v.
        Map<String, String> env = processBuilder.environment();
        env.clear();

        // 3. Giới hạn bộ nhớ (Memory Limit Logic)
        applyMemoryLimit(processBuilder);
    }

    private void applyMemoryLimit(ProcessBuilder pb) {
        if (memoryLimitBytes <= 0) return;

        List<String> command = pb.command();
        if (command.isEmpty()) return;

        // Lấy tên file thực thi (ví dụ: "java", "C:/.../bin/java.exe")
        String executable = command.get(0).toLowerCase();

        // Kiểm tra xem có phải lệnh Java không
        if (executable.endsWith("java") || executable.endsWith("java.exe")) {
            // Tính toán dung lượng RAM (MB)
            long memoryInMb = memoryLimitBytes / 1024 / 1024;

            // Đảm bảo tối thiểu 64MB để JVM khởi động ổn định trên hầu hết cấu hình
            if (memoryInMb < 64) memoryInMb = 64;

            // Tạo danh sách lệnh mới để chèn flag -Xmx
            // Cũ: [java, -cp, ..., Solution]
            // Mới: [java, -Xmx128m, -cp, ..., Solution]
            List<String> newCommand = new ArrayList<>(command);
            newCommand.add(1, "-Xmx" + memoryInMb + "m");

            pb.command(newCommand);
        }

        // Ghi chú: Với C/C++ (gcc/g++), việc giới hạn RAM trên Windows qua Java
        // rất phức tạp và cần thư viện ngoài (JNI). Hiện tại chỉ support Java.
    }
}