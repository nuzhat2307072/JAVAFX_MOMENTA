package com.momenta.controller;

import com.momenta.dao.UserDAO;
import com.momenta.dao.VaultDAO;
import com.momenta.model.User;
import com.momenta.model.VaultEntry;
import com.momenta.util.AsyncUtil;
import com.momenta.util.PasswordUtil;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class VaultController {

    @FXML private SidebarController sidebarController;

    @FXML private Button addEntryButton;
    @FXML private VBox lockedBox;
    @FXML private Label lockedTitleLabel;
    @FXML private Label lockedMessageLabel;
    @FXML private PasswordField pinField;
    @FXML private Button unlockButton;
    @FXML private Label lockedErrorLabel;
    @FXML private ScrollPane entriesScroll;
    @FXML private VBox entriesBox;

    private boolean settingUpNewPin;

    @FXML
    public void initialize() {
        sidebarController.setActive("vault");

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        settingUpNewPin = user.getVaultPinHash() == null || user.getVaultPinHash().isEmpty();
        if (settingUpNewPin) {
            lockedTitleLabel.setText("Set up your Vault PIN");
            lockedMessageLabel.setText("Choose a PIN (4+ characters) to protect this section. You'll need it every time you open the Vault in a new session.");
            unlockButton.setText("Set PIN");
        } else {
            lockedTitleLabel.setText("Vault is locked");
            lockedMessageLabel.setText("Enter your PIN to unlock.");
            unlockButton.setText("Unlock");
        }

        if (SessionManager.isVaultUnlocked()) {
            showUnlocked();
        } else {
            showLocked();
        }
    }

    private void showLocked() {
        lockedBox.setVisible(true);
        lockedBox.setManaged(true);
        entriesScroll.setVisible(false);
        entriesScroll.setManaged(false);
        addEntryButton.setVisible(false);
        addEntryButton.setManaged(false);
    }

    private void showUnlocked() {
        lockedBox.setVisible(false);
        lockedBox.setManaged(false);
        entriesScroll.setVisible(true);
        entriesScroll.setManaged(true);
        addEntryButton.setVisible(true);
        addEntryButton.setManaged(true);
        loadEntries();
    }

    @FXML
    private void handleUnlock() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        String pin = pinField.getText() == null ? "" : pinField.getText();

        if (settingUpNewPin) {
            if (pin.length() < 4) {
                lockedErrorLabel.setText("PIN must be at least 4 characters.");
                return;
            }
            String hash = PasswordUtil.hash(pin);
            AsyncUtil.run(
                    () -> { new UserDAO().updateVaultPinHash(user.getId(), hash); return null; },
                    v -> {
                        user.setVaultPinHash(hash);
                        SessionManager.setVaultUnlocked(true);
                        showUnlocked();
                    },
                    error -> lockedErrorLabel.setText("Could not set PIN: " + error.getMessage())
            );
        } else {
            if (PasswordUtil.matches(pin, user.getVaultPinHash())) {
                lockedErrorLabel.setText("");
                SessionManager.setVaultUnlocked(true);
                showUnlocked();
            } else {
                lockedErrorLabel.setText("Incorrect PIN.");
            }
        }
    }

    private void loadEntries() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new VaultDAO().getAllByUser(user.getId()),
                (List<VaultEntry> entries) -> {
                    entriesBox.getChildren().clear();
                    if (entries.isEmpty()) {
                        Label empty = new Label("Nothing saved yet.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        entriesBox.getChildren().add(empty);
                    } else {
                        for (VaultEntry entry : entries) entriesBox.getChildren().add(buildCard(entry));
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load vault entries: " + error.getMessage()).showAndWait()
        );
    }

    private VBox buildCard(VaultEntry entry) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label title = new Label(entry.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F2C4C;");
        Label content = new Label(entry.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 12px;");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("secondary-button");
        editBtn.setOnAction(e -> {
            Optional<VaultEntry> result = VaultDialogs.showEditDialog(entry);
            result.ifPresent(updated -> AsyncUtil.run(
                    () -> { new VaultDAO().update(updated); return null; },
                    v -> loadEntries(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not save: " + error.getMessage()).showAndWait()
            ));
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> AsyncUtil.run(
                () -> { new VaultDAO().delete(entry.getId()); return null; },
                v -> {
                    loadEntries();
                    com.momenta.util.UndoHelper.offerUndo("\"" + entry.getTitle() + "\" moved to Trash.",
                            () -> new VaultDAO().restore(entry.getId()), this::loadEntries);
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not delete: " + error.getMessage()).showAndWait()
        ));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, editBtn, spacer, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, content, actions);
        return card;
    }

    @FXML
    private void handleAddEntry() {
        Optional<VaultEntry> result = VaultDialogs.showAddDialog();
        result.ifPresent(entry -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            entry.setUserId(user.getId());
            AsyncUtil.run(
                    () -> new VaultDAO().insert(entry),
                    saved -> loadEntries(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not save entry: " + error.getMessage()).showAndWait()
            );
        });
    }
}
