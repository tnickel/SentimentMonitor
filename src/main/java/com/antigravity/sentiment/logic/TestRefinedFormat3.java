package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormat3 {
    public static void main(String[] args) {
        String content = "### SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: EURUSD\n" +
                "ANALYSTEN-KONSENS (1-3 Tage):\n" +
                "Konsens: 2 von 3 verifizierbaren Quellen (FXStreet, Investing.com) kurzfr. Bearish\n" +
                "\n" +
                "Technische Überdehnung:\n" +
                "- RSI (14): 36.809 → Neutral...\n" +
                "- ATR: 0.0007 → Volatilität...\n" +
                "\n" +
                "Makro-Risiken:\n" +
                "- VIX Index: ~15–16 → Normal...\n" +
                "\n" +
                "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Instrument: EURUSD\n" +
                "Reversion-Wahrscheinlichkeit: 35%  \n" +
                "Trend-Fortsetzungs-Risiko: 40%  \n" +
                "\n" +
                "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "SCHRITT 1 - Panic Check:\n" +
                "Status: Sicher\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("VIX Found? " + (data.getVix() != null ? data.getVix() : "NO"));
        System.out.println("RSI Found? " + (data.getRsi() != null ? data.getRsi() : "NO"));
        System.out.println("ATR Found? " + (data.getAtr() != null ? data.getAtr() : "NO"));
        System.out.println(
                "Consensus Numbers? " + (data.getConsensusNumbers() != null ? data.getConsensusNumbers() : "NO"));
        System.out.println("Panic Status: " + data.getPanicStatus());
    }
}
