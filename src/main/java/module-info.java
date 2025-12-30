module com.stukit.codebench {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens com.stukit.codebench to javafx.fxml;
    exports com.stukit.codebench;
    exports com.stukit.codebench.ui.controller;
    opens com.stukit.codebench.ui.controller to javafx.fxml;
}