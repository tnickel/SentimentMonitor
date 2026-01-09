package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormat {
    public static void main(String[] args) {
        String content = "## SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: EURUSD\n" +
                "Datum: 2026-01-09\n" +
                "\n" +
                "FXSSI Sentiment:\n" +
                "- Long Position: 42%\n" +
                "- Short Position: 58%\n" +
                "- Interpretation: Crowd ist Neutral (leichter Short-Überschuss, aber nicht extrem >65%). ([fxssi.com](https://fxssi.com/tools/current-ratio))\n"
                +
                "\n" +
                "Technische Überdehnung:\n" +
                "- RSI (14): 37.135 → Neutral / leicht unter Druck (nicht überverkauft). ([investing.com](https://www.investing.com/currencies/eur-usd-technical))\n"
                +
                "\n" +
                "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Instrument: EURUSD\n" +
                "\n" +
                "Ist der Markt überdehnt? NEIN\n" +
                "\n" +
                "Reversion-Wahrscheinlichkeit: 35%  \n" +
                "Trend-Fortsetzungs-Risiko: 65%  \n" +
                "\n" +
                "Grid-Sicherheit (nächste 1-3 Tage):\n" +
                "- Range-Trading Chance: 40%  \n" +
                "- Runaway-Trend Risiko: 60%  \n" +
                "\n" +
                "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "Instrument: EURUSD\n" +
                "\n" +
                "SCHRITT 1 - Panic Check:\n" +
                "Status: Sicher  \n" +
                "\n" +
                "SCHRITT 2 - Sentiment (Contrarian):\n" +
                "Crowd-Positionierung: 42% Long / 58% Short. ([fxssi.com](https://fxssi.com/tools/current-ratio))  \n" +
                "\n" +
                "Finaler Bias: Neutral (kein neues Grid‑Bias; vorsichtig gegenüber Short‑Fortsetzung)\n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "Instrument: EURUSD\n" +
                "CSV_SIGNAL: NEUTRAL\n" +
                "\n" +
                "Risiko-Level: MEDIUM\n" +
                "\n" +
                "### SECTION_4_DETAILLIERTE_BEGRUENDUNG\n" +
                "Alle Erklärungen beziehen sich auf EURUSD und den Grid-EA.\n" +
                "\n" +
                "1) Begründung des CSV_SIGNALS  \n" +
                "- FXSSI zeigt kein extremes Herd‑Positioning (42/58)...\n" +
                "\n" +
                "2) Crowd-Psychologie (FXSSI)  \n" +
                "- Retail hat leichtes Short‑Übergewicht...";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Date: " + data.getDate());
        System.out.println("FXSSI: " + data.getFxssiLong() + " / " + data.getFxssiShort());
        System.out.println("Sideways: " + data.getSidewaysProbability());
        System.out.println("Up/Down Split from Trend: " + data.getUpProbability());
        System.out.println("Bias: " + data.getBias());
        System.out.println("Calculation Log:\n" + data.getProbabilityCalculation());
        System.out.println("Rationales Keys: " + data.getRationales().keySet());
    }
}
