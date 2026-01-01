# CodeBench

**CodeBench** là công cụ **Offline Code Judge** dành cho sinh viên và giảng viên, phục vụ học tập, thực hành và đánh giá bài tập lập trình mà **không phụ thuộc vào kết nối Internet**.

Phần mềm được phát triển theo định hướng học thuật, ổn định và dễ triển khai trong môi trường lớp học và phòng máy.

🔗 Repository: https://github.com/huycitEIU/CodeBench  
⬇ Download (Latest Release): https://github.com/huycitEIU/CodeBench/releases/latest  

---

## Tech Stack
- **Java 17 (LTS)**
- **JavaFX**
- **Gradle**

---

## StuKit CodeBench v2.0.0  
### _“The Architecture Update”_

Phiên bản 2.0.0 là bản cập nhật lớn, tập trung vào **tái cấu trúc kiến trúc**, cải thiện **độ ổn định**, **hiệu năng**, và **trải nghiệm sử dụng** trong môi trường học thuật.

---

## New Features
- **Dark Mode Support**  
  Giao diện tối giúp giảm mỏi mắt khi sử dụng trong thời gian dài.
- **Custom Fonts**  
  Tích hợp **JetBrains Mono**, tăng khả năng đọc và theo dõi mã nguồn.

---

## Architecture & Performance Improvements
- **File-Based Execution**  
  Chuyển từ xử lý dựa trên RAM sang xử lý bằng **File Stream**.
  - Khắc phục lỗi tràn bộ nhớ với output lớn
  - Tăng độ ổn định khi chạy nhiều test case liên tục
- **Smart Cleanup**  
  Cơ chế tự động dọn dẹp file tạm và file rác khi đóng ứng dụng
- **MVC Refactoring**  
  Tách biệt rõ ràng giữa giao diện và logic xử lý
- **Manual Testcase Enhancement**  
  Tinh chỉnh chức năng thêm và quản lý test case thủ công
- **Detail Viewer (Lazy Loading)**  
  Xem chi tiết *Input / Output / Expected Output* mà không ảnh hưởng hiệu năng

---

## Bug Fixes
- Khắc phục lỗi **“File is being used by another process”** trên Windows
- Sửa lỗi giao diện bị treo khi chương trình chạy vòng lặp vô hạn  
  (nhờ cơ chế xử lý đa luồng với `Thread / Task`)

---

## Previous Versions
- **v1.x**: Hỗ trợ chấm bài offline cơ bản

---

## StuKit Ecosystem
CodeBench là một phần của hệ sinh thái **StuKit**, bao gồm:
- **CodeBench** – Offline Code Judge
- **Flow** – Công cụ hỗ trợ quy trình học tập *(đang lên kế hoạch)*
- **PlanU** – Quản lý kế hoạch cá nhân *(đang lên kế hoạch)*

---

## Academic Use
Phần mềm được phát triển **phục vụ mục đích học tập và nghiên cứu**,  
không hướng tới sử dụng trong các kỳ thi chính thức có giám sát nghiêm ngặt.

---

## Author
**Trần Gia Huy**  
Software Engineering – Eastern International University  

📧 Email: huy.trangia.cit23@eiu.edu.vn  
🌐 EIU IT Club: https://www.eiuitclub.io.vn/

---

© 2026 CodeBench – StuKit Project
