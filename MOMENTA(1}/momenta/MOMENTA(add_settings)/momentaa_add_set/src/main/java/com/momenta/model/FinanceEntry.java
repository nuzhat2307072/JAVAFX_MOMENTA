package com.momenta.model;

import javafx.beans.property.*;

public class FinanceEntry {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();     // INCOME / EXPENSE
    private final StringProperty category = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final StringProperty date = new SimpleStringProperty();

    public FinanceEntry() {}

    public FinanceEntry(int id, int userId, String type, String category, double amount, String date) {
        setId(id); setUserId(userId); setType(type); setCategory(category); setAmount(amount); setDate(date);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public String getType() { return type.get(); }
    public void setType(String v) { type.set(v); }
    public StringProperty typeProperty() { return type; }

    public String getCategory() { return category.get(); }
    public void setCategory(String v) { category.set(v); }
    public StringProperty categoryProperty() { return category; }

    public double getAmount() { return amount.get(); }
    public void setAmount(double v) { amount.set(v); }
    public DoubleProperty amountProperty() { return amount; }

    public String getDate() { return date.get(); }
    public void setDate(String v) { date.set(v); }
    public StringProperty dateProperty() { return date; }
}
