package com.antigravity.sentiment.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HistoryData {
    private final StringProperty date;
    private final StringProperty up;
    private final StringProperty sideways;
    private final StringProperty down;
    private final String sourceFilePath; // Path to the source file for detailed analysis

    public HistoryData(String date, String up, String sideways, String down, String sourceFilePath) {
        this.date = new SimpleStringProperty(date);
        this.up = new SimpleStringProperty(up);
        this.sideways = new SimpleStringProperty(sideways);
        this.down = new SimpleStringProperty(down);
        this.sourceFilePath = sourceFilePath;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty upProperty() {
        return up;
    }

    public StringProperty sidewaysProperty() {
        return sideways;
    }

    public StringProperty downProperty() {
        return down;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }
}
