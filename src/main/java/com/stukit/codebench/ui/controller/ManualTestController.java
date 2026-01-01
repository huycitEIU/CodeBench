package com.stukit.codebench.ui.controller;

import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.service.FileIOService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ManualTestController {

    @FXML private TextField txtName;
    @FXML private TextArea txtInput;
    @FXML private TextArea txtOutput;

    private FileIOService fileService;

    public void setFileService(FileIOService fileService) {
        this.fileService = fileService;
    }

    public void setDefaultName(int index) {
        txtName.setText("Manual Test " + index);
    }

    public TestCase createTestCase() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) name = "Manual Test";

        String inputContent = txtInput.getText();
        String expectedContent = txtOutput.getText();

        try {
            // --- THAY ĐỔI TẠI ĐÂY ---
            // 1. Lấy thư mục gốc giống với MainController ("./temp-workspaces")
            Path rootWorkspace = Path.of("./temp-workspaces");

            // 2. Tạo thư mục con "manual"
            Path manualDir = rootWorkspace.resolve("manual")
                    .resolve(String.valueOf(System.currentTimeMillis()));

            Path inputPath = manualDir.resolve(name + "_input.txt");
            Path expectedPath = manualDir.resolve(name + "_expected.txt");

            // Ghi file
            if (fileService != null) {
                fileService.writeString(inputPath, inputContent);
                fileService.writeString(expectedPath, expectedContent);
            } else {
                Files.writeString(inputPath, inputContent);
                Files.writeString(expectedPath, expectedContent);
            }

            return new TestCase(name, inputPath, expectedPath);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}