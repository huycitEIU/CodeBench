package com.stukit.codebench.ui.controller;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.TestCase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class TestCaseDetailController {
    @FXML
    private Label lblTitle;
    @FXML private TextArea txtInput;
    @FXML private TextArea txtExpected;
    @FXML private TextArea txtActual;
    @FXML private TextArea txtError;

    // Hàm này sẽ được MainController gọi để truyền dữ liệu vào
    public void setTestCaseData(TestCase testCase, JudgeResult judgeResult) {
        return;
    }
}
