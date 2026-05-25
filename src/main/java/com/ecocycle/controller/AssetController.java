package com.ecocycle.controller;

import com.ecocycle.model.Asset;
import com.ecocycle.service.AssetService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Controller JavaFX para a tela de cadastro/listagem de ativos (RF01 / RF02 / RF05).
 */
public class AssetController {

    // --- Formulario ---
    @FXML private TextField idField;
    @FXML private TextField categoryField;
    @FXML private TextField modelField;
    @FXML private DatePicker acquiredAtPicker;
    @FXML private TextField lifespanField;
    @FXML private ComboBox<Asset.Status> statusCombo;

    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;
    @FXML private Label  feedbackLabel;

    // --- Filtros (RF05) ---
    @FXML private ComboBox<String> hazardFilter;
    @FXML private TextField        searchField;

    // --- Tabela ---
    @FXML private TableView<Asset>            assetsTable;
    @FXML private TableColumn<Asset, String>  colId;
    @FXML private TableColumn<Asset, String>  colCategory;
    @FXML private TableColumn<Asset, String>  colModel;
    @FXML private TableColumn<Asset, String>  colAcquired;
    @FXML private TableColumn<Asset, String>  colLifespan;
    @FXML private TableColumn<Asset, String>  colExpiration;
    @FXML private TableColumn<Asset, String>  colStatus;
    @FXML private TableColumn<Asset, String>  colHazard;
    @FXML private TableColumn<Asset, String>  colDeprec;

    private final AssetService service = new AssetService();
    private final ObservableList<Asset> allAssets     = FXCollections.observableArrayList();
    private final FilteredList<Asset>   filteredAssets = new FilteredList<>(allAssets, a -> true);
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean editMode = false;

    @FXML
    public void initialize() {
        // Status combo
        statusCombo.setItems(FXCollections.observableArrayList(Asset.Status.values()));
        statusCombo.getSelectionModel().select(Asset.Status.ACTIVE);

        // Filtro de periculosidade (RF05)
        List<String> hazardOptions = Arrays.asList("Todas", "Alta", "Media", "Baixa");
        hazardFilter.setItems(FXCollections.observableArrayList(hazardOptions));
        hazardFilter.getSelectionModel().select("Todas");
        hazardFilter.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        // Colunas da tabela
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colModel.setCellValueFactory(c -> new SimpleStringProperty(
                nvl(c.getValue().getModel())));
        colAcquired.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAcquiredAt().format(dateFmt)));
        colLifespan.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLifespanYears() + " anos"));
        colExpiration.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getExpirationDate().format(dateFmt)));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().getValue()));
        // RF05 — periculosidade
        colHazard.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHazardLevel().getLabel()));
        // RF02 — depreciacao
        colDeprec.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepreciationPercent() + "%"));

        // RF02 — colorir linhas por status
        assetsTable.setRowFactory(tv -> new TableRow<Asset>() {
            @Override
            protected void updateItem(Asset item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-overdue", "row-expiring", "row-disposed", "row-active");
                if (item == null || empty) return;
                switch (item.getStatus()) {
                    case OVERDUE   -> getStyleClass().add("row-overdue");
                    case EXPIRING  -> getStyleClass().add("row-expiring");
                    case DISPOSED  -> getStyleClass().add("row-disposed");
                    default        -> getStyleClass().add("row-active");
                }
            }
        });

        assetsTable.setItems(filteredAssets);

        assetsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, newSel) -> {
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
            asset.setModel(nvl(modelField.getText()).trim());
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

    /** Limpa filtros de periculosidade e texto (RF05). */
    @FXML
    private void onClearFilter() {
        hazardFilter.getSelectionModel().select("Todas");
        searchField.clear();
    }

    // -----------------------------------------------------------------------
    // Internos
    // -----------------------------------------------------------------------

    private void applyFilter() {
        String hazardSel = hazardFilter.getValue();
        String text      = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        filteredAssets.setPredicate(asset -> {
            // Filtro de periculosidade
            if (hazardSel != null && !hazardSel.equals("Todas")) {
                if (!asset.getHazardLevel().getLabel().equalsIgnoreCase(hazardSel)) return false;
            }
            // Filtro de texto livre
            if (!text.isBlank()) {
                boolean match = asset.getId().toLowerCase().contains(text)
                             || asset.getCategory().toLowerCase().contains(text)
                             || (asset.getModel() != null && asset.getModel().toLowerCase().contains(text));
                if (!match) return false;
            }
            return true;
        });
    }

    private void loadIntoForm(Asset a) {
        idField.setText(a.getId());
        categoryField.setText(a.getCategory());
        modelField.setText(nvl(a.getModel()));
        acquiredAtPicker.setValue(a.getAcquiredAt());
        lifespanField.setText(String.valueOf(a.getLifespanYears()));
        statusCombo.setValue(a.getStatus());
    }

    private void refreshTable() {
        allAssets.setAll(service.listAll());
        applyFilter();
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

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
