package com.stukit.codebench.ui.helper;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Helper class chuyên xử lý các Dialog, Alert thông báo.
 */
public class ViewHelper {

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị Dialog so sánh Output (Expected vs Actual).
     */
    public void showDiffDialog(Window owner, String title, String expected, String actual, String cssPath) {
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

    public void showAboutDialog(String css) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewHelper.class.getResource("/com/stukit/codebench/fxml/about.fxml"));
            Parent root = loader.load();
            root.getStylesheets().add(css);

            Stage stage = new Stage();
            setIcon(stage);
            stage.setTitle("About CodeBench");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn cửa sổ chính cho đến khi đóng about
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDocument(String css) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/stukit/codebench/fxml/document.fxml"));
            Parent root = loader.load();
            root.getStylesheets().add(css);

            Stage stage = new Stage();
            setIcon(stage);
            stage.setTitle("Tài liệu hướng dẫn");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TextArea createReadOnlyTextArea(String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false); // Code không nên wrap text
        textArea.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', 'Monospaced'; -fx-font-size: 12px;");
        return textArea;
    }

    private void setIcon(Stage stage) {
        try {
            // Lưu ý đường dẫn bắt đầu bằng dấu / (tính từ thư mục resources)
            Image icon = new Image(getClass().getResourceAsStream("/com/stukit/codebench/icons/logo64.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Lỗi không tìm thấy file icon: " + e.getMessage());
        }
    }
}