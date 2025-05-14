module com.stima {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.stima to javafx.fxml;
    exports com.stima;
}
