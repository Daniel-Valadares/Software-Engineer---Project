package com.ecocycle.app;

/**
 * Launcher separado que NAO estende javafx.application.Application.
 * Necessario para rodar o fat-JAR via "java -jar" sem precisar
 * de module-path explicito do JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
