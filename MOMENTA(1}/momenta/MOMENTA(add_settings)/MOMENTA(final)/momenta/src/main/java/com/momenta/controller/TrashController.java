package com.momenta.controller;

import com.momenta.dao.TrashDAO;
import com.momenta.model.TrashItem;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.BackupManager;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class TrashController {

    @FXML private SidebarController sidebarController;
    @FXML private VBox trashBox;
    @FXML private Button emptyTrashButton;

    private List<TrashItem> currentItems = List.of();

    @FXML
    public void initialize() {
        sidebarController.setActive("trash");
        loadTrash();
    }

    private void loadTrash() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new TrashDAO().getAllTrash(user.getId()),
                (List<TrashItem> items) -> {
                    currentItems = items;
                    trashBox.getChildren().clear();
                    emptyTrashButton.setDisable(items.isEmpty());
                    if (items.isEmpty()) {
                        Label empty = new Label("Trash is empty.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        trashBox.getChildren().add(empty);
                    } else {
                        for (TrashItem item : items) trashBox.getChildren().add(buildRow(item));
                    }
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load Trash: " + error.getMessage()).showAndWait()
        );
    }

    private HBox buildRow(TrashItem item) {
        Label icon = new Label(item.typeIcon());
        icon.setStyle("-fx-font-size: 16px;");

        VBox textBox = new VBox(2);
        Label label = new Label(item.getLabel());
        label.setStyle("-fx-font-weight: bold;");
        label.setWrapText(true);
        Label meta = new Label(item.typeLabel() + " · deleted " + item.getDeletedAt());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        textBox.getChildren().addAll(label, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button restoreBtn = new Button("Restore");
        restoreBtn.getStyleClass().add("secondary-button");
        restoreBtn.setOnAction(e -> AsyncUtil.run(
                () -> { new TrashDAO().restore(item); return null; },
                v -> loadTrash(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not restore: " + error.getMessage()).showAndWait()
        ));

        Button deleteForeverBtn = new Button("Delete Forever");
        deleteForeverBtn.getStyleClass().add("danger-button");
        deleteForeverBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Permanently delete \"" + item.getLabel() + "\"? This cannot be undone.", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    AsyncUtil.run(
                            () -> { new TrashDAO().permanentlyDelete(item); return null; },
                            v -> loadTrash(),
                            error -> new Alert(Alert.AlertType.ERROR, "Could not delete: " + error.getMessage()).showAndWait()
                    );
                }
            });
        });

        HBox row = new HBox(10, icon, textBox, spacer, restoreBtn, deleteForeverBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        return row;
    }

    @FXML
    private void handleEmptyTrash() {
        if (currentItems.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Permanently delete all " + currentItems.size() + " item(s) in Trash? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;
            List<TrashItem> toDelete = currentItems;
            AsyncUtil.run(
                    () -> {
                        TrashDAO dao = new TrashDAO();
                        for (TrashItem item : toDelete) dao.permanentlyDelete(item);
                        return null;
                    },
                    v -> loadTrash(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not empty Trash: " + error.getMessage()).showAndWait()
            );
        });
    }
}
