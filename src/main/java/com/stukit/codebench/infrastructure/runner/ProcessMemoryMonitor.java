package com.stukit.codebench.infrastructure.runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class tiện ích để theo dõi bộ nhớ của một Process đang chạy.
 * Sử dụng kỹ thuật Polling (hỏi liên tục) hệ điều hành.
 */
public class ProcessMemoryMonitor extends Thread {
    private final Process process;
    private long peakMemoryBytes = 0;
    private volatile boolean running = true;

    public ProcessMemoryMonitor(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
        long pid = process.pid(); // Java 9+ feature

        while (running && process.isAlive()) {
            long currentMem = getMemoryUsage(pid);
            if (currentMem > peakMemoryBytes) {
                peakMemoryBytes = currentMem;
            }

            try {
                // Kiểm tra mỗi 50ms.
                // Nếu giảm xuống thấp hơn sẽ ít tốn CPU nhưng độ chính xác giảm.
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopMonitoring() {
        this.running = false;
        this.interrupt(); // Đánh thức nếu đang ngủ
    }

    public long getPeakMemoryBytes() {
        return peakMemoryBytes;
    }

    /**
     * Gọi lệnh hệ thống để lấy bộ nhớ (RSS/Working Set).
     */
    private long getMemoryUsage(long pid) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Windows: tasklist /FI "PID eq <pid>" /FO CSV /NH
                // Output: "java.exe","1234","Console","1","15,230 K"
                pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH");
            } else {
                // Linux/Mac: ps -p <pid> -o rss=
                // Output: 15230 (đơn vị KB)
                pb = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-o", "rss=");
            }

            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.isBlank()) {
                    return parseMemoryString(line, os);
                }
            }
        } catch (Exception ignored) {
            // Process có thể đã kết thúc nhanh quá, không kịp lấy
        }
        return 0;
    }

    private long parseMemoryString(String line, String os) {
        try {
            if (os.contains("win")) {
                // Format: "Image Name","PID","Session","Session#","Mem Usage"
                // VD: "java.exe","1234","Console","1","15,230 K"
                String[] parts = line.split("\",\"");
                if (parts.length >= 5) {
                    // Lấy phần cuối, bỏ dấu ngoặc kép và chữ K
                    String memStr = parts[4].replaceAll("[\" K,]", "");
                    return Long.parseLong(memStr) * 1024; // KB -> Bytes
                }
            } else {
                // Linux: Trả về số KB trực tiếp (VD: 15230)
                return Long.parseLong(line.trim()) * 1024;
            }
        } catch (NumberFormatException e) {
            // Ignored
        }
        return 0;
    }
}