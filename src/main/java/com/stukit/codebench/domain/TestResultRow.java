package com.stukit.codebench.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TestResultRow {
    // Dữ liệu gốc
    private final TestCase testCase;

    // Dữ liệu hiển thị lên UI (dùng Property để UI tự update)
    private final StringProperty testName;
    private final StringProperty status; // Waiting, Running, AC, WA, TLE...
    private final StringProperty time;   // 15ms, 1.2s...
    private final StringProperty message; // Chi tiết lỗi nếu có

    public TestResultRow(TestCase testCase) {
        this.testCase = testCase;
        this.testName = new SimpleStringProperty(testCase.getName()); 
        this.status = new SimpleStringProperty("Waiting"); // Mặc định 
        this.time = new SimpleStringProperty("");
        this.message = new SimpleStringProperty("");
    }

    // Getters for Property (bắt buộc để TableView hiểu)
    public StringProperty testNameProperty() { return testName; }
    public StringProperty statusProperty() { return status; }
    public StringProperty timeProperty() { return time; }
    public StringProperty messageProperty() { return message; }

    // Getter thường
    public TestCase getTestCase() { return testCase; }

    // Setter tiện ích
    public void setStatus(String s) { this.status.set(s); }
    public void setTime(String t) { this.time.set(t); }
    public void setMessage(String m) { this.message.set(m); }
}
