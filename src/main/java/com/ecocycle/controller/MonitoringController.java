package com.ecocycle.controller;

import com.ecocycle.model.Asset;
import com.ecocycle.service.AssetService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller do painel de Monitoramento de Ciclo de Vida (RF02).
 * Exibe depreciacao, status e alertas de ativos proximos ao vencimento.
 */
public class MonitoringController {

    @FXML private Label lblTotal;
    @FXML private Label lblActive;
    @FXML private Label lblExpiring;
    @FXML private Label lblOverdue;
    @FXML private Label lblDisposed;

    @FXML private TableView<Asset>           monTable;
    @FXML private TableColumn<Asset, String> monColId;
    @FXML private TableColumn<Asset, String> monColCategory;
    @FXML private TableColumn<Asset, String> monColModel;
    @FXML private TableColumn<Asset, String> monColExpiration;
    @FXML private TableColumn<Asset, String> monColDaysLeft;
    @FXML private TableColumn<Asset, String> monColDeprec;
    @FXML private TableColumn<Asset, String> monColStatus;

    private final AssetService service = new AssetService();
    private final ObservableList<Asset> data = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        monColId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        monColCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        monColModel.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getModel() == null ? "" : c.getValue().getModel()));
        monColExpiration.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getExpirationDate() == null ? "-" :
                c.getValue().getExpirationDate().format(dateFmt)));
        monColDaysLeft.setCellValueFactory(c -> {
            long days = c.getValue().getDaysUntilExpiration();
            String label = days < 0
                    ? "Vencido há " + Math.abs(days) + " dias"
                    : days + " dias restantes";
            return new SimpleStringProperty(label);
        });
        monColDeprec.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepreciationPercent() + "%"));
        monColStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().getValue()));

        // Colorir linhas por urgência (RF02)
        monTable.setRowFactory(tv -> new TableRow<Asset>() {
            @Override
            protected void updateItem(Asset item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-overdue", "row-expiring", "row-disposed", "row-active");
                if (item == null || empty) return;
                switch (item.getStatus()) {
                    case OVERDUE  -> getStyleClass().add("row-overdue");
                    case EXPIRING -> getStyleClass().add("row-expiring");
                    case DISPOSED -> getStyleClass().add("row-disposed");
                    default       -> getStyleClass().add("row-active");
                }
            }
        });

        monTable.setItems(data);
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        List<Asset> all = service.listAll();

        long total    = all.size();
        long active   = all.stream().filter(a -> a.getStatus() == Asset.Status.ACTIVE).count();
        long expiring = all.stream().filter(a -> a.getStatus() == Asset.Status.EXPIRING).count();
        long overdue  = all.stream().filter(a -> a.getStatus() == Asset.Status.OVERDUE).count();
        long disposed = all.stream().filter(a -> a.getStatus() == Asset.Status.DISPOSED).count();

        lblTotal.setText(String.valueOf(total));
        lblActive.setText(String.valueOf(active));
        lblExpiring.setText(String.valueOf(expiring));
        lblOverdue.setText(String.valueOf(overdue));
        lblDisposed.setText(String.valueOf(disposed));

        // Mostra primeiro os que precisam de atencao
        all.sort((a, b) -> Long.compare(a.getDaysUntilExpiration(), b.getDaysUntilExpiration()));
        data.setAll(all);
    }
}
