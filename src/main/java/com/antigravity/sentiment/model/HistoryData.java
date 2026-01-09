package com.antigravity.sentiment.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HistoryData {
    private final StringProperty date;
    private final StringProperty up;
    private final StringProperty sideways;
    private final StringProperty down;
    private final String sourceFilePath; // Path to the source file for detailed analysis

    private final java.time.LocalDate sortableDate;

    public HistoryData(String date, String up, String sideways, String down, String sourceFilePath) {
        this.date = new SimpleStringProperty(date);
        this.up = new SimpleStringProperty(up);
        this.sideways = new SimpleStringProperty(sideways);
        this.down = new SimpleStringProperty(down);
        this.sourceFilePath = sourceFilePath;
        this.sortableDate = parseDate(date);
    }

    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return java.time.LocalDate.MIN;
        try {
            // Try ISO format (yyyy-MM-dd) first as it is standard in new files
            try {
                return java.time.LocalDate.parse(dateStr);
            } catch (Exception ignored) {
                // Formatting specific for German short date d.M.yy
            }

            // Remove weekday prefix if present (e.g. "Mo 5.1.26" -> "5.1.26")
            String cleanDate = dateStr;
            int firstDigit = -1;
            for (int i = 0; i < dateStr.length(); i++) {
                if (Character.isDigit(dateStr.charAt(i))) {
                    firstDigit = i;
                    break;
                }
            }
            if (firstDigit > 0) {
                cleanDate = dateStr.substring(firstDigit);
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d.M.yy");
            return java.time.LocalDate.parse(cleanDate, formatter);
        } catch (Exception e) {
            // Fallback for safety
            return java.time.LocalDate.MIN;
        }
    }

    public java.time.LocalDate getSortableDate() {
        return sortableDate;
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
