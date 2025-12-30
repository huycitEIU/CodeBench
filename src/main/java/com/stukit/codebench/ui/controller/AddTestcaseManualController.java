package com.stukit.codebench.ui.controller;

import com.stukit.codebench.domain.TestCase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddTestcaseManualController {
    @FXML
    private TextField txtName;
    @FXML private TextArea txtInput;
    @FXML private TextArea txtExpected;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private TestCase createdTestCase = null;

    @FXML
    public void initialize() {
        btnSave.setOnAction(actionEvent -> handleSave());
        btnCancel.setOnAction(actionEvent -> closeWindow());
    }

    private void handleSave() {
        String name = txtName.getText().trim();
        String input = txtInput.getText();
        String expected = txtExpected.getText().trim();

        if (name.isEmpty()) {
            showAlert("Vui lòng nhập tên Testcase!");
            return;
        }
        createdTestCase = new TestCase(name, input, expected);
        closeWindow();
    }
    private void closeWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(content);
        alert.show();
    }
    public TestCase getCreatedTestCase() {
        return createdTestCase;
    }
}
