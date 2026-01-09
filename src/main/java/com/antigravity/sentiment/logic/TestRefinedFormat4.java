package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormat4 {
    public static void main(String[] args) {
        String content = "### SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: AUDUSD  \n" +
                "Datum: 2026-01-09  \n" +
                "Recherche-Zeitstempel: 2026-01-09 13:22:01 UTC\n" +
                "\n" +
                "FXSSI SENTIMENT (von https://fxssi.com/tools/):\n" +
                "- Long Position: 30%. ([fxssi.com](https://fxssi.com/tools/))  \n" +
                "- Short Position: 70%. ([fxssi.com](https://fxssi.com/tools/))  \n" +
                "- Open Interest: Nicht in den kostenlosen Web-Tools verfügbar...\n" +
                "\n" +
                "ANALYSTEN-KONSENS (1-3 Tage):\n" +
                "Quelle 1 - DailyFX: Neutral...\n" +
                "Konsens: 2 von 4 Quellen kurzfristig Bullish (FXStreet, MarketPulse/DailyForex)...\n" +
                "Mehrheitsmeinung: Leicht Bullish / Mixed...\n" +
                "\n" +
                "Technische Überdehnung:\n" +
                "- RSI (14): Investing.com 42.48 → neutral/leicht bearish...\n" +
                "- ATR: niedrige bis mittlere Volatilität (Investing.com ATR(14) sehr klein... andere zeigen ATR ~0.004...)\n"
                +
                "\n" +
                "Makro-Risiken:\n" +
                "- VIX Index: ca. 14–15 (kein Paniklevel...)\n" +
                "\n" +
                "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Instrument: AUDUSD\n" +
                "Reversion-Wahrscheinlichkeit: 65%  \n" +
                "Trend-Fortsetzungs-Risiko: 35%  \n" +
                "\n" +
                "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "SCHRITT 1 - Panic Check:\n" +
                "Status: Sicher (kein PANIC). VIX ≈ 14–15...\n" +
                "\n" +
                "SCHRITT 3 - FXSSI Sentiment (Contrarian) - HAUPTKRITERIUM:\n" +
                "Crowd-Positionierung... 30% Long / 70% Short.\n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "Instrument: AUDUSD  \n" +
                "CSV_SIGNAL: BUY\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Date: " + data.getDate());
        System.out.println("FXSSI: " + data.getFxssiLong() + " / " + data.getFxssiShort());
        System.out.println("Consensus Numbers: " + data.getConsensusNumbers());
        System.out.println("Consensus Text: " + (data.getAnalystConsensus() != null ? "Found" : "Null"));
        System.out.println("RSI: " + data.getRsi());
        System.out.println("ATR: " + data.getAtr());
        System.out.println("VIX: " + data.getVix());
        System.out.println("Panic: " + data.getPanicStatus());
        System.out.println("Signal: " + data.getCsvSignal());
    }
}
