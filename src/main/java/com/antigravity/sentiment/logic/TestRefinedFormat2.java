package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormat2 {
    public static void main(String[] args) {
        String content = "### SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: EURUSD\n" +
                "Datum: 2026-01-09\n" +
                "Recherche-Zeitstempel: 2026-01-09 11:55:38 UTC\n" +
                "\n" +
                "FXSSI SENTIMENT (von https://fxssi.com/tools/):\n" +
                "- Long Position: 42%\n" +
                "- Short Position: 58%\n" +
                "- Open Interest: Nicht als numerischer Live-Wert...\n" +
                "\n" +
                "ANALYSTEN-KONSENS (1-3 Tage):\n" +
                "Quelle 1 - DailyFX: Unzugänglich...\n" +
                "Konsens: Bearish\n" +
                "\n" +
                "KONFLIKT-ANALYSE:\n" +
                "Analysten vs. FXSSI...\n" +
                "\n" +
                "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Instrument: EURUSD\n" +
                "\n" +
                "Reversion-Wahrscheinlichkeit: 35%  \n" +
                "Trend-Fortsetzungs-Risiko: 40%  \n" +
                "\n" +
                "Grid-Sicherheit (nächste 1-3 Tage):\n" +
                "- Range-Trading Chance: 45% (niedrig-mittel...)\n" +
                "- Runaway-Trend Risiko: 40% (moderates Risiko...)\n" +
                "\n" +
                "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "Instrument: EURUSD\n" +
                "Finaler Bias: NEUTRAL — keine saubere Contrarian-Entry...\n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "Instrument: EURUSD\n" +
                "CSV_SIGNAL: NEUTRAL\n" +
                "\n" +
                "### SECTION_4_DETAILLIERTE_BEGRUENDUNG\n" +
                "Alle Erklärungen beziehen sich auf EURUSD und den Grid-EA.\n" +
                "\n" +
                "1) Begründung des CSV_SIGNALS\n" +
                "- Hauptregel: FXSSI (HAUPTKRITERIUM)...\n" +
                "\n" +
                "2) FXSSI-Analyse (Crowd-Psychologie)\n" +
                "- Retail ist moderat short...";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Date: " + data.getDate());
        System.out.println("FXSSI: " + data.getFxssiLong() + " / " + data.getFxssiShort());
        System.out.println("Sideways: " + data.getSidewaysProbability());
        System.out.println("Up/Down Split from Trend: " + data.getUpProbability());
        System.out.println("Bias: " + data.getBias());
        System.out.println("Consensus Start: "
                + data.getAnalystConsensus().substring(0, Math.min(50, data.getAnalystConsensus().length())) + "...");
        System.out.println("Calculation Log:\n" + data.getProbabilityCalculation());
        System.out.println("Rationales Keys: " + data.getRationales().keySet());
    }
}
