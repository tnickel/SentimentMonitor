package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormat5 {
    public static void main(String[] args) {
        String content = "### SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: GBPUSD  \n" +
                "Datum: 2026-01-09  \n" +
                "Recherche-Zeitstempel: 2026-01-09 13:22:01 UTC\n" +
                "\n" +
                "FXSSI SENTIMENT (von https://fxssi.com/tools/):\n" +
                "- Long Position: 51%  \n" +
                "- Short Position: 49%  \n" +
                "\n" +
                "ANALYSTEN-KONSENS (1-3 Tage):\n" +
                "Konsens: 1 von 4 Quellen klar bullisch, 2 von 4 tendenziell bärisch/mixed...\n" +
                "\n" +
                "Technische Überdehnung (Kurzfrist):\n" +
                "- RSI (14): ~43.4 → Neutral bis leicht unterhalb Mitte...\n" +
                "- ATR / Volatilität: ATR(14) relativ niedrig / moderate (Investing.com ATR(14) ≈ 0.0011–0.0016 → gering bis mittlere Volatilität).\n"
                +
                "\n" +
                "Makro-Risiken:\n" +
                "- VIX Index: ca. 15–16 → Normal...\n" +
                "\n" +
                "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Reversion-Wahrscheinlichkeit: 40%\n" +
                "\n" +
                "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "SCHRITT 1 - Panic Check:\n" +
                "Status: Sicher (kein Kriegs-/Terror‑Ereignis...)\n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "Instrument: GBPUSD  \n" +
                "CSV_SIGNAL: NEUTRAL\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Date: " + data.getDate());
        System.out.println("FXSSI: " + data.getFxssiLong() + " / " + data.getFxssiShort());
        System.out.println("Consensus Numbers: " + data.getConsensusNumbers());
        System.out.println("RSI: " + data.getRsi());
        System.out.println("ATR: " + data.getAtr());
        System.out.println("VIX: " + data.getVix());
        System.out.println("Signal: " + data.getCsvSignal());
    }
}
