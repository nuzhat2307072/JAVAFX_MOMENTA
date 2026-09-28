package com.momenta.controller;

import com.momenta.dao.LifeEntryDAO;
import com.momenta.model.User;
import com.momenta.util.AsyncUtil;
import com.momenta.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

/**
 * Handles all seven Life Areas modules from one screen. Each module is the
 * same underlying LifeEntry shape (see that class), rendered by the shared
 * LifeEntryCardFactory with whichever mix of progress bar / done checkbox /
 * date actually applies to it.
 */
public class LifeAreasController {

    @FXML private SidebarController sidebarController;

    @FXML private VBox learningBox;
    @FXML private VBox careerBox;
    @FXML private VBox relationshipBox;
    @FXML private VBox homeBox;
    @FXML private VBox travelBox;
    @FXML private VBox hobbyBox;
    @FXML private VBox journalBox;

    @FXML
    public void initialize() {
        sidebarController.setActive("life");
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        int userId = user.getId();

        LifeEntryCardFactory.loadModule(learningBox, userId, "LEARNING", true, false, false);
        LifeEntryCardFactory.loadModule(careerBox, userId, "CAREER", true, false, false);
        LifeEntryCardFactory.loadModule(relationshipBox, userId, "RELATIONSHIP", false, false, true);
        LifeEntryCardFactory.loadModule(homeBox, userId, "HOME", false, true, false);
        LifeEntryCardFactory.loadModule(travelBox, userId, "TRAVEL", false, false, true);
        LifeEntryCardFactory.loadModule(hobbyBox, userId, "HOBBY", true, false, false);
        LifeEntryCardFactory.loadModule(journalBox, userId, "JOURNAL", false, false, true);
    }

    // ---------- Add dialogs, one per module ----------

    @FXML
    private void handleAddLearning() {
        addEntry("LEARNING", learningBox, true, false, false,
                "Add Learning Item", "Subject / Topic", "Status (e.g. In Progress)", false, "Study notes");
    }

    @FXML
    private void handleAddCareer() {
        addEntry("CAREER", careerBox, true, false, false,
                "Add Career Item", "Item", "Category (Skill/Application/Interview/Certification)", false, "Notes");
    }

    @FXML
    private void handleAddRelationship() {
        addEntry("RELATIONSHIP", relationshipBox, false, false, true,
                "Add Contact", "Name", "Relationship (e.g. Friend, Colleague)", true, "Notes / gift ideas");
    }

    @FXML
    private void handleAddHome() {
        addEntry("HOME", homeBox, false, true, false,
                "Add Home Item", "Item", "Type (Shopping/Chore/Bill/Maintenance)", false, "Notes");
    }

    @FXML
    private void handleAddTravel() {
        addEntry("TRAVEL", travelBox, false, false, true,
                "Add Trip", "Trip / Destination", "Destination details", true, "Itinerary / budget / packing notes");
    }

    @FXML
    private void handleAddHobby() {
        addEntry("HOBBY", hobbyBox, true, false, false,
                "Add Hobby / Project", "Project", "Category", false, "Notes");
    }

    @FXML
    private void handleAddJournal() {
        addEntry("JOURNAL", journalBox, false, false, true,
                "New Journal Entry", "Title", "Type (Daily/Gratitude/Reflection)", true, "Your entry");
    }

    private void addEntry(String module, VBox box, boolean showProgress, boolean showDone, boolean showDate,
                           String dialogTitle, String titlePrompt, String subtitlePrompt,
                           boolean dialogShowDate, String notesPrompt) {
        LifeEntryDialogs.showAddDialog(dialogTitle, titlePrompt, subtitlePrompt, dialogShowDate, notesPrompt)
                .ifPresent(entry -> {
                    User user = SessionManager.getCurrentUser();
                    if (user == null) return;
                    entry.setUserId(user.getId());
                    entry.setModule(module);
                    AsyncUtil.run(
                            () -> new LifeEntryDAO().insert(entry),
                            saved -> LifeEntryCardFactory.loadModule(box, user.getId(), module, showProgress, showDone, showDate),
                            error -> new Alert(Alert.AlertType.ERROR, "Could not add: " + error.getMessage()).showAndWait()
                    );
                });
    }
}
