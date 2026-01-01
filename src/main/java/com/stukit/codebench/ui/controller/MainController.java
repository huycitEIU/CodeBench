package com.stukit.codebench.ui.controller;

import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.domain.TestResultRow;
import com.stukit.codebench.infrastructure.compiler.JavaCompilerService;
import com.stukit.codebench.infrastructure.fs.WorkspaceFactory;
import com.stukit.codebench.infrastructure.parser.DefaultOutputParser;
import com.stukit.codebench.infrastructure.runner.JavaRunner;
import com.stukit.codebench.service.BatchJudgeTask;
import com.stukit.codebench.service.FileIOService;
import com.stukit.codebench.service.JudgeService;
import com.stukit.codebench.service.TestCaseImportService;
import com.stukit.codebench.ui.component.JavaCodeEditor;
import com.stukit.codebench.ui.component.StatusCell;
import com.stukit.codebench.ui.helper.ViewHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MainController {

    /* ===================== FXML ===================== */
    @FXML private Button btnImportTest, btnImportCode, btnAddTest, btnRun, btnToggleTheme;
    @FXML private BorderPane mainContainer;
    @FXML private TextField txtTimeLimit, txtMemoryLimit; // Thêm field Memory Limit
    @FXML private Label lblScore, lblStatus, lblVersion;
    @FXML private ProgressBar progressBar;
    @FXML private StackPane editorContainer;
    @FXML private TableView<TestResultRow> tblResults;
    @FXML private TableColumn<TestResultRow, String> colStatus, colName, colRuntime, colMemory;
    @FXML private MenuItem menuAbout, menuExit, menuDocs, menuImportTest, menuImportCode, menuTheme;
    @FXML private MenuItem menuRun;

    /* ===================== SERVICES & STATE ===================== */
    private final ObservableList<TestResultRow> tblResultData = FXCollections.observableArrayList();
    private JudgeService judgeService;
    private FileIOService fileService;
    private JavaCodeEditor codeEditor;
    private JudgeService.JudgeSession currentSession;

    private boolean isDarkMode = false;
    private static final String CSS_LIGHT = "/com/stukit/codebench/css/theme-light.css";
    private static final String CSS_DARK  = "/com/stukit/codebench/css/theme-dark.css";
    private static final String VIEW_MANUAL_TEST = "/com/stukit/codebench/fxml/manual-test-view.fxml";
    private final Path WORKSPACE_ROOT = com.stukit.codebench.App.APP_TEMP_ROOT;

    private ViewHelper viewHelper = new ViewHelper();

    @FXML
    public void initialize() {
        lblVersion.setText("v2.0.0");
        initServices();
        initEditor();
        initTable();
        initActions();
        initShortcut();
        resetStatus();
    }

    private void initServices() {
        WorkspaceFactory wsFactory = new WorkspaceFactory(WORKSPACE_ROOT);

        // Khởi tạo JudgeService với các component con
        this.judgeService = new JudgeService(
                wsFactory,
                new JavaCompilerService(),
                new JavaRunner(),
                new DefaultOutputParser()
        );
        this.fileService = new FileIOService();
    }

    private void initEditor() {
        codeEditor = new JavaCodeEditor();
        // Load CSS mặc định cho Editor
        URL css = getClass().getResource(CSS_LIGHT);
        if (css != null) codeEditor.getStylesheets().add(css.toExternalForm());

        editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeEditor));
    }

    private void initTable() {
        tblResults.setItems(tblResultData);
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colRuntime.setCellValueFactory(cell -> cell.getValue().runtimeProperty());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
        colMemory.setCellValueFactory(cell -> cell.getValue().memoryProperty());

        // Sử dụng Custom Cell Factory đã tách ra
        colStatus.setCellFactory(column -> new StatusCell());

        // Context Menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewItem = new MenuItem("Xem chi tiết Input/Output");
        viewItem.setOnAction(e -> onOpenTestDetail(tblResults.getSelectionModel().getSelectedItem()));
        contextMenu.getItems().add(viewItem);
        tblResults.setContextMenu(contextMenu);
    }

    private void initActions() {
        btnRun.setOnAction(e -> onRunCode());
        btnImportTest.setOnAction(e -> onImportTest());
        btnImportCode.setOnAction(e -> onImportCode());
        btnAddTest.setOnAction(e -> onAddTestManually());
        btnToggleTheme.setOnAction(e -> onToggleTheme());
        menuAbout.setOnAction(e -> onOpenAbout());
        menuDocs.setOnAction(e -> onOpenDocs());
        menuExit.setOnAction(e -> onCloseApp());
        menuImportCode.setOnAction(e -> onImportCode());
        menuImportTest.setOnAction(e -> onImportTest());
        menuTheme.setOnAction(e -> onToggleTheme());
        menuRun.setOnAction(e -> onRunCode());
    }

    private void initShortcut() {
        setShortcut(menuImportCode, "Shortcut+O");
        setShortcut(menuImportTest, "Shortcut+T");
        setShortcut(menuRun, "F5");
    }

    /* ===================== LOGIC XỬ LÝ ===================== */

    private void onRunCode() {
        // Validation
        String sourceCode = codeEditor.getText();
        if (sourceCode == null || sourceCode.isBlank()) {
            lblStatus.setText("Lỗi: Chưa nhập source code.");
            return;
        }
        if (tblResultData.isEmpty()) {
            lblStatus.setText("Chưa có test case nào.");
            return;
        }

        long timeLimit = parseLongSafe(txtTimeLimit.getText(), 1000);
        long memoryLimit = parseLongSafe(txtMemoryLimit.getText(), 128 * 1024 * 1024); // Mặc định 128MB nếu không có UI field, hoặc lấy từ txtMemoryLimit

        // Dọn dẹp session cũ
        if (currentSession != null) currentSession.close();

        // Tạo Session mới
        currentSession = judgeService.createSession(sourceCode, timeLimit, memoryLimit);

        // Tạo và chạy Task (Sử dụng class BatchJudgeTask đã tách)
        BatchJudgeTask task = new BatchJudgeTask(
                currentSession,
                tblResultData,
                status -> Platform.runLater(() -> lblStatus.setText(status)), // Callback update status
                this::updateScoreUI // Callback update score
        );

        new Thread(task).start();
    }

    private void onImportTest() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Chọn thư mục testcase");
        File dir = dc.showDialog(mainContainer.getScene().getWindow());
        if (dir != null) {
            List<TestCase> cases = new TestCaseImportService().loadTestCasesFromFolder(dir);
            tblResultData.clear();
            cases.forEach(tc -> tblResultData.add(new TestResultRow(tc)));
            updateScoreUI();
            lblStatus.setText("Đã nạp " + cases.size() + " testcases.");
        }
    }

    private void onImportCode() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));
        File file = fc.showOpenDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            try {
                codeEditor.replaceText(Files.readString(file.toPath()));
                lblStatus.setText("Đã nạp file: " + file.getName());
            } catch (IOException e) {
                viewHelper.showError("Lỗi đọc file", e.getMessage());
            }
        }
    }

    private void onAddTestManually() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEW_MANUAL_TEST));
            DialogPane pane = loader.load();

            ManualTestController ctrl = loader.getController();
            ctrl.setFileService(fileService);
            ctrl.setDefaultName(tblResultData.size() + 1);

            Dialog<TestCase> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Thêm Test Thủ Công");
            dialog.initOwner(mainContainer.getScene().getWindow());

            ButtonType btnAdd = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
            pane.getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

            dialog.setResultConverter(bt -> bt == btnAdd ? ctrl.createTestCase() : null);

            dialog.showAndWait().ifPresent(tc -> {
                tblResultData.add(new TestResultRow(tc));
                updateScoreUI();
            });
        } catch (IOException e) {
            viewHelper.showError("Lỗi UI", "Không thể mở form thêm test: " + e.getMessage());
        }
    }

    private void onOpenTestDetail(TestResultRow row) {
        if (row == null) return;
        // Lazy loading file content
        String expected = fileService.readPreview(row.getExpectedOutputPath());
        String actual = fileService.readPreview(row.getActualOutputPath());

        String css = isDarkMode ? getClass().getResource(CSS_DARK).toExternalForm() : null;
        viewHelper.showDiffDialog(mainContainer.getScene().getWindow(), row.nameProperty().get(), expected, actual, css);
    }

    private void onToggleTheme() {
        isDarkMode = !isDarkMode;
        String cssRemove = isDarkMode ? CSS_LIGHT : CSS_DARK;
        String cssAdd = isDarkMode ? CSS_DARK : CSS_LIGHT;

        updateStyleSheet(mainContainer.getScene(), cssRemove, cssAdd);
        updateStyleSheet(codeEditor, cssRemove, cssAdd);

        btnToggleTheme.setText(isDarkMode ? "☀" : "🌙");
    }

    private void onOpenAbout() {
        viewHelper.showAboutDialog(getCurrentCSS());
    }

    private void onOpenDocs() {
        viewHelper.showDocument(getCurrentCSS());
    }
    private void onCloseApp() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }

    // Helper xử lý CSS cho cả Scene và CodeArea
    private void updateStyleSheet(Object target, String removePath, String addPath) {
        ObservableList<String> sheets;
        if (target instanceof Scene s) sheets = s.getStylesheets();
        else if (target instanceof JavaCodeEditor e) sheets = e.getStylesheets();
        else return;

        URL urlRemove = getClass().getResource(removePath);
        URL urlAdd = getClass().getResource(addPath);

        if (urlRemove != null) sheets.remove(urlRemove.toExternalForm());
        if (urlAdd != null && !sheets.contains(urlAdd.toExternalForm())) {
            sheets.add(urlAdd.toExternalForm());
        }
    }

    private String getCurrentCSS() {
        return isDarkMode ? getClass().getResource(CSS_DARK).toExternalForm() :
                getClass().getResource(CSS_LIGHT).toExternalForm();
    }

    private void updateScoreUI() {
        long passed = tblResultData.stream().filter(r -> "AC".equals(r.statusProperty().get())).count();
        lblScore.setText(passed + " / " + tblResultData.size());
        progressBar.setProgress(tblResultData.isEmpty() ? 0 : (double) passed / tblResultData.size());
    }

    private void resetStatus() {
        lblStatus.setText("Sẵn sàng.");
        lblScore.setText("0 / 0");
        progressBar.setProgress(0);
    }

    public void shutdown() {
        if (currentSession != null) currentSession.close();
        fileService.deletePath(Path.of("temp-workspaces/manual"));
    }

    private long parseLongSafe(String text, long defaultVal) {
        try { return Long.parseLong(text.trim()); } catch (Exception e) { return defaultVal; }
    }

    private void setShortcut(MenuItem menuItem, String shortcut) {
        menuItem.setAccelerator(KeyCombination.keyCombination(shortcut));
    }
}