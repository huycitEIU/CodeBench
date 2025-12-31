# CodeBench

Offline code judging tool for students.

## Tech
- Java 17
- JavaFX
- Gradle

## Stukit CodeBench v2.0.0 - "The Architecture Update"

## New Features
* **Dark Mode Support:** Giao diện tối giúp dịu mắt.
* **Custom Fonts:** Tích hợp font JetBrains Mono.

## Architecture & Performance
* **File-Based Execution:** Chuyển từ xử lý RAM sang xử lý File Stream.
    * Khắc phục lỗi tràn bộ nhớ với output lớn.
    * Tăng độ ổn định khi chạy nhiều testcase liên tục.
* **Smart Cleanup:** Cơ chế tự động dọn dẹp file rác khi đóng ứng dụng.
* **MVC Refactoring:** Tách biệt logic xử lý và giao diện.
* **Manual Testcase:** Tinh chỉnh chức năng thêm testcase thủ công.
* **Detail Viewer:** Xem chi tiết Input/Output/Expected bằng Lazy Loading.

## Bug Fixes
* Sửa lỗi "File is being used by another process" trên Windows.
* Sửa lỗi giao diện bị đơ khi chạy vòng lặp vô hạn (nhờ cơ chế Thread/Task mới).

Version 1 – basic judging supported.
