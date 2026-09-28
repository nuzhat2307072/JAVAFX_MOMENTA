package com.momenta.model;

import javafx.beans.property.*;

/** One day's health entry for a user: sleep, water, exercise, meals, mood, weight, steps and notes. */
public class HealthLog {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final DoubleProperty sleepHours = new SimpleDoubleProperty();
    private final IntegerProperty waterGlasses = new SimpleIntegerProperty();
    private final IntegerProperty exerciseMinutes = new SimpleIntegerProperty();
    /** Comma-separated meal codes eaten today, e.g. "BREAKFAST,LUNCH,DINNER,SNACKS". */
    private final StringProperty meals = new SimpleStringProperty("");
    private final StringProperty mood = new SimpleStringProperty("");
    private final DoubleProperty weightKg = new SimpleDoubleProperty();
    private final IntegerProperty steps = new SimpleIntegerProperty();
    private final StringProperty notes = new SimpleStringProperty("");

    public HealthLog() {}

    public HealthLog(int id, int userId, String date, double sleepHours, int waterGlasses,
                      int exerciseMinutes, String meals, String mood,
                      double weightKg, int steps, String notes) {
        setId(id); setUserId(userId); setDate(date);
        setSleepHours(sleepHours); setWaterGlasses(waterGlasses);
        setExerciseMinutes(exerciseMinutes);
        setMeals(meals == null ? "" : meals);
        setMood(mood == null ? "" : mood);
        setWeightKg(weightKg);
        setSteps(steps);
        setNotes(notes == null ? "" : notes);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getDate() { return date.get(); }
    public void setDate(String v) { date.set(v); }

    public double getSleepHours() { return sleepHours.get(); }
    public void setSleepHours(double v) { sleepHours.set(v); }

    public int getWaterGlasses() { return waterGlasses.get(); }
    public void setWaterGlasses(int v) { waterGlasses.set(v); }

    public int getExerciseMinutes() { return exerciseMinutes.get(); }
    public void setExerciseMinutes(int v) { exerciseMinutes.set(v); }

    public String getMeals() { return meals.get(); }
    public void setMeals(String v) { meals.set(v); }

    public boolean hasMeal(String code) {
        String v = getMeals();
        if (v == null || v.isBlank()) return false;
        for (String part : v.split(",")) if (part.equals(code)) return true;
        return false;
    }

    /** Number of the four tracked meals (breakfast/lunch/dinner/snacks) logged today. */
    public int getMealsCount() {
        String v = getMeals();
        if (v == null || v.isBlank()) return 0;
        return (int) java.util.Arrays.stream(v.split(",")).filter(s -> !s.isBlank()).count();
    }

    public String getMood() { return mood.get(); }
    public void setMood(String v) { mood.set(v); }

    public double getWeightKg() { return weightKg.get(); }
    public void setWeightKg(double v) { weightKg.set(v); }

    public int getSteps() { return steps.get(); }
    public void setSteps(int v) { steps.set(v); }

    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v); }

    /** Maps a stored mood code to a display emoji; blank/unknown falls back to a dash. */
    public static String moodEmoji(String mood) {
        if (mood == null) return "—";
        return switch (mood) {
            case "GREAT" -> "😄";
            case "GOOD" -> "🙂";
            case "OKAY" -> "😐";
            case "LOW" -> "🙁";
            case "BAD" -> "😢";
            default -> "—";
        };
    }
}
