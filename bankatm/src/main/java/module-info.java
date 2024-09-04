module com.nyle {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.nyle to javafx.fxml;
    exports com.nyle;
}
