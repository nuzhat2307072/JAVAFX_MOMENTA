package com.momenta.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Undo (paired with Trash): every delete in the app already moves the item
 * to Trash rather than removing it, so this just offers an immediate
 * shortcut back — restore right now, without having to go find it in
 * Trash later. Declining just leaves it in Trash as normal.
 *
 * Deliberately scoped to "undo a delete" only — full multi-step Undo/Redo
 * (edits, status changes, priority changes, ...) would need a command-
 * pattern action stack and is out of scope for this pass; see the README.
 */
public class UndoHelper {

    public static void offerUndo(String message, AsyncUtil.RunnableThrows restoreAction, Runnable onRestored) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Deleted");
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType undoButton = new ButtonType("Undo");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(undoButton, okButton);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == undoButton) {
            AsyncUtil.runVoid(restoreAction, onRestored, error ->
                    new Alert(Alert.AlertType.ERROR, "Could not undo: " + error.getMessage()).showAndWait());
        }
    }
}
