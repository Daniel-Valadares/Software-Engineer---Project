package com.ecocycle.controller;

import com.ecocycle.model.Asset;
import com.ecocycle.service.AssetService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

/**
 * Controller JavaFX para a tela de cadastro/listagem de ativos (RF01).
 */
public class AssetController {

    @FXML private TextField idField;
    @FXML private TextField categoryField;
    @FXML private TextField modelField;
    @FXML private DatePicker acquiredAtPicker;
    @FXML private TextField lifespanField;
    @FXML private ComboBox<Asset.Status> statusCombo;

    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;

    @FXML private TableView<Asset> assetsTable;
    @FXML private TableColumn<Asset, String> colId;
    @FXML private TableColumn<Asset, String> colCategory;
    @FXML private TableColumn<Asset, String> colModel;
    @FXML private TableColumn<Asset, String> colAcquired;
    @FXML private TableColumn<Asset, String> colLifespan;
    @FXML private TableColumn<Asset, String> colExpiration;
    @FXML private TableColumn<Asset, String> colStatus;

    @FXML private Label feedbackLabel;

    private final AssetService service = new AssetService();
    private final ObservableList<Asset> assets = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean editMode = false;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(Asset.Status.values()));
        statusCombo.getSelectionModel().select(Asset.Status.ACTIVE);

        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colModel.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getModel() == null ? "" : c.getValue().getModel()));
        colAcquired.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAcquiredAt().format(dateFmt)));
        colLifespan.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLifespanYears() + " anos"));
        colExpiration.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getExpirationDate().format(dateFmt)));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().getValue()));

        assetsTable.setItems(assets);

        assetsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadIntoForm(newSel);
                editMode = true;
                idField.setEditable(false);
                deleteButton.setDisable(false);
            }
        });

        refreshTable();
    }

    @FXML
    private void onSave() {
        try {
            Asset asset = new Asset();
            asset.setId(idField.getText().trim());
            asset.setCategory(categoryField.getText().trim());
            asset.setModel(modelField.getText() == null ? "" : modelField.getText().trim());
            asset.setAcquiredAt(acquiredAtPicker.getValue());
            asset.setLifespanYears(parseIntSafe(lifespanField.getText()));
            asset.setStatus(statusCombo.getValue());

            if (editMode) {
                service.update(asset);
                showFeedback("Ativo atualizado com sucesso!", false);
            } else {
                service.register(asset);
                showFeedback("Ativo cadastrado com sucesso!", false);
            }
            refreshTable();
            onClear();
        } catch (IllegalArgumentException e) {
            showFeedback(e.getMessage(), true);
        } catch (Exception e) {
            showAlert("Erro inesperado", e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        idField.clear();
        categoryField.clear();
        modelField.clear();
        acquiredAtPicker.setValue(null);
        lifespanField.clear();
        statusCombo.getSelectionModel().select(Asset.Status.ACTIVE);
        editMode = false;
        idField.setEditable(true);
        deleteButton.setDisable(true);
        assetsTable.getSelectionModel().clearSelection();
        feedbackLabel.setText("");
    }

    @FXML
    private void onDelete() {
        Asset selected = assetsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Remover o ativo '" + selected.getId() + "'?");
        confirm.setHeaderText("Confirmar exclusao");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                service.delete(selected.getId());
                showFeedback("Ativo removido.", false);
                refreshTable();
                onClear();
            }
        });
    }

    private void loadIntoForm(Asset a) {
        idField.setText(a.getId());
        categoryField.setText(a.getCategory());
        modelField.setText(a.getModel());
        acquiredAtPicker.setValue(a.getAcquiredAt());
        lifespanField.setText(String.valueOf(a.getLifespanYears()));
        statusCombo.setValue(a.getStatus());
    }

    private void refreshTable() {
        assets.setAll(service.listAll());
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vida util deve ser um numero inteiro");
        }
    }

    private void showFeedback(String msg, boolean isError) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle(isError
                ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;"
                : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
