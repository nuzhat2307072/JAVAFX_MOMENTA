package com.momenta.controller;

import com.momenta.dao.FinanceDAO;
import com.momenta.model.FinanceEntry;
import com.momenta.model.User;
import com.momenta.util.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FinanceController {

    @FXML private SidebarController sidebarController;
    @FXML private Label incomeLabel;
    @FXML private Label expensesLabel;
    @FXML private Label netLabel;
    @FXML private ListView<FinanceEntry> entriesList;
    @FXML private PieChart expenseChart;

    @FXML
    public void initialize() {
        sidebarController.setActive("finance");
        entriesList.setCellFactory(list -> new EntryCell());
        loadEntries();
    }

    private void loadEntries() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new FinanceDAO().getAllByUser(user.getId()),
                (List<FinanceEntry> entries) -> {
                    entriesList.setItems(FXCollections.observableArrayList(entries));
                    double income = entries.stream().filter(e -> "INCOME".equals(e.getType())).mapToDouble(FinanceEntry::getAmount).sum();
                    double expenses = entries.stream().filter(e -> "EXPENSE".equals(e.getType())).mapToDouble(FinanceEntry::getAmount).sum();
                    incomeLabel.setText("৳" + format(income));
                    expensesLabel.setText("৳" + format(expenses));
                    netLabel.setText("৳" + format(income - expenses));
                    updateExpenseChart(entries);
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load finance entries: " + error.getMessage()).showAndWait()
        );
    }

    private String format(double amount) {
        return String.format("%,.0f", amount);
    }

    private void updateExpenseChart(List<FinanceEntry> entries) {
        Map<String, Double> byCategory = new LinkedHashMap<>();
        for (FinanceEntry e : entries) {
            if (!"EXPENSE".equals(e.getType())) continue;
            String cat = (e.getCategory() == null || e.getCategory().isBlank()) ? "Other" : e.getCategory();
            byCategory.merge(cat, e.getAmount(), Double::sum);
        }
        javafx.collections.ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        byCategory.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(e -> data.add(new PieChart.Data(e.getKey(), e.getValue())));
        if (data.isEmpty()) data.add(new PieChart.Data("No expenses yet", 1));
        expenseChart.setData(data);
    }

    @FXML
    private void handleAddEntry() {
        Optional<FinanceEntry> result = FinanceDialogs.showAddEntryDialog();
        result.ifPresent(entry -> {
            User user = SessionManager.getCurrentUser();
            entry.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new FinanceDAO().insert(entry),
                    saved -> loadEntries(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not save entry: " + error.getMessage()).showAndWait()
            );
        });
    }

    @FXML
    private void handleExportJson() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> { JsonExporter.exportAll(user.getId()); return null; },
                v -> new Alert(Alert.AlertType.INFORMATION,
                        "Backup exported to momenta.json in the project folder.").showAndWait(),
                error -> new Alert(Alert.AlertType.ERROR, "Export failed: " + error.getMessage()).showAndWait()
        );
    }

    private void deleteEntry(FinanceEntry entry) {
        AsyncUtil.run(
                () -> { new FinanceDAO().delete(entry.getId()); return null; },
                v -> {
                    loadEntries();
                    UndoHelper.offerUndo("Entry moved to Trash.",
                            () -> new FinanceDAO().restore(entry.getId()), this::loadEntries);
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not delete entry: " + error.getMessage()).showAndWait()
        );
    }

    private class EntryCell extends ListCell<FinanceEntry> {
        @Override
        protected void updateItem(FinanceEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) { setGraphic(null); return; }

            VBox textBox = new VBox(2);
            Label category = new Label(entry.getCategory());
            category.setStyle("-fx-font-weight: bold;");
            Label date = new Label(entry.getDate());
            date.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            textBox.getChildren().addAll(category, date);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            boolean isIncome = "INCOME".equals(entry.getType());
            Label amount = new Label((isIncome ? "+" : "-") + "৳" + format(entry.getAmount()));
            amount.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isIncome ? "#22C55E;" : "#EF4444;"));

            Button deleteBtn = new Button("✕");
            deleteBtn.getStyleClass().add("danger-button");
            deleteBtn.setOnAction(e -> deleteEntry(entry));

            HBox row = new HBox(14, textBox, spacer, amount, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 8 4;");
            setGraphic(row);
        }
    }
}
