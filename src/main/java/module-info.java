module com.ecocycle {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens com.ecocycle.app        to javafx.fxml;
    opens com.ecocycle.controller to javafx.fxml;
    opens com.ecocycle.model      to javafx.base, javafx.fxml;

    exports com.ecocycle.app;
    exports com.ecocycle.controller;
    exports com.ecocycle.model;
    exports com.ecocycle.service;
    exports com.ecocycle.repository;
    exports com.ecocycle.util;
}
