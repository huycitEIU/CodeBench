package com.stukit.codebench.ui.component;

import com.stukit.codebench.domain.TestResultRow;
import javafx.scene.control.TableCell;

/**
 * Custom Cell để tô màu trạng thái (AC, WA, TLE...) trong bảng kết quả.
 */
public class StatusCell extends TableCell<TestResultRow, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);

        // Xóa style cũ trước khi set style mới
        getStyleClass().removeAll("status-ac", "status-wa", "status-tle", "status-running", "status-ce", "status-se");

        if (item != null && !empty) {
            switch (item) {
                case "AC" -> getStyleClass().add("status-ac");
                case "WA" -> getStyleClass().add("status-wa");
                case "TLE" -> getStyleClass().add("status-tle");
                case "CE" -> getStyleClass().add("status-ce");
                case "RTE", "SE" -> getStyleClass().add("status-se"); // Gom RTE và SE chung màu đỏ
                default -> {
                    if (item.startsWith("Run")) getStyleClass().add("status-running");
                }
            }
        }
    }
}