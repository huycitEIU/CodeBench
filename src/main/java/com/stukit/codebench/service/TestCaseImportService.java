package com.stukit.codebench.service;

import com.stukit.codebench.domain.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCaseImportService {

    /**
     * Quét thư mục để tìm cặp file .in / .out
     * @param folder thư mục chứa test case
     * @return danh sách TestCase đã được sắp xếp tự nhiên (1, 2, 10...)
     */
    public List<TestCase> loadTestCasesFromFolder(File folder) {
        List<TestCase> testCases = new ArrayList<>();
        if (folder == null || !folder.isDirectory()) return testCases;

        // Lấy tất cả file .in
        File[] inputFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".in"));
        if (inputFiles == null) return testCases;

        // Sắp xếp tự nhiên (Natural Sort)
        Arrays.sort(inputFiles, this::compareNatural);

        for (File inputFile : inputFiles) {
            String inputName = inputFile.getName();
            // Tìm file output tương ứng (thay .in bằng .out)
            String baseName = inputName.substring(0, inputName.lastIndexOf('.'));
            File outputFile = new File(folder, baseName + ".out");

            if (outputFile.exists()) {
                // Record TestCase tự động có constructor ngắn gọn
                testCases.add(new TestCase(baseName, inputFile.toPath(), outputFile.toPath()));
            } else {
                System.err.println("[Warning] Missing output file for: " + inputName);
            }
        }
        return testCases;
    }

    /**
     * So sánh tên file theo kiểu số học (Natural Order).
     * vd: "test2" < "test10"
     */
    private int compareNatural(File f1, File f2) {
        String name1 = removeExtension(f1.getName());
        String name2 = removeExtension(f2.getName());

        // Thử tách phần số ở cuối tên file (ví dụ: test1, test2 -> lấy 1, 2)
        // Cách đơn giản nhất: regex lấy toàn bộ số trong chuỗi
        String num1Str = name1.replaceAll("\\D+", "");
        String num2Str = name2.replaceAll("\\D+", "");

        if (!num1Str.isEmpty() && !num2Str.isEmpty()) {
            try {
                // So sánh số trước nếu cả 2 đều có số
                int cmp = Long.compare(Long.parseLong(num1Str), Long.parseLong(num2Str));
                if (cmp != 0) return cmp;
            } catch (NumberFormatException ignored) {}
        }

        // Fallback về so sánh chuỗi thông thường
        return name1.compareToIgnoreCase(name2);
    }

    private String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}