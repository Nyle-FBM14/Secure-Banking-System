module com.nyle {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires securinyle;

    opens com.atm to javafx.fxml;
    exports com.atm;
    opens com.atm.controllers to javafx.fxml;
    exports com.atm.controllers;
}
