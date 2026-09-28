package com.momenta.model;

import javafx.beans.property.*;

/**
 * One entry in a "Life Area" list. The same shape is reused across seven
 * modules (Learning, Career, Relationships, Life &amp; Home, Travel, Hobbies,
 * Journal); which fields matter depends on the module:
 *
 *   LEARNING      title=subject/topic, subtitle=status, progress=0-100, notes=study notes
 *   CAREER        title=item, subtitle=category (Skill/Application/Interview/Cert), progress, notes
 *   RELATIONSHIP  title=name, subtitle=relationship, entryDate=important date, notes
 *   HOME          title=item, subtitle=type (Shopping/Chore/Bill/Maintenance), done=checkbox, notes
 *   TRAVEL        title=trip, subtitle=destination, entryDate=travel date, notes=itinerary/budget
 *   HOBBY         title=project, subtitle=category, progress=0-100, notes
 *   JOURNAL       title=entry title, subtitle=type (Daily/Gratitude/Reflection), entryDate, notes=content
 */
public class LifeEntry {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty module = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty("");
    private final StringProperty entryDate = new SimpleStringProperty("");
    private final IntegerProperty progress = new SimpleIntegerProperty();
    private final BooleanProperty done = new SimpleBooleanProperty();
    private final StringProperty notes = new SimpleStringProperty("");

    public LifeEntry() {}

    public LifeEntry(int id, int userId, String module, String title, String subtitle,
                      String entryDate, int progress, boolean done, String notes) {
        setId(id); setUserId(userId); setModule(module); setTitle(title);
        setSubtitle(subtitle == null ? "" : subtitle);
        setEntryDate(entryDate == null ? "" : entryDate);
        setProgress(progress); setDone(done);
        setNotes(notes == null ? "" : notes);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getModule() { return module.get(); }
    public void setModule(String v) { module.set(v); }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public String getSubtitle() { return subtitle.get(); }
    public void setSubtitle(String v) { subtitle.set(v); }

    public String getEntryDate() { return entryDate.get(); }
    public void setEntryDate(String v) { entryDate.set(v); }

    public int getProgress() { return progress.get(); }
    public void setProgress(int v) { progress.set(v); }
    public IntegerProperty progressProperty() { return progress; }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean v) { done.set(v); }
    public BooleanProperty doneProperty() { return done; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v); }
}
