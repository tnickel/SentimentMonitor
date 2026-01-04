package com.antigravity.sentiment.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ForecastData {
    private final StringProperty asset;
    private final String assetPath; // Store path for history loading

    // Day 1
    private final StringProperty date1;
    private final StringProperty up1;
    private final StringProperty sideways1;
    private final StringProperty down1;

    // Day 2
    private final StringProperty date2;
    private final StringProperty up2;
    private final StringProperty sideways2;
    private final StringProperty down2;

    // 6 Monate
    private final StringProperty date6m;
    private final StringProperty up6m;
    private final StringProperty sideways6m;
    private final StringProperty down6m;

    public ForecastData(String asset, String assetPath,
            String date1, String up1, String sideways1, String down1,
            String date2, String up2, String sideways2, String down2,
            String date6m, String up6m, String sideways6m, String down6m) {
        this.asset = new SimpleStringProperty(asset);
        this.assetPath = assetPath;
        this.date1 = new SimpleStringProperty(date1);
        this.up1 = new SimpleStringProperty(up1);
        this.sideways1 = new SimpleStringProperty(sideways1);
        this.down1 = new SimpleStringProperty(down1);

        this.date2 = new SimpleStringProperty(date2);
        this.up2 = new SimpleStringProperty(up2);
        this.sideways2 = new SimpleStringProperty(sideways2);
        this.down2 = new SimpleStringProperty(down2);

        this.date6m = new SimpleStringProperty(date6m);
        this.up6m = new SimpleStringProperty(up6m);
        this.sideways6m = new SimpleStringProperty(sideways6m);
        this.down6m = new SimpleStringProperty(down6m);
    }

    // Getters for properties (needed for TableView factories)
    public StringProperty assetProperty() {
        return asset;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public StringProperty date1Property() {
        return date1;
    }

    public StringProperty up1Property() {
        return up1;
    }

    public StringProperty sideways1Property() {
        return sideways1;
    }

    public StringProperty down1Property() {
        return down1;
    }

    public StringProperty date2Property() {
        return date2;
    }

    public StringProperty up2Property() {
        return up2;
    }

    public StringProperty sideways2Property() {
        return sideways2;
    }

    public StringProperty down2Property() {
        return down2;
    }

    public StringProperty date6mProperty() {
        return date6m;
    }

    public StringProperty up6mProperty() {
        return up6m;
    }

    public StringProperty sideways6mProperty() {
        return sideways6m;
    }

    public StringProperty down6mProperty() {
        return down6m;
    }

    // Standard Getters
    public String getAsset() {
        return asset.get();
    }
}
