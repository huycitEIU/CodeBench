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
import com.stukit.codebench.service.JudgeService;
import com.stukit.codebench.service.TestCaseImportService;
import com.stukit.codebench.ui.component.JavaCodeEditor; // <--- IMPORT MỚI

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser; // <--- IMPORT MỚI
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane; // <--- IMPORT MỚI

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MainController {

    /* ===================== FXML ===================== */
    @FXML private Button btnImportTest;
    @FXML private Button btnImportCode;
    @FXML private Button btnAddTest;
    @FXML private Button btnRun;
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
    private final ObservableList<TestResultRow> tblResultData
            = FXCollections.observableArrayList();
    private List<TestCase> testCases;
    private WorkspaceFactory workspaceFactory;
    private JavaCompilerService compilerService;
    private JavaRunner runner;
    private OutputParser parser;

    // THÊM BIẾN NÀY ĐỂ QUẢN LÝ EDITOR
    private JavaCodeEditor codeEditor;

    /* ===================== INIT ===================== */
    @FXML
    public void initialize() {
        workspaceFactory = new WorkspaceFactory(Path.of("./temp-workspaces"));
        compilerService = new JavaCompilerService();
        runner = new JavaRunner();
        parser = new DefaultOutputParser();

        setupTable();
        setupDefaultState();
        setupActions();

        // GỌI HÀM SETUP EDITOR
        setupEditor();
    }

    private void setupEditor() {
        // 1. Khởi tạo Editor
        codeEditor = new JavaCodeEditor();

        // 2. Load CSS (Sử dụng đường dẫn an toàn)
        try {
            String cssPath = getClass().getResource("/com/stukit/codebench/css/java-keywords.css").toExternalForm();
            codeEditor.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Không tìm thấy file CSS cho editor! Kiểm tra lại thư mục resources.");
        }

        // 3. Bọc trong ScrollPane ảo hoá (quan trọng cho RichTextFX)
        VirtualizedScrollPane<JavaCodeEditor> vsPane = new VirtualizedScrollPane<>(codeEditor);

        // 4. Gắn vào giao diện (thay thế Label cũ)
        editorContainer.getChildren().clear();
        editorContainer.getChildren().add(vsPane);
    }

    private void setupTable() {

        tblResults.setItems(tblResultData);
        colStatus.setCellValueFactory(cellData -> {
            return cellData.getValue().statusProperty();
        });
        colStatus.setCellFactory(column -> new TableCell<TestResultRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);

                // Xóa style cũ
                getStyleClass().removeAll("status-ac", "status-wa", "status-tle", "status-running");

                if (item != null && !empty) {
                    if (item.equals("AC")) getStyleClass().add("status-ac");
                    if (item.equals("WA")) getStyleClass().add("status-wa");
                    if (item.equals("TLE")) getStyleClass().add("status-tle");
                    if (item.equals("Running...")) getStyleClass().add("status-running");
                }
            }
        });
        colName.setCellValueFactory(cellData -> {
            return cellData.getValue().testNameProperty();
        });
        colRuntime.setCellValueFactory(cellData -> {
            return cellData.getValue().timeProperty();
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewItem = new MenuItem("Xem chi tiết");

        viewItem.setOnAction(actionEvent -> {
            TestResultRow testResultRow = tblResults.getSelectionModel().getSelectedItem();
            if (testResultRow != null && testCases != null) {
                testCases.stream()
                        .filter(tc -> tc.getName().equals(testResultRow.testNameProperty()))
                        .findFirst()
                        .ifPresent(tc -> onOpenTestDetail());
            }
        });

        contextMenu.getItems().add(viewItem);
        tblResults.setContextMenu(contextMenu);
    }

    private void setupDefaultState() {
        lblStatus.setText("Ready");
        lblScore.setText("0 / 0");
        progressBar.setProgress(0);
    }

    private void setupActions() {
        btnRun.setOnAction(e -> onRunClicked());
        btnImportTest.setOnAction(e -> onImportTest());
        btnImportCode.setOnAction(e -> onImportCode());
        btnAddTest.setOnAction(e -> onAddTestManually());
    }

    /* ===================== ACTIONS ===================== */

    private void onRunClicked() {

        String sourceCode = codeEditor.getText();

        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            lblStatus.setText("Lỗi: Chưa nhập source code");
            return;
        }
        JudgeService judgeService = new JudgeService(
                workspaceFactory,
                compilerService,
                runner,
                parser
        );
        if (testCases == null || testCases.isEmpty()) {
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

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (JudgeService.JudgeSession session = judgeService.createSession(sourceCode, timeLimit)) {

                    // 1. Biên dịch trước
                    try {
                        session.compile();
                    } catch (CompileException e) {
                        // Nếu lỗi biên dịch -> Update TOÀN BỘ bảng thành Compile Error và return luôn
                        Platform.runLater(() -> {
                            for (TestResultRow row : tblResultData) {
                                row.setStatus("CE");
                                row.setMessage(e.getMessage());
                            }
                        });
                        return null; // Dừng task
                    }

                    // 2. Chạy từng test case (Chỉ chạy khi compile thành công)
                    for (TestResultRow row : tblResultData) {

                        // Update UI: Running
                        Platform.runLater(() -> row.setStatus("Running..."));

                        // Chạy chấm (Blocking operation - tốn thời gian)
                        JudgeResult result = session.runTestCase(row.getTestCase());

                        // Update UI: Kết quả (AC/WA/TLE)
                        Platform.runLater(() -> {
                            row.setStatus(result.getStatus().toString()); // Ví dụ: PASSED -> AC
                            row.setTime(result.getRunTimeMs() + "ms");
                            // ... set các info khác
                        });

                        // Nghỉ 1 xíu để hiệu ứng mượt hơn (tuỳ chọn)
                        // Thread.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Ra khỏi khối try, session.close() tự kích hoạt -> Xoá workspace
                return null;
            }
        };

        new Thread(task).start();

        lblStatus.setText("Hoàn tất");
    }

    private void onImportTest() {
        TestCaseImportService importService = new TestCaseImportService();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Chọn thư mục chứa Testcase");

        File selectedDirectory = directoryChooser.showDialog(btnImportTest.getScene().getWindow());

        if (selectedDirectory != null) {
            List<TestCase> loaded = importService.loadTestCasesFromFolder(selectedDirectory);
            tblResultData.clear();
            for (TestCase tc: loaded) {
                tblResultData.add(new TestResultRow(tc));
            }
            setTestCases(loaded);
            lblStatus.setText("Đã nạp: " + loaded.size() + " testcase");
        }
    }

    private void onImportCode() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Java");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        File selectedFile = fileChooser.showOpenDialog(btnImportCode.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                // Set nội dung vào Editor
                codeEditor.replaceText(content);
                lblStatus.setText("Đã load code từ: " + selectedFile.getName());
            } catch (IOException e) {
                lblStatus.setText("Lỗi đọc file code");
                e.printStackTrace();
            }
        }
    }

    private void onAddTestManually() {
        lblStatus.setText("Tính năng đang phát triển");
    }

    private void onOpenTestDetail() {
        lblStatus.setText("Tính năng đang phát triển");
    }

    /* ===================== UI HELPERS ===================== */
    private void updateSummary(List<JudgeResult> results) {
        long passed = results.stream().filter(JudgeResult::isPassed).count();
        lblScore.setText(passed + " / " + results.size());
        progressBar.setProgress(results.isEmpty() ? 0 : (double) passed / results.size());
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}