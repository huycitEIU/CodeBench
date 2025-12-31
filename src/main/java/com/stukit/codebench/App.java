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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Class chính của ứng dụng JavaFX.
 * Chịu trách nhiệm thiết lập Stage, Scene và quản lý vòng đời ứng dụng.
 */
public class App extends Application {

    private static final String APP_TITLE = "Stukit CodeBench - Offline Judge";

    // Định nghĩa thư mục tạm ở một nơi cố định (để Controller cũng dùng được nếu cần)
    public static final Path APP_TEMP_ROOT = Path.of(System.getProperty("user.dir"), "temp-workspaces");
    // Đường dẫn Resource
    private static final String FXML_PATH = "/com/stukit/codebench/fxml/main.fxml";
    private static final String CSS_PATH = "/com/stukit/codebench/css/theme-light.css";
    private static final String FONT_CODE_PATH = "/com/stukit/codebench/fonts/JetBrainsMono-Regular.ttf";
    private static final String FONT_UI_PATH = "/com/stukit/codebench/fonts/Inter-Regular.ttf";

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Dọn dẹp rác từ lần chạy trước (kể cả khi bị tắt đột ngột)
        performStartupCleanup();

        // 2. Load Font
        loadCustomFonts();

        // 3. Load UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        Parent root = loader.load();
        MainController controller = loader.getController();

        Scene scene = new Scene(root, 1000, 700);
        String cssUrl = Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm();
        scene.getStylesheets().add(cssUrl);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);

        // Handle Exit (Cho trường hợp tắt bình thường)
        primaryStage.setOnCloseRequest(event -> handleExit(controller));

        // 4. Đăng ký Shutdown Hook (Phòng hờ trường hợp tắt bằng Ctrl+C trong Terminal)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // JVM Shutdown Hook: Cố gắng dọn dẹp nếu có thể
            // Lưu ý: Nút Stop IDE vẫn có thể bỏ qua cái này, nhưng có còn hơn không.
            performStartupCleanup();
        }));

        primaryStage.show();
    }

    /**
     * Logic dọn dẹp quan trọng nhất: Xóa thư mục temp-workspaces cũ.
     */
    private void performStartupCleanup() {
        if (!Files.exists(APP_TEMP_ROOT)) return;

        System.out.println("Cleaning up old workspaces at: " + APP_TEMP_ROOT);
        try (Stream<Path> walk = Files.walk(APP_TEMP_ROOT)) {
            walk.sorted(Comparator.reverseOrder()) // Xóa con trước, cha sau
                    .map(Path::toFile)
                    .forEach(file -> {
                        boolean deleted = file.delete();
                        if (!deleted && file.exists()) {
                            System.err.println("Không thể xóa file rác: " + file.getAbsolutePath());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Cảnh báo: Lỗi khi dọn dẹp khởi động - " + e.getMessage());
        }
    }

    /**
     * Load font từ resource.
     * Cần load thủ công vì JavaFX CSS đôi khi không tự load được font từ file .ttf nhúng trong jar.
     */
    private void loadCustomFonts() {
        loadFont(FONT_CODE_PATH, 12);
        loadFont(FONT_UI_PATH, 12);
    }

    private void loadFont(String path, double size) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                Font font = Font.loadFont(stream, size);
                // System.out.println("Loaded font: " + font.getFamily()); // Uncomment để debug
            } else {
                System.err.println("Warning: Could not find font at " + path);
            }
        } catch (Exception e) {
            System.err.println("Error loading font " + path + ": " + e.getMessage());
        }
    }

    /**
     * Logic tắt ứng dụng an toàn.
     */
    private void handleExit(MainController controller) {
        System.out.println("Application is closing...");

        // 1. Dọn dẹp tài nguyên (xóa file tạm, kill process con)
        if (controller != null) {
            controller.shutdown();
        }

        // 2. Tắt JavaFX Thread
        Platform.exit();

        // 3. Kill JVM (Bắt buộc để đảm bảo các thread 'Process' của Runner bị tắt hẳn)
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}