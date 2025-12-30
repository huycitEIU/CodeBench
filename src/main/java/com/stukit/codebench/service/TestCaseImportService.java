package com.stukit.codebench.service;

import com.stukit.codebench.domain.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TestCaseImportService {

    public List<TestCase> loadTestCasesFromFolder(File folder) {
        List<TestCase> testCases = new ArrayList<>();

        if (folder == null || !folder.isDirectory()) return testCases;

        File[] inputFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".in"));

        if (inputFiles == null) return testCases;

        // --- SỬA LOGIC SORT ---
        // Sắp xếp theo "Natural Order" (Số học) thay vì Alphabet
        Arrays.sort(inputFiles, (f1, f2) -> {
            String name1 = getFileNameWithoutExtension(f1.getName());
            String name2 = getFileNameWithoutExtension(f2.getName());

            try {
                // Thử ép kiểu sang số nguyên để so sánh
                int n1 = Integer.parseInt(name1);
                int n2 = Integer.parseInt(name2);
                return Integer.compare(n1, n2);
            } catch (NumberFormatException e) {
                // Nếu tên file không phải là số (ví dụ: testA.in), so sánh chuỗi bình thường
                return name1.compareToIgnoreCase(name2);
            }
        });

        for (File inputFile : inputFiles) {
            String inputName = inputFile.getName();
            // Lấy tên file .out tương ứng
            String outputName = inputName.substring(0, inputName.lastIndexOf('.')) + ".out";
            File outputFile = new File(folder, outputName);

            String inputContent = readFileContent(inputFile);
            String expectedOutput;

            if (outputFile.exists()) {
                expectedOutput = readFileContent(outputFile);
            } else {
                expectedOutput = "Không tìm thấy file .out tương ứng.";
            }

            TestCase testCase = new TestCase(inputName, inputContent, expectedOutput);
            testCases.add(testCase);
        }
        return testCases;
    }

    // Helper: Lấy tên file bỏ đuôi mở rộng
    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    private String readFileContent(File file) {
        try {
            // Java 11+ hỗ trợ readString, mặc định UTF-8
            return Files.readString(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return "Lỗi khi đọc file: " + e.getMessage();
        }
    }
}
