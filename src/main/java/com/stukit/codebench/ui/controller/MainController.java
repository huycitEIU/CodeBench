package com.stukit.codebench.ui.controller;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.domain.TestResultRow;
import com.stukit.codebench.infrastructure.compiler.CompileException;
import com.stukit.codebench.infrastructure.compiler.JavaCompilerService;
import com.stukit.codebench.infrastructure.fs.WorkspaceFactory;
import com.stukit.codebench.infrastructure.parser.DefaultOutputParser;
import com.stukit.codebench.infrastructure.parser.OutputParser;
import com.stukit.codebench.infrastructure.runner.JavaRunner;
import com.stukit.codebench.service.FileIOService;
import com.stukit.codebench.service.JudgeService;
import com.stukit.codebench.service.TestCaseImportService;
import com.stukit.codebench.ui.component.JavaCodeEditor;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MainController {

    /* ===================== FXML ===================== */
    @FXML private Button btnImportTest;
    @FXML private Button btnImportCode;
    @FXML private Button btnAddTest;
    @FXML private Button btnRun;
    @FXML private Button btnToggleTheme;
    @FXML private BorderPane mainContainer;

    @FXML private TextField txtTimeLimit;
    @FXML private Label lblScore;
    @FXML private Label lblStatus;
    @FXML private Label lblVersion;
    @FXML private ProgressBar progressBar;

    @FXML private StackPane editorContainer;

    @FXML private TableView<TestResultRow> tblResults;
    @FXML private TableColumn<TestResultRow, String> colStatus;
    @FXML private TableColumn<TestResultRow, String> colName;
    @FXML private TableColumn<TestResultRow, String> colRuntime;

    /* ===================== STATE ===================== */
    private final ObservableList<TestResultRow> tblResultData = FXCollections.observableArrayList();

    private WorkspaceFactory workspaceFactory;
    private JavaCompilerService compilerService;
    private JavaRunner runner;
    private OutputParser parser;

    private FileIOService fileService;
    private JudgeService.JudgeSession currentSession;

    private JavaCodeEditor codeEditor;
    private boolean isDarkMode = false;

    String currentPath = System.getProperty("user.dir");
    private final Path WORKSPACE_ROOT = Path.of(currentPath, "temp-workspaces");

    private static final String CSS_LIGHT = "/com/stukit/codebench/css/theme-light.css";
    private static final String CSS_DARK  = "/com/stukit/codebench/css/theme-dark.css";
    private static final String VIEW_MANUAL_TEST = "/com/stukit/codebench/fxml/manual-test-view.fxml";

    /* ===================== INIT ===================== */
    @FXML
    public void initialize() {
        lblVersion.setText("v2.0.0");

        fileService = new FileIOService();
        workspaceFactory = new WorkspaceFactory(WORKSPACE_ROOT);
        compilerService = new JavaCompilerService();
        runner = new JavaRunner();
        parser = new DefaultOutputParser();

        setupEditor();
        setupTable();
        setupDefaultState();
        setupActions();
    }

    private void setupEditor() {
        codeEditor = new JavaCodeEditor();

        // Load CSS an toàn (tránh lỗi NullPointer nếu file chưa build kịp)
        URL cssResource = getClass().getResource(CSS_LIGHT);
        if (cssResource != null) {
            codeEditor.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("Warning: Không tìm thấy CSS tại " + CSS_LIGHT);
        }

        VirtualizedScrollPane<JavaCodeEditor> vsPane = new VirtualizedScrollPane<>(codeEditor);
        editorContainer.getChildren().clear();
        editorContainer.getChildren().add(vsPane);
    }

    private void setupTable() {
        tblResults.setItems(tblResultData);

        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colRuntime.setCellValueFactory(cellData -> cellData.getValue().runtimeProperty());

        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colStatus.setCellFactory(column -> new TableCell<TestResultRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("status-ac", "status-wa", "status-tle", "status-running", "status-ce");

                if (item != null && !empty) {
                    switch (item) {
                        case "AC" -> getStyleClass().add("status-ac");
                        case "WA" -> getStyleClass().add("status-wa");
                        case "TLE" -> getStyleClass().add("status-tle");
                        case "CE" -> getStyleClass().add("status-ce");
                        case "Running..." -> getStyleClass().add("status-running");
                    }
                }
            }
        });

        // Context Menu để xem chi tiết
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewItem = new MenuItem("Xem chi tiết Input/Output");
        viewItem.setOnAction(actionEvent -> {
            TestResultRow selectedRow = tblResults.getSelectionModel().getSelectedItem();
            if (selectedRow != null) {
                onOpenTestDetail(selectedRow);
            }
        });
        contextMenu.getItems().add(viewItem);
        tblResults.setContextMenu(contextMenu);
    }

    private void setupDefaultState() {
        lblStatus.setText("Sẵn sàng.");
        lblScore.setText("0 / 0");
        progressBar.setProgress(0);
    }

    private void setupActions() {
        btnRun.setOnAction(e -> onRunClicked());
        btnImportTest.setOnAction(e -> onImportTest());
        btnImportCode.setOnAction(e -> onImportCode());
        btnAddTest.setOnAction(e -> onAddTestManually());
        btnToggleTheme.setOnAction(e -> onToggleTheme());
    }

    /* ===================== ACTIONS ===================== */

    private void onRunClicked() {
        // Dọn dẹp session cũ
        if (currentSession != null) {
            currentSession.cleanupWorkspace();
            currentSession = null;
        }

        String sourceCode = codeEditor.getText();
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            lblStatus.setText("Lỗi: Chưa nhập source code");
            return;
        }

        if (tblResultData.isEmpty()) {
            lblStatus.setText("Chưa có testcase nào!");
            return;
        }

        long timeLimit;
        try {
            timeLimit = Long.parseLong(txtTimeLimit.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("Time limit không hợp lệ");
            return;
        }

        lblStatus.setText("Đang khởi tạo môi trường chấm...");
        JudgeService judgeService = new JudgeService(workspaceFactory, compilerService, runner, parser);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                currentSession = judgeService.createSession(sourceCode, timeLimit);

                Platform.runLater(() -> {
                    for (TestResultRow row : tblResultData) {
                        row.reset();
                    }
                    lblStatus.setText("Đang biên dịch code...");
                });

                try {
                    // 1. Compile
                    try {
                        currentSession.compile();
                    } catch (CompileException e) {
                        Platform.runLater(() -> {
                            for (TestResultRow row : tblResultData) {
                                row.updateResult("CE", 0, null);
                            }
                            lblStatus.setText("Lỗi biên dịch: " + e.getMessage());
                        });
                        return null;
                    }

                    // 2. Run Testcases
                    int total = tblResultData.size();
                    int current = 0;

                    for (TestResultRow row : tblResultData) {
                        current++;
                        final int index = current;

                        Platform.runLater(() -> {
                            row.updateStatus("Running...");
                            lblStatus.setText(String.format("Đang chấm testcase %d/%d: %s", index, total, row.nameProperty().get()));
                        });

                        TestCase testCase = row.getTestCase();
                        JudgeResult result = currentSession.runTestCase(testCase);

                        Platform.runLater(() -> {
                            row.updateResult(
                                    result.getStatus().toString(),
                                    result.getRunTimeMs(),
                                    result.getActualOutputPath()
                            );
                            updateScoreUI();
                        });
                    }

                    Platform.runLater(() -> lblStatus.setText("Đã chấm xong toàn bộ testcase."));

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> lblStatus.setText("Lỗi hệ thống: " + e.getMessage()));
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void shutdown() {
        if (currentSession != null) {
            currentSession.cleanupWorkspace();
        }
        // Dọn dẹp folder manual
        Path manualRoot = Path.of("./temp-workspaces/manual");
        if (Files.exists(manualRoot)) {
            try (Stream<Path> walk = Files.walk(manualRoot)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                System.err.println("Warning: Failed to clean manual tests: " + e.getMessage());
            }
        }
    }

    private void onImportTest() {
        TestCaseImportService importService = new TestCaseImportService();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Chọn thư mục chứa Testcase");

        File selectedDirectory = directoryChooser.showDialog(tblResults.getScene().getWindow());

        if (selectedDirectory != null) {
            List<TestCase> loaded = importService.loadTestCasesFromFolder(selectedDirectory);
            tblResultData.clear();
            for (TestCase tc : loaded) {
                tblResultData.add(new TestResultRow(tc));
            }
            lblStatus.setText("Đã nạp: " + loaded.size() + " testcase từ thư mục " + selectedDirectory.getName());
            updateScoreUI();
        }
    }

    private void onImportCode() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Java");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));
        File selectedFile = fileChooser.showOpenDialog(btnImportCode.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                codeEditor.replaceText(content);
                lblStatus.setText("Đã nạp source code: " + selectedFile.getName());
            } catch (IOException e) {
                lblStatus.setText("Lỗi: " + e.getMessage());
            }
        }
    }

    private void onAddTestManually() {
        try {
            URL fxmlUrl = getClass().getResource(VIEW_MANUAL_TEST);
            if (fxmlUrl == null) {
                lblStatus.setText("Lỗi: Không tìm thấy file " + VIEW_MANUAL_TEST);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            DialogPane dialogPane = loader.load();

            ManualTestController controller = loader.getController();
            controller.setFileService(this.fileService);
            controller.setDefaultName(tblResultData.size() + 1);

            Dialog<TestCase> dialog = new Dialog<>();
            dialog.setTitle("Thêm Testcase Thủ Công");
            dialog.setDialogPane(dialogPane);

            if (tblResults.getScene() != null) {
                dialog.initOwner(tblResults.getScene().getWindow());
            }

            ButtonType btnTypeAdd = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnTypeAdd, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == btnTypeAdd) {
                    return controller.createTestCase();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(testCase -> {
                TestResultRow newRow = new TestResultRow(testCase);
                tblResultData.add(newRow);
                updateScoreUI();
                lblStatus.setText("Đã thêm test thủ công: " + testCase.getName());
            });

        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi mở dialog: " + e.getMessage());
        }
    }

    private void onToggleTheme() {
        isDarkMode = !isDarkMode;
        Scene scene = btnToggleTheme.getScene();
        if (scene == null) return;

        URL lightUrl = getClass().getResource(CSS_LIGHT);
        URL darkUrl = getClass().getResource(CSS_DARK);

        // Nếu không tìm thấy file CSS thì thoát luôn, tránh lỗi
        if (lightUrl == null || darkUrl == null) {
            System.err.println("Lỗi: Không tìm thấy file CSS (Light/Dark)");
            return;
        }

        String themeDarkPath = darkUrl.toExternalForm();
        String themeLightPath = lightUrl.toExternalForm();

        if (isDarkMode) {
            scene.getStylesheets().remove(themeLightPath);
            if (!scene.getStylesheets().contains(themeDarkPath)) scene.getStylesheets().add(themeDarkPath);

            codeEditor.getStylesheets().remove(themeLightPath);
            if (!codeEditor.getStylesheets().contains(themeDarkPath)) codeEditor.getStylesheets().add(themeDarkPath);

            btnToggleTheme.setText("☀ Light Mode");
            btnToggleTheme.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black;");
        } else {
            scene.getStylesheets().remove(themeDarkPath);
            if (!scene.getStylesheets().contains(themeLightPath)) scene.getStylesheets().add(themeLightPath);

            codeEditor.getStylesheets().remove(themeDarkPath);
            if (!codeEditor.getStylesheets().contains(themeLightPath)) codeEditor.getStylesheets().add(themeLightPath);

            btnToggleTheme.setText("🌙 Dark Mode");
            btnToggleTheme.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        }
    }

    private void onOpenTestDetail(TestResultRow row) {
        if (row == null) return;

        // Lazy loading nội dung file
        String expectedContent = fileService.readPreview(row.getExpectedOutputPath());
        String actualContent = fileService.readPreview(row.getActualOutputPath());

        showDetailDialog(row.nameProperty().get(), expectedContent, actualContent);
    }

    private void showDetailDialog(String title, String expected, String actual) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết: " + title);
        alert.setHeaderText("So sánh kết quả");
        alert.getDialogPane().setMinWidth(800);
        alert.getDialogPane().setMinHeight(500);

        TextArea txtExpected = new TextArea(expected);
        txtExpected.setEditable(false);
        txtExpected.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");

        TextArea txtActual = new TextArea(actual);
        txtActual.setEditable(false);
        txtActual.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Expected:"), 0, 0);
        grid.add(txtExpected, 0, 1);
        GridPane.setHgrow(txtExpected, Priority.ALWAYS);
        GridPane.setVgrow(txtExpected, Priority.ALWAYS);

        grid.add(new Label("Actual:"), 1, 0);
        grid.add(txtActual, 1, 1);
        GridPane.setHgrow(txtActual, Priority.ALWAYS);
        GridPane.setVgrow(txtActual, Priority.ALWAYS);

        alert.getDialogPane().setContent(grid);

        // Thêm CSS cho Dialog để đồng bộ theme
        if (tblResults.getScene() != null) {
            alert.getDialogPane().getStylesheets().addAll(tblResults.getScene().getStylesheets());
        }

        alert.showAndWait();
    }

    private void updateScoreUI() {
        long passed = tblResultData.stream()
                .filter(r -> "AC".equals(r.statusProperty().get()))
                .count();
        lblScore.setText(passed + " / " + tblResultData.size());
        progressBar.setProgress(tblResultData.isEmpty() ? 0 : (double) passed / tblResultData.size());
    }
}