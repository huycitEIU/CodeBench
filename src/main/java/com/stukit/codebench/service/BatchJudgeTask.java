package com.stukit.codebench.service;

import com.stukit.codebench.domain.JudgeResult;
import com.stukit.codebench.domain.TestCase;
import com.stukit.codebench.domain.TestResultRow;
import com.stukit.codebench.infrastructure.compiler.CompileException;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.function.Consumer;

/**
 * Task chạy ngầm để chấm toàn bộ danh sách bài test.
 * <p>
 * Logic:
 * 1. Compile (1 lần).
 * 2. Loop qua từng TestCase -> Run.
 * 3. Update UI real-time.
 */
public class BatchJudgeTask extends Task<Void> {

    private final JudgeService.JudgeSession session;
    private final List<TestResultRow> rows;
    private final Consumer<String> statusUpdater; // Callback để update thanh trạng thái
    private final Runnable scoreUpdater;          // Callback để update điểm số

    public BatchJudgeTask(JudgeService.JudgeSession session,
                          List<TestResultRow> rows,
                          Consumer<String> statusUpdater,
                          Runnable scoreUpdater) {
        this.session = session;
        this.rows = rows;
        this.statusUpdater = statusUpdater;
        this.scoreUpdater = scoreUpdater;
    }

    @Override
    protected Void call() {
        try {
            // 1. Reset trạng thái UI
            Platform.runLater(() -> {
                rows.forEach(TestResultRow::reset);
                statusUpdater.accept("Đang biên dịch code...");
            });

            // 2. Compile
            session.compile();

            // 3. Chạy từng test case
            int total = rows.size();
            for (int i = 0; i < total; i++) {
                if (isCancelled()) break; // Cho phép hủy nếu người dùng muốn

                TestResultRow row = rows.get(i);
                TestCase testCase = row.getTestCase();

                // Update UI: Đang chạy
                int finalI = i + 1;
                Platform.runLater(() -> {
                    row.updateStatus("Running...");
                    statusUpdater.accept(String.format("Đang chấm %d/%d: %s", finalI, total, testCase.name()));
                });

                // --- CHẤM BÀI (Nặng nhất) ---
                JudgeResult result = session.runTestCase(testCase);

                // Update UI: Kết quả
                Platform.runLater(() -> {
                    row.updateResult(
                            result.getStatus(),
                            result.getRunTimeMs(),
                            result.getMemoryBytes(),
                            result.getActualOutputPath()
                    );
                    scoreUpdater.run(); // Tính lại điểm ngay lập tức
                });
            }

            Platform.runLater(() -> statusUpdater.accept("Đã chấm xong."));

        } catch (CompileException e) {
            // Xử lý lỗi Compile
            Platform.runLater(() -> {
                rows.forEach(row -> row.updateResult("CE", 0, 0, null));
                statusUpdater.accept("Lỗi biên dịch: " + e.getMessage());
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusUpdater.accept("Lỗi hệ thống: " + e.getMessage()));
        }
        return null;
    }
}