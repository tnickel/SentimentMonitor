package com.antigravity.sentiment.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the complete analysis data from a forecast file.
 * updated for new properties (FXSSI, Derivation, Rationales).
 */
public class FullAnalysisData {

    // SECTION 0: Data Basis
    private String date;
    private String fxssiLong; // e.g. "41%"
    private String fxssiShort; // e.g. "59%"
    private String highImpactEvents; // Text summary

    // SECTION 1: Profile (Probabilities)
    private String upProbability;
    private String sidewaysProbability;
    private String downProbability;

    // SECTION 2 & 3: Derivation / Robot Control
    private String instrument;
    private String bias; // from result bias
    private String csvSignal;
    private String riskLevel;
    private String derivationText; // Combined text for display

    // Calculation explanation
    private String probabilityCalculation;

    // Analyst Consensus
    private String analystConsensus;

    // New Table Columns
    private String vix;
    private String rsi;
    private String atr;
    private String consensusNumbers;
    private String panicStatus;

    // SECTION 4: Rationales
    // Map title -> content
    private Map<String, String> rationales;

    // Legacy / Fallback
    private String rawContent;

    public FullAnalysisData() {
        this.date = "";
        this.fxssiLong = "50%";
        this.fxssiShort = "50%";
        this.highImpactEvents = "";

        this.upProbability = "0%";
        this.sidewaysProbability = "0%";
        this.downProbability = "0%";

        this.instrument = "";
        this.bias = "NEUTRAL";
        this.csvSignal = "NEUTRAL";
        this.riskLevel = "MEDIUM";
        this.derivationText = "";
        this.probabilityCalculation = "Keine Details verfügbar.";
        this.analystConsensus = "Kein Analysten-Konsens gefunden.";

        this.vix = "-";
        this.rsi = "-";
        this.atr = "-";
        this.consensusNumbers = "-";
        this.panicStatus = "Sicher"; // Default

        this.rationales = new LinkedHashMap<>();
        this.rawContent = "";
    }

    // Getters and Setters

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFxssiLong() {
        return fxssiLong;
    }

    public void setFxssiLong(String fxssiLong) {
        this.fxssiLong = fxssiLong;
    }

    public String getFxssiShort() {
        return fxssiShort;
    }

    public void setFxssiShort(String fxssiShort) {
        this.fxssiShort = fxssiShort;
    }

    public String getHighImpactEvents() {
        return highImpactEvents;
    }

    public void setHighImpactEvents(String event) {
        this.highImpactEvents = event;
    }

    public String getUpProbability() {
        return upProbability;
    }

    public void setUpProbability(String p) {
        this.upProbability = p;
    }

    public String getSidewaysProbability() {
        return sidewaysProbability;
    }

    public void setSidewaysProbability(String p) {
        this.sidewaysProbability = p;
    }

    public String getDownProbability() {
        return downProbability;
    }

    public void setDownProbability(String p) {
        this.downProbability = p;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String i) {
        this.instrument = i;
    }

    public String getBias() {
        return bias;
    }

    public void setBias(String bias) {
        this.bias = bias;
    }

    public String getCsvSignal() {
        return csvSignal;
    }

    public void setCsvSignal(String s) {
        this.csvSignal = s;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String r) {
        this.riskLevel = r;
    }

    public String getDerivationText() {
        return derivationText;
    }

    public void setDerivationText(String t) {
        this.derivationText = t;
    }

    public String getProbabilityCalculation() {
        return probabilityCalculation;
    }

    public void setProbabilityCalculation(String c) {
        this.probabilityCalculation = c;
    }

    public String getAnalystConsensus() {
        return analystConsensus;
    }

    public void setAnalystConsensus(String c) {
        this.analystConsensus = c;
    }

    public String getVix() {
        return vix;
    }

    public void setVix(String v) {
        this.vix = v;
    }

    public String getRsi() {
        return rsi;
    }

    public void setRsi(String r) {
        this.rsi = r;
    }

    public String getAtr() {
        return atr;
    }

    public void setAtr(String a) {
        this.atr = a;
    }

    public String getConsensusNumbers() {
        return consensusNumbers;
    }

    public void setConsensusNumbers(String c) {
        this.consensusNumbers = c;
    }

    public String getPanicStatus() {
        return panicStatus;
    }

    public void setPanicStatus(String p) {
        this.panicStatus = p;
    }

    public Map<String, String> getRationales() {
        return rationales;
    }

    public void setRationales(Map<String, String> r) {
        this.rationales = r;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String r) {
        this.rawContent = r;
    }
}
