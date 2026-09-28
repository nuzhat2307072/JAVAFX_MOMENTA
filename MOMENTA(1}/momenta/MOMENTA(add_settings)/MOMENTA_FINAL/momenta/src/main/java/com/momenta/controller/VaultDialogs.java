package com.momenta.controller;

import com.momenta.model.VaultEntry;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class VaultDialogs {

    public static Optional<VaultEntry> showAddDialog() { return showDialog(null); }
    public static Optional<VaultEntry> showEditDialog(VaultEntry existing) { return showDialog(existing); }

    private static Optional<VaultEntry> showDialog(VaultEntry existing) {
        boolean editing = existing != null;
        Dialog<VaultEntry> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Edit Vault Entry" : "New Vault Entry");
        dialog.getDialogPane().getStylesheets().add(
                VaultDialogs.class.getResource("/com/momenta/css/styles.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType(editing ? "Save" : "Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField(editing ? existing.getTitle() : "");
        titleField.setPromptText("Title");

        TextArea contentArea = new TextArea(editing ? existing.getContent() : "");
        contentArea.setPromptText("Content — notes, reference info, important dates...");
        contentArea.setPrefRowCount(6);
        contentArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Title"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content"), 0, 1);
        grid.add(contentArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        titleField.requestFocus();

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(titleField.textProperty().isEmpty());

        dialog.setResultConverter(button -> {
            if (button != saveButtonType) return null;
            VaultEntry entry = editing ? existing : new VaultEntry();
            entry.setTitle(titleField.getText().trim());
            entry.setContent(contentArea.getText());
            return entry;
        });

        return dialog.showAndWait();
    }
}
