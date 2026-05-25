package com.ecocycle.controller;

import com.ecocycle.model.Asset;
import com.ecocycle.model.Disposal;
import com.ecocycle.service.AssetService;
import com.ecocycle.service.DisposalService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller do Checklist de Descarte responsavel (RF03).
 */
public class DisposalController {

    // --- Formulario ---
    @FXML private ComboBox<Asset>   assetCombo;
    @FXML private DatePicker        disposedAtPicker;
    @FXML private CheckBox          diskSanitizedCheck;
    @FXML private CheckBox          batteryRemovedCheck;
    @FXML private ComboBox<String>  destinationCombo;
    @FXML private Button            registerButton;
    @FXML private Label             feedbackLabel;

    // --- Tabela de descartes ---
    @FXML private TableView<Disposal>           disposalsTable;
    @FXML private TableColumn<Disposal, String> colDspId;
    @FXML private TableColumn<Disposal, String> colDspAsset;
    @FXML private TableColumn<Disposal, String> colDspDate;
    @FXML private TableColumn<Disposal, String> colDspDisk;
    @FXML private TableColumn<Disposal, String> colDspBattery;
    @FXML private TableColumn<Disposal, String> colDspDest;

    private final AssetService    assetService    = new AssetService();
    private final DisposalService disposalService = new DisposalService();
    private final ObservableList<Asset>    assetsObs    = FXCollections.observableArrayList();
    private final ObservableList<Disposal> disposalsObs = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Destinos pre-definidos
        destinationCombo.setItems(FXCollections.observableArrayList(
                "Cooperativa de reciclagem",
                "Fabricante (programa take-back)",
                "Reciclador certificado",
                "Ponto de coleta municipal",
                "Outro"
        ));
        destinationCombo.getSelectionModel().selectFirst();

        disposedAtPicker.setValue(LocalDate.now());

        // Configura ComboBox de ativos (apenas nao-descartados)
        assetCombo.setItems(assetsObs);
        assetCombo.setCellFactory(lv -> new ListCell<Asset>() {
            @Override
            protected void updateItem(Asset item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? null : item.getId() + " — " + item.getCategory());
            }
        });
        assetCombo.setButtonCell(new ListCell<Asset>() {
            @Override
            protected void updateItem(Asset item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? "Selecione um ativo..." : item.getId() + " — " + item.getCategory());
            }
        });

        // Colunas da tabela de descartes
        colDspId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colDspAsset.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssetId()));
        colDspDate.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getDisposedAt();
            return new SimpleStringProperty(d == null ? "-" : d.format(dateFmt));
        });
        colDspDisk.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isDiskSanitized() ? "Sim" : "Nao"));
        colDspBattery.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isBatteryRemoved() ? "Sim" : "Nao"));
        colDspDest.setCellValueFactory(c -> new SimpleStringProperty(
                nvl(c.getValue().getDestination())));

        disposalsTable.setItems(disposalsObs);
        refresh();
    }

    @FXML
    private void onRegister() {
        try {
            Asset selected = assetCombo.getValue();
            if (selected == null) {
                showFeedback("Selecione um ativo para descartar.", true);
                return;
            }

            Disposal d = new Disposal();
            d.setAssetId(selected.getId());
            d.setDisposedAt(disposedAtPicker.getValue());
            d.setDiskSanitized(diskSanitizedCheck.isSelected());
            d.setBatteryRemoved(batteryRemovedCheck.isSelected());
            d.setDestination(destinationCombo.getValue());

            disposalService.register(d);
            showFeedback("Descarte registrado com sucesso! Ativo marcado como DESCARTADO.", false);
            refresh();
            clearForm();
        } catch (IllegalArgumentException e) {
            showFeedback(e.getMessage(), true);
        } catch (Exception e) {
            showFeedback("Erro inesperado: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onClearForm() {
        clearForm();
    }

    private void clearForm() {
        assetCombo.getSelectionModel().clearSelection();
        disposedAtPicker.setValue(LocalDate.now());
        diskSanitizedCheck.setSelected(false);
        batteryRemovedCheck.setSelected(false);
        destinationCombo.getSelectionModel().selectFirst();
        feedbackLabel.setText("");
    }

    private void refresh() {
        // Carrega ativos que ainda nao foram descartados
        List<Asset> active = assetService.listAll().stream()
                .filter(a -> a.getStatus() != Asset.Status.DISPOSED)
                .collect(Collectors.toList());
        assetsObs.setAll(active);

        // Carrega historico de descartes
        disposalsObs.setAll(disposalService.listAll());
    }

    private void showFeedback(String msg, boolean isError) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle(isError
                ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;"
                : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
