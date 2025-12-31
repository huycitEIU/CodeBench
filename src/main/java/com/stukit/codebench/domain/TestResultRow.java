package com.stukit.codebench.domain;

import com.stukit.codebench.domain.TestCase;
import javafx.beans.property.*;

import java.nio.file.Path;

public class TestResultRow {
    // 1. Tham chiếu ngược lại TestCase gốc (Để lấy Input/Expected khi cần)
    private final TestCase originalTestCase;

    // 2. Các thông tin kết quả chạy (Dynamic)
    private final SimpleStringProperty name; // Tên lấy từ TestCase
    private final SimpleStringProperty status; // AC, WA, TLE, Running
    private final SimpleStringProperty runtime;

    // 3. Đường dẫn Output thực tế (Mới sinh ra sau khi chạy)
    private Path actualOutputPath;

    public TestResultRow(TestCase testCase) {
        this.originalTestCase = testCase;

        // Khởi tạo mặc định cho UI
        this.name = new SimpleStringProperty(testCase.getName());
        this.status = new SimpleStringProperty("Waiting...");
        this.runtime = new SimpleStringProperty("-");
    }

    // --- Setter cập nhật kết quả sau khi chạy xong ---
    public void updateResult(String status, long timeMs, Path actualOutput) {
        this.status.set(status);
        this.runtime.set(timeMs + " ms");
        this.actualOutputPath = actualOutput;
    }

    // --- Getters phục vụ logic "Xem chi tiết" ---
    public Path getExpectedOutputPath() {
        return originalTestCase.getExpectedOutputPath();
    }

    public Path getActualOutputPath() {
        return actualOutputPath;
    }



    // --- Property Getters phục vụ JavaFX TableView ---
    public StringProperty nameProperty() { return name; }
    public StringProperty statusProperty() { return status; }
    public StringProperty runtimeProperty() { return runtime; }

    public void reset() {
        this.status.set("Waiting");
        this.runtime.set("-");
        this.actualOutputPath = null;
    }
    public void updateStatus(String s) {
        this.status.set(s);
    }
    public TestCase getTestCase() {
        return this.originalTestCase;
    }
}