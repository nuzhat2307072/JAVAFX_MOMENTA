package com.momenta.controller;

import com.momenta.dao.LifeEntryDAO;
import com.momenta.model.LifeEntry;
import com.momenta.util.AsyncUtil;
import com.momenta.util.UndoHelper;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Renders a VBox of LifeEntry cards for one module (Learning, Career, ..., or
 * Health's Workouts/Nutrition). Shared by LifeAreasController and
 * HealthController so the card UI (progress bar / done checkbox / date /
 * delete-with-undo) stays identical everywhere it's used, instead of being
 * copy-pasted per screen.
 */
public class LifeEntryCardFactory {

    public static void loadModule(VBox box, int userId, String module,
                                   boolean showProgress, boolean showDone, boolean showDate) {
        AsyncUtil.run(
                () -> new LifeEntryDAO().getAllByUserAndModule(userId, module),
                (List<LifeEntry> entries) -> {
                    box.getChildren().clear();
                    if (entries.isEmpty()) {
                        Label empty = new Label("Nothing here yet. Click \"+ Add\" to create the first entry.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        box.getChildren().add(empty);
                    } else {
                        for (LifeEntry e : entries) {
                            box.getChildren().add(buildCard(e, box, userId, module, showProgress, showDone, showDate));
                        }
                    }
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load data: " + error.getMessage()).showAndWait()
        );
    }

    private static VBox buildCard(LifeEntry entry, VBox box, int userId, String module,
                                   boolean showProgress, boolean showDone, boolean showDate) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        if (showDone) {
            CheckBox doneBox = new CheckBox();
            doneBox.setSelected(entry.isDone());
            doneBox.selectedProperty().addListener((obs, oldV, newV) -> AsyncUtil.run(
                    () -> { new LifeEntryDAO().toggleDone(entry.getId(), newV); return null; },
                    v -> { },
                    error -> new Alert(Alert.AlertType.ERROR, "Could not update: " + error.getMessage()).showAndWait()
            ));
            header.getChildren().add(doneBox);
        }

        Label title = new Label(entry.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0F2C4C;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteEntry(entry, box, userId, module, showProgress, showDone, showDate));
        header.getChildren().addAll(title, spacer, deleteBtn);

        card.getChildren().add(header);

        String metaLine = (entry.getSubtitle() == null ? "" : entry.getSubtitle())
                + (showDate && entry.getEntryDate() != null && !entry.getEntryDate().isBlank() ? "  ·  " + entry.getEntryDate() : "");
        if (!metaLine.isBlank()) {
            Label meta = new Label(metaLine);
            meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            card.getChildren().add(meta);
        }

        if (entry.getNotes() != null && !entry.getNotes().isBlank()) {
            Label notes = new Label(entry.getNotes());
            notes.setWrapText(true);
            notes.setStyle("-fx-font-size: 12px;");
            card.getChildren().add(notes);
        }

        if (showProgress) {
            ProgressBar bar = new ProgressBar(entry.getProgress() / 100.0);
            bar.setPrefWidth(300);
            Label percent = new Label(entry.getProgress() + "%");
            Button minus = new Button("－10%");
            minus.getStyleClass().add("secondary-button");
            minus.setOnAction(e -> adjustProgress(entry, box, userId, module, -10, showProgress, showDone, showDate));
            Button plus = new Button("＋10%");
            plus.getStyleClass().add("secondary-button");
            plus.setOnAction(e -> adjustProgress(entry, box, userId, module, 10, showProgress, showDone, showDate));
            HBox progressRow = new HBox(10, bar, percent, minus, plus);
            progressRow.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(progressRow);
        }

        return card;
    }

    private static void adjustProgress(LifeEntry entry, VBox box, int userId, String module, int delta,
                                        boolean showProgress, boolean showDone, boolean showDate) {
        int newProgress = Math.max(0, Math.min(100, entry.getProgress() + delta));
        AsyncUtil.run(
                () -> { new LifeEntryDAO().updateProgress(entry.getId(), newProgress); return null; },
                v -> loadModule(box, userId, module, showProgress, showDone, showDate),
                error -> new Alert(Alert.AlertType.ERROR, "Could not update progress: " + error.getMessage()).showAndWait()
        );
    }

    private static void deleteEntry(LifeEntry entry, VBox box, int userId, String module,
                                     boolean showProgress, boolean showDone, boolean showDate) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Move \"" + entry.getTitle() + "\" to Trash?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AsyncUtil.run(
                        () -> { new LifeEntryDAO().delete(entry.getId()); return null; },
                        v -> {
                            loadModule(box, userId, module, showProgress, showDone, showDate);
                            UndoHelper.offerUndo("\"" + entry.getTitle() + "\" moved to Trash.",
                                    () -> new LifeEntryDAO().restore(entry.getId()),
                                    () -> loadModule(box, userId, module, showProgress, showDone, showDate));
                        },
                        error -> new Alert(Alert.AlertType.ERROR, "Could not delete: " + error.getMessage()).showAndWait()
                );
            }
        });
    }
}
