package com.ecocycle.controller;

import com.ecocycle.service.ReportService;
import com.ecocycle.service.ReportService.ReportData;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Controller do Relatorio de Impacto Ambiental (RF04).
 */
public class ReportController {

    @FXML private Label rptTotal;
    @FXML private Label rptActive;
    @FXML private Label rptExpiring;
    @FXML private Label rptOverdue;
    @FXML private Label rptDisposed;
    @FXML private Label rptDiskSanitized;
    @FXML private Label rptBatteryRemoved;
    @FXML private Label rptHigh;
    @FXML private Label rptMedium;
    @FXML private Label rptLow;
    @FXML private Label rptCo2;
    @FXML private Label rptEwaste;
    @FXML private Label rptWater;

    @FXML private TextArea reportArea;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar Relatorio");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo de Texto", "*.txt"));
        chooser.setInitialFileName("relatorio-ecocycle.txt");
        File file = chooser.showSaveDialog(reportArea.getScene().getWindow());
        if (file == null) return;

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(reportArea.getText());
            new Alert(Alert.AlertType.INFORMATION,
                    "Relatorio exportado com sucesso!\n" + file.getAbsolutePath())
                    .showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao exportar: " + e.getMessage()).showAndWait();
        }
    }

    private void refresh() {
        ReportData d = reportService.generate();

        rptTotal.setText(String.valueOf(d.total));
        rptActive.setText(String.valueOf(d.active));
        rptExpiring.setText(String.valueOf(d.expiring));
        rptOverdue.setText(String.valueOf(d.overdue));
        rptDisposed.setText(String.valueOf(d.disposed));
        rptDiskSanitized.setText(String.valueOf(d.diskSanitized));
        rptBatteryRemoved.setText(String.valueOf(d.batteryRemoved));

        rptHigh.setText(String.valueOf(
                d.byHazard.getOrDefault(com.ecocycle.model.Asset.HazardLevel.HIGH, 0L)));
        rptMedium.setText(String.valueOf(
                d.byHazard.getOrDefault(com.ecocycle.model.Asset.HazardLevel.MEDIUM, 0L)));
        rptLow.setText(String.valueOf(
                d.byHazard.getOrDefault(com.ecocycle.model.Asset.HazardLevel.LOW, 0L)));

        rptCo2.setText(String.format("%.1f kg", d.co2Saved));
        rptEwaste.setText(String.format("%.1f kg", d.eWasteAvoided));
        rptWater.setText(String.format("%.0f L", d.waterSaved));

        reportArea.setText(reportService.generateText());
    }
}
