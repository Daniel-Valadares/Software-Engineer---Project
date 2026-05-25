package com.ecocycle.app;

import com.ecocycle.util.DBConnection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Ponto de entrada da aplicacao JavaFX.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DBConnection.getInstance();

        URL fxml = getClass().getResource("/fxml/MainView.fxml");
        if (fxml == null) {
            throw new IllegalStateException("MainView.fxml nao encontrado em resources/fxml/");
        }
        Parent root = FXMLLoader.load(fxml);

        Scene scene = new Scene(root, 1100, 720);
        primaryStage.setTitle("EcoCycle Manager - Logistica Reversa (ODS 12)");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DBConnection.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
