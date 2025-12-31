package com.stukit.codebench.ui.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

/**
 * Helper class chuyên xử lý các Dialog, Alert thông báo.
 */
public class ViewHelper {

    public static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị Dialog so sánh Output (Expected vs Actual).
     */
    public static void showDiffDialog(Window owner, String title, String expected, String actual, String cssPath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết: " + title);
        alert.setHeaderText("So sánh kết quả");
        alert.initOwner(owner);

        // Tùy chỉnh kích thước
        alert.getDialogPane().setMinWidth(800);
        alert.getDialogPane().setMinHeight(500);

        // Load CSS nếu có
        if (cssPath != null) {
            alert.getDialogPane().getStylesheets().add(cssPath);
        }

        TextArea txtExpected = createReadOnlyTextArea(expected);
        TextArea txtActual = createReadOnlyTextArea(actual);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Expected Output:"), 0, 0);
        grid.add(txtExpected, 0, 1);
        GridPane.setHgrow(txtExpected, Priority.ALWAYS);
        GridPane.setVgrow(txtExpected, Priority.ALWAYS);

        grid.add(new Label("Actual Output:"), 1, 0);
        grid.add(txtActual, 1, 1);
        GridPane.setHgrow(txtActual, Priority.ALWAYS);
        GridPane.setVgrow(txtActual, Priority.ALWAYS);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    private static TextArea createReadOnlyTextArea(String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false); // Code không nên wrap text
        textArea.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Monospaced'; -fx-font-size: 12px;");
        return textArea;
    }
}