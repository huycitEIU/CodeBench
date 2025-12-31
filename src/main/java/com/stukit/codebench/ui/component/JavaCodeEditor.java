package com.stukit.codebench.ui.component;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Component soạn thảo code Java có hỗ trợ Syntax Highlighting.
 * Sử dụng thư viện RichTextFX.
 */
public class JavaCodeEditor extends CodeArea {

    // --- Mẫu Code Mặc Định ---
    private static final String DEFAULT_CODE = """
            import java.util.Scanner;

            public class Solution {
                public static void main(String[] args) {
                    // Viết code của bạn ở đây
                    Scanner scanner = new Scanner(System.in);
                    int n = scanner.nextInt();
                    System.out.println("Input: " + n);
                }
            }
            """;

    // --- Định nghĩa Keywords ---
    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
            "var"
    };

    // --- Regex Groups ---
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    // Tổng hợp Regex
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public JavaCodeEditor() {
        // 1. Thêm số dòng bên trái
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));

        // 2. Logic highlight tự động (Debounce 200ms để tránh lag khi gõ nhanh)
        this.multiPlainChanges()
                .successionEnds(Duration.ofMillis(200))
                .subscribe(ignore -> this.setStyleSpans(0, computeHighlighting(this.getText())));

        // 3. Set nội dung ban đầu
        this.replaceText(0, 0, DEFAULT_CODE);

        // 4. Highlight thủ công ngay lần đầu (vì sự kiện change chưa trigger)
        this.setStyleSpans(0, computeHighlighting(DEFAULT_CODE));
    }

    /**
     * Tính toán các span style dựa trên Regex.
     * Hàm này chạy khá nặng nên cần được gọi bất đồng bộ hoặc debounce.
     */
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = determineStyleClass(matcher);
            assert styleClass != null;

            // Thêm phần text thường (không style) nằm giữa các match
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            // Thêm phần text khớp Regex (có style)
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());

            lastKwEnd = matcher.end();
        }

        // Thêm phần text còn lại cuối cùng
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Helper xác định tên CSS class dựa trên Group name của Regex.
     */
    private static String determineStyleClass(Matcher matcher) {
        if (matcher.group("KEYWORD") != null)   return "keyword";
        if (matcher.group("PAREN") != null)     return "paren";
        if (matcher.group("BRACE") != null)     return "brace";
        if (matcher.group("BRACKET") != null)   return "bracket";
        if (matcher.group("SEMICOLON") != null) return "semicolon";
        if (matcher.group("STRING") != null)    return "string";
        if (matcher.group("COMMENT") != null)   return "comment";
        return "default";
    }
}