module com.stima {
    requires transitive javafx.graphics;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.stima to javafx.fxml;
    exports com.stima;
}
