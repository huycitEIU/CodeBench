package com.stukit.codebench.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.nio.file.Path;

/**
 * Model đại diện cho một dòng trong bảng kết quả.
 * Kết nối giữa dữ liệu (TestCase) và giao diện (TableView).
 */
public class TestResultRow {

    // Dữ liệu gốc (Immutable Record)
    private final TestCase originalTestCase;

    // Các Property để Binding lên UI (TableView)
    private final StringProperty name;
    private final StringProperty status;
    private final StringProperty runtime;
    private final SimpleStringProperty memory;

    // Đường dẫn file output thực tế sinh ra sau khi chạy (có thể null nếu chưa chạy)
    private Path actualOutputPath;

    public TestResultRow(TestCase testCase) {
        this.originalTestCase = testCase;

        // Lưu ý: Vì TestCase là Record nên dùng testCase.name()
        this.name = new SimpleStringProperty(testCase.name());

        // Trạng thái mặc định
        this.status = new SimpleStringProperty("Waiting...");
        this.runtime = new SimpleStringProperty("-");
        this.memory = new SimpleStringProperty("-");
    }

    /**
     * Cập nhật kết quả cuối cùng (Sau khi chấm xong).
     */
    public void updateResult(String statusText, long timeMs, long memoryBytes, Path actualOutput) {
        this.status.set(statusText);
        this.runtime.set(timeMs + " ms");
        // Convert Bytes sang KB hoặc MB cho dễ đọc
        double memMB = memoryBytes / 1024.0 / 1024.0;
        this.memory.set(String.format("%.2f MB", memMB));
        this.actualOutputPath = actualOutput;
    }

    /**
     * Cập nhật trạng thái tạm thời (Ví dụ: "Running...").
     */
    public void updateStatus(String statusText) {
        this.status.set(statusText);
    }

    /**
     * Reset về trạng thái chờ (Khi bấm nút Run lại).
     */
    public void reset() {
        this.status.set("Waiting...");
        this.runtime.set("-");
        this.actualOutputPath = null;
    }

    // --- Getters cho Logic ---

    public TestCase getTestCase() {
        return originalTestCase;
    }

    public Path getExpectedOutputPath() {
        // Record accessor
        return originalTestCase.expectedOutputPath();
    }

    public Path getActualOutputPath() {
        return actualOutputPath;
    }

    // --- Getters cho JavaFX TableView (Property Pattern) ---

    public StringProperty nameProperty() { return name; }
    public StringProperty statusProperty() { return status; }
    public StringProperty runtimeProperty() { return runtime; }
    public StringProperty memoryProperty() { return memory; }
}