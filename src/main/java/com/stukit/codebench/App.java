package com.stukit.codebench;

import com.stukit.codebench.ui.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {

        loadCustomFonts();

        // 1. Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/stukit/codebench/fxml/main.fxml"));
        String themeLightPath = getClass().getResource("/com/stukit/codebench/css/theme-light.css").toExternalForm();
        Parent root = loader.load();

        // 2. Lấy Controller từ Loader
        // (Đây là bước quan trọng để truy cập được hàm shutdown)
        MainController controller = loader.getController();

        // 3. Thiết lập Scene
        primaryStage.setTitle("Stukit CodeBench - Java Judge System");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.getScene().getStylesheets().add(themeLightPath);

        // 4. Bắt sự kiện đóng cửa sổ (Nhấn nút X)
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Application is closing...");

            // Gọi hàm dọn dẹp trong Controller
            if (controller != null) {
                controller.shutdown();
            }

            // Đóng JavaFX Platform
            Platform.exit();

            // Buộc dừng JVM (đảm bảo tắt hết các luồng ngầm nếu còn sót)
            System.exit(0);
        });

        primaryStage.show();
    }

    private void loadCustomFonts() {
        try {
            // Đường dẫn phải bắt đầu bằng dấu / (tính từ thư mục resources)
            // Load font cho Code Editor (Monospace)
            Font fontCode = Font.loadFont(getClass().getResourceAsStream("/com/stukit/codebench/fonts/JetBrainsMono-Regular.ttf"), 12);

            // Load font cho Giao diện (UI)
            Font fontUI = Font.loadFont(getClass().getResourceAsStream("/com/stukit/codebench/fonts/Inter-Regular.ttf"), 12);

            // --- QUAN TRỌNG: In tên font ra để biết tên mà điền vào CSS ---
            if (fontCode != null) System.out.println("Loaded Font: " + fontCode.getFamily());
            if (fontUI != null) System.out.println("Loaded Font: " + fontUI.getFamily());

        } catch (Exception e) {
            System.err.println("Không thể load font: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
