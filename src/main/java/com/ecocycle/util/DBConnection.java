package com.ecocycle.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Singleton de conexao com SQLite.
 * O arquivo ecocycle.db e criado automaticamente no diretorio de trabalho.
 */
public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:ecocycle.db";
    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            this.connection = DriverManager.getConnection(DB_URL);
            initializeSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao conectar com o banco SQLite", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeSchema() {
        try (InputStream is = getClass().getResourceAsStream("/db/schema.sql")) {
            if (is == null) {
                throw new RuntimeException("schema.sql nao encontrado em resources/db/");
            }
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            try (Statement stmt = connection.createStatement()) {
                for (String statement : sql.split(";")) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Falha ao inicializar schema do banco", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexao: " + e.getMessage());
        }
    }
}
