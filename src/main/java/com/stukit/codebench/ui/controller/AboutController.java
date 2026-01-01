package com.stukit.codebench.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.awt.*;

public class AboutController {
    @FXML private Button btnClose;

    @FXML
    public void initialize() {
        intAction();
    }

    private void intAction() {
        btnClose.setOnAction(e -> onClose());
    }

    private void onClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
