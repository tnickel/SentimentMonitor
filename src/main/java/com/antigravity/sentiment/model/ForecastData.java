package com.antigravity.sentiment.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ForecastData {
    private final StringProperty asset;
    private final String assetPath;

    // New Table Fields
    private final StringProperty signal; // "STEIGT", "FAELLT", "SEITWAERTS", "PANIC", "NEUTRAL"
    private final StringProperty date; // Single date
    private final StringProperty sentiment; // "42% L / 58% S"
    private final StringProperty vix; // "15"
    private final StringProperty consensus; // "2 von 3"
    private final StringProperty indicators; // "RSI=36, ATR=0.0007"

    // Values useful for sorting/filtering but maybe not direct display
    private final StringProperty upProb;
    private final StringProperty sideProb;
    private final StringProperty downProb;

    private final StringProperty explanation;

    public ForecastData(String asset, String assetPath, String date,
            String signal, String sentiment, String vix,
            String consensus, String indicators,
            String upProb, String sideProb, String downProb, String explanation) {
        this.asset = new SimpleStringProperty(asset);
        this.assetPath = assetPath;
        this.date = new SimpleStringProperty(date);
        this.signal = new SimpleStringProperty(signal);
        this.sentiment = new SimpleStringProperty(sentiment);
        this.vix = new SimpleStringProperty(vix);
        this.consensus = new SimpleStringProperty(consensus);
        this.indicators = new SimpleStringProperty(indicators);
        this.upProb = new SimpleStringProperty(upProb);
        this.sideProb = new SimpleStringProperty(sideProb);
        this.downProb = new SimpleStringProperty(downProb);
        this.explanation = new SimpleStringProperty(explanation);
    }

    // Getters for properties
    public StringProperty assetProperty() {
        return asset;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty signalProperty() {
        return signal;
    }

    public StringProperty sentimentProperty() {
        return sentiment;
    }

    public StringProperty vixProperty() {
        return vix;
    }

    public StringProperty consensusProperty() {
        return consensus;
    }

    public StringProperty indicatorsProperty() {
        return indicators;
    }

    public StringProperty upProbProperty() {
        return upProb;
    }

    public StringProperty sideProbProperty() {
        return sideProb;
    }

    public StringProperty downProbProperty() {
        return downProb;
    }

    public String getAsset() {
        return asset.get();
    }

    public String getSignal() {
        return signal.get();
    }

    public StringProperty explanationProperty() {
        return explanation;
    }

    public String getExplanation() {
        return explanation.get();
    }
}
