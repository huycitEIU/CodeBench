package com.stukit.codebench;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.infrastructure.compiler.JavaCompilerService;
import com.stukit.codebench.infrastructure.fs.WorkspaceFactory;
import com.stukit.codebench.infrastructure.parser.DefaultOutputParser;
import com.stukit.codebench.infrastructure.parser.OutputParser;
import com.stukit.codebench.infrastructure.runner.JavaRunner;
import com.stukit.codebench.service.JudgeService;
import javafx.application.Application;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
//    public static void main(String[] args) {
//        // 1. Cấu hình các Component (Dependency Injection)
//        // Lưu workspace tạm ở thư mục hiện tại của project
//        WorkspaceFactory workspaceFactory = new WorkspaceFactory(Path.of("./temp-workspaces"));
//
//        JavaCompilerService compilerService = new JavaCompilerService();
//        JavaRunner runner = new JavaRunner();
//        OutputParser parser = new DefaultOutputParser(); // Dùng parser mặc định
//
//        // 2. Chuẩn bị dữ liệu bài toán (A + B)
//        // Code mẫu của người dùng nộp lên
//        String sourceCode = """
//                import java.util.Scanner;
//
//                public class Solution {
//                    public static void main(String[] args) {
//                        Scanner scanner = new Scanner(System.in);
//                        if (scanner.hasNextInt()) {
//                            int a = scanner.nextInt();
//                            int b = scanner.nextInt();
//                            System.out.println(a + b);
//                        }
//                    }
//                }
//            """;
//
//        // Tạo bộ test case
//        List<TestCase> testCases = Arrays.asList(
//                new TestCase("Test 1 (Cơ bản)", "1 2", "3"),
//                new TestCase("Test 2 (Số lớn)", "100 200", "300"),
//                new TestCase("Test 3 (Sai)", "5 5", "11") // Cố tình expected sai để test case này Fail
//        );
//
//        // 3. Khởi tạo JudgeService
//        JudgeService judgeService = new JudgeService(
//                workspaceFactory,
//                compilerService,
//                runner,
//                parser,
//                sourceCode
//        );
//
//        System.out.println("--- Bắt đầu chấm bài ---");
//
//        // 4. Chấm bài (Time limit 1000ms)
//        List<JudgeResult> results = judgeService.judge(testCases, 1000);
//
//        // 5. In kết quả
//        for (JudgeResult result : results) {
//            System.out.println(String.format("[%s] %s | Time: %dms",
//                    result.getVerdict(),
//                    result.getName(),
//                    result.getRunTimeMs()));
//
//            if (!result.isPassed()) {
//                System.out.println("   -> Actual: " + result.getOutput());
//                System.out.println("   -> Error : " + result.getError());
//            }
//        }
//
//        System.out.println("--- Hoàn tất ---");
//    }
}
