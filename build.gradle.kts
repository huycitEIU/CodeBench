import org.gradle.internal.os.OperatingSystem

plugins {
    java
    application
    // Plugin tạo Fat Jar (Gộp tất cả vào 1 file)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.stukit"
version = "2.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.0"
val javafxVersion = "21.0.6"

// Xác định hệ điều hành
val platform = when {
    OperatingSystem.current().isWindows -> "win"
    OperatingSystem.current().isMacOsX -> "mac"
    else -> "linux"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    // Trỏ vào class Launcher (QUAN TRỌNG: Phải dùng Launcher, không dùng MainApp trực tiếp)
    mainClass.set("com.stukit.codebench.Launcher")
}

dependencies {
    // JavaFX (Khai báo đủ 4 món ăn chơi)
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$platform")

    // Thư viện ngoài
    implementation("org.fxmisc.richtext:richtextfx:0.11.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Cấu hình Shadow Jar (Fat Jar)
tasks.shadowJar {
    archiveBaseName.set("CodeBench")
    archiveClassifier.set("all") // Tên file sẽ là CodeBench-all.jar
    archiveVersion.set("2.0.0")

    // Gộp các file cấu hình service
    mergeServiceFiles()

    // Loại bỏ chữ ký số để tránh lỗi SecurityException
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

// === TASK TỰ TẠO: TẠO BẢN PORTABLE (CHẠY NGAY) ===
tasks.register<Exec>("createPortable") {
    group = "distribution"
    description = "Create Windows Portable App (Folder) using jpackage"

    dependsOn("shadowJar")

    val inputDir = layout.buildDirectory.dir("libs").get().asFile.absolutePath
    val inputJar = "CodeBench-2.0.0-all.jar"
    val outputDir = layout.buildDirectory.dir("distribution").get().asFile.absolutePath

    // Đường dẫn đến thư mục runtime sẽ được tạo ra
    val runtimeBin = "$outputDir/CodeBench/runtime/bin"

    doFirst {
        file(outputDir).deleteRecursively()
    }

    commandLine(
        "jpackage",
        "--type", "app-image",
        "--dest", outputDir,
        "--input", inputDir,
        "--name", "CodeBench",
        "--main-class", "com.stukit.codebench.Launcher",
        "--main-jar", inputJar,
        "--icon", "app.ico",
        "--win-console"
    )

    // --- BƯỚC MỚI: COPY CÁC CÔNG CỤ CẦN THIẾT VÀO RUNTIME ---
    doLast {
        println("Dang bo sung java.exe va javac.exe vao runtime...")

        // Lấy đường dẫn JDK hiện tại trên máy bạn
        val hostJavaHome = System.getProperty("java.home")

        // Copy java.exe
        copy {
            from("$hostJavaHome/bin/java.exe")
            into(runtimeBin)
        }

        // Copy javac.exe (Trình biên dịch)
        // Lưu ý: javac thường nằm trong JDK/bin, nếu java.home trỏ vào JRE có thể không thấy
        // Ta thử tìm trong cùng thư mục với java.exe
        if (file("$hostJavaHome/bin/javac.exe").exists()) {
            copy {
                from("$hostJavaHome/bin/javac.exe")
                into(runtimeBin)
            }
        } else {
            // Nếu đang chạy bằng JRE, thử lùi ra một cấp để tìm JDK (thường là ../bin/javac.exe)
            copy {
                from("$hostJavaHome/../bin/javac.exe")
                into(runtimeBin)
            }
        }

        println("Da copy xong! Runtime gio da co the compile va run code.")
    }
}