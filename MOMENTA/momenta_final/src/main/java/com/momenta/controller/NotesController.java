package com.momenta.controller;

import com.momenta.dao.NoteDAO;
import com.momenta.dao.TaskDAO;
import com.momenta.model.Note;
import com.momenta.model.Task;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.BackupManager;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class NotesController {

    @FXML private SidebarController sidebarController;
    @FXML private TextArea newNoteArea;
    @FXML private VBox notesBox;

    @FXML
    public void initialize() {
        sidebarController.setActive("notes");
        loadNotes();
    }

    private void loadNotes() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new NoteDAO().getAllByUser(user.getId()),
                (List<Note> notes) -> {
                    notesBox.getChildren().clear();
                    if (notes.isEmpty()) {
                        Label empty = new Label("Nothing dumped yet — jot down whatever's on your mind above.");
                        empty.setStyle("-fx-text-fill: #6B7280;");
                        notesBox.getChildren().add(empty);
                    } else {
                        for (Note n : notes) notesBox.getChildren().add(buildNoteCard(n));
                    }
                    BackupManager.autoBackup(user.getId());
                },
                error -> new Alert(Alert.AlertType.ERROR, "Could not load notes: " + error.getMessage()).showAndWait()
        );
    }

    private VBox buildNoteCard(Note note) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        Label content = new Label(note.getContent());
        content.setWrapText(true);

        Label timestamp = new Label(note.getCreatedAt());
        timestamp.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        Button convertBtn = new Button("→ Convert to Task");
        convertBtn.getStyleClass().add("secondary-button");
        convertBtn.setOnAction(e -> convertToTask(note));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteNote(note));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, convertBtn, spacer, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(content, timestamp, actions);
        return card;
    }

    @FXML
    private void handleAddNote() {
        String text = newNoteArea.getText();
        if (text == null || text.trim().isEmpty()) return;
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        AsyncUtil.run(
                () -> new NoteDAO().insert(new Note(0, user.getId(), text.trim(), LocalDateTime.now().toString())),
                saved -> { newNoteArea.clear(); loadNotes(); },
                error -> new Alert(Alert.AlertType.ERROR, "Could not save note: " + error.getMessage()).showAndWait()
        );
    }

    private void convertToTask(Note note) {
        Task draft = new Task();
        draft.setTitle(note.getContent().length() > 60 ? note.getContent().substring(0, 60) + "…" : note.getContent());
        draft.setDescription(note.getContent());
        draft.setPriority(3);
        draft.setProgress(0);
        draft.setStatus("PENDING");
        draft.setDeadline("");
        draft.setCategory("");
        draft.setRecurrence("NONE");

        Optional<Task> result = TaskDialogs.showEditTaskDialog(draft);
        result.ifPresent(task -> {
            User user = SessionManager.getCurrentUser();
            if (user == null) return;
            task.setUserId(user.getId());
            AsyncUtil.run(
                    () -> {
                        new TaskDAO().insert(task);
                        new NoteDAO().delete(note.getId());
                        return null;
                    },
                    v -> loadNotes(),
                    error -> new Alert(Alert.AlertType.ERROR, "Could not convert note: " + error.getMessage()).showAndWait()
            );
        });
    }

    private void deleteNote(Note note) {
        AsyncUtil.run(
                () -> { new NoteDAO().delete(note.getId()); return null; },
                v -> loadNotes(),
                error -> new Alert(Alert.AlertType.ERROR, "Could not delete note: " + error.getMessage()).showAndWait()
        );
    }
}
