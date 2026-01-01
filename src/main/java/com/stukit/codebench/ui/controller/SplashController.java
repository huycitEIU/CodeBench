package com.stukit.codebench.ui.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class SplashController {

    @FXML private Label lblLoading;

    // Phương thức để cập nhật tiến trình từ bên ngoài
    public void updateProgress(String message, double progress) {
        lblLoading.setText(message);
    }
    public StringProperty loadingMessageProperty() {
        return lblLoading.textProperty();
    }
}