package com.stukit.codebench.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class DocumentController {
    @FXML private ListView<String> listTopics;
    @FXML private Label lblContentTitle;
    @FXML private Label lblContentBody;
    @FXML private Button btnClose;

    // Dữ liệu mẫu (Tiêu đề -> Nội dung)
    // Trong thực tế, nên dùng Map<String, String> hoặc Class riêng
    private final String[] TOPICS = {
            "1. Giới thiệu chung",
            "2. Cách Import Testcase",
            "3. Viết và chạy Code",
            "4. Cấu hình Time/Memory",
            "5. Phím tắt"
    };

    private final String[] CONTENTS = {
            "Chào mừng bạn đến với CodeBench!\n\nĐây là công cụ hỗ trợ chấm bài tự động",
            "Để thêm testcase:\n1. Nhấn nút 'Import Testcase'.\n2. Chọn folder chứa testcase",
            "Bạn có thể viết code trực tiếp hoặc mở file có sẵn",
            "Giới hạn thời gian (Time Limit) mặc định là 1000ms",
            "Ctrl+O: Import file code\nCtrl+T: Thêm thư mục chứa testcase\nF5: Chạy test"
    };

    private ObservableList<String> items;

    @FXML
    public void initialize() {
        initNavigation();
        initAction();


    }

    private void initNavigation() {
        items = FXCollections.observableArrayList(TOPICS);
        listTopics.setItems(items);

        listTopics.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int index = newValue.intValue();
                if (index >= 0 && index < TOPICS.length) {
                    onSelectTopic(index);
                }
            }
        });
    }

    private void initAction() {
        btnClose.setOnAction(e -> onClose());
    }


    // Helper funtions
    private void onClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private void onSelectTopic(int index) {
        lblContentTitle.setText(TOPICS[index]);
        lblContentBody.setText(CONTENTS[index]);
    }
}
