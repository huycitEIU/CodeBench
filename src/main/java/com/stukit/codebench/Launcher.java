package com.stukit.codebench;

/**
 * Class khởi động phụ (Wrapper).
 * <p>
 * Mục đích: Dùng để đánh lừa JVM khi chạy ứng dụng dưới dạng "Fat Jar" (Uber-jar).
 * Nếu gọi trực tiếp App.main() trong Jar, JavaFX sẽ báo lỗi thiếu module Runtime components.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}