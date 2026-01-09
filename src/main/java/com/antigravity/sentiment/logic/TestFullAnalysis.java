package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestFullAnalysis {
    public static void main(String[] args) {
        String content = "### SECTION_0_DATEN_BASIS\n" +
                "Ratio (Long/Short): 41% / 59%\n" +
                "### SECTION_1_RISIKO_PROFIL\n" +
                "Datum: 2026-01-09\n" +
                "Wahrscheinlichkeit Range/Reversion: 75%\n" +
                "Wahrscheinlichkeit Trend-Start: 25%\n" +
                "### SECTION_2_LOGISCHE_HERLEITUNG\n" +
                "Ergebnis Bias: NEUTRAL\n" +
                "Some text here.\n" +
                "### SECTION_3_ROBOTER_STEUERUNG\n" +
                "CSV_SIGNAL: NEUTRAL\n" +
                "### SECTION_4_BEGRUENDUNGEN\n" +
                "1) Titel Eins\n" +
                "Inhalt Eins.\n" +
                "2) Titel Zwei\n" +
                "Inhalt Zwei.";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Date: " + data.getDate());
        System.out.println("FXSSI: " + data.getFxssiLong() + " / " + data.getFxssiShort());
        System.out.println("Sideways: " + data.getSidewaysProbability());
        System.out.println("Up: " + data.getUpProbability());
        System.out.println("Calculation:\n" + data.getProbabilityCalculation());
        System.out.println("Derivation: \n" + data.getDerivationText());
        System.out.println("Rationales: " + data.getRationales().keySet());
    }
}
