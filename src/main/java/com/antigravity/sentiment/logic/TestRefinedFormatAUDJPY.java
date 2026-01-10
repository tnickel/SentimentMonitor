package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormatAUDJPY {
    public static void main(String[] args) {
        String content = "### SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: AUDJPY\n" +
                "Datum: 2026-01-09\n" +
                "\n" +
                "FXSSI SENTIMENT (von https://fxssi.com/tools/):\n" +
                "- Long Position: 33%.\n" +
                "- Short Position: 67%.\n" +
                "\n" +
                "ANALYSTEN-KONSENS (1-3 Tage):\n" +
                "Quelle 1 - DailyFX ...\n" +
                "Konsens: 1 of 4 sources Bullish / 2 of 4 Mixed / 1 of 4 Bearish (no strong unanimous short-term consensus).\n"
                +
                "Mehrheitsmeinung: Mixed (leaning bearish in technical aggregator data).\n" +
                "\n" +
                "Technische Überdehnung:\n" +
                "- RSI (14): ~49.4 → Neutral (nicht überkauft/überverkauft).\n" +
                "- ATR (14): 0.1014 → \"Less Volatility\" / niedrigere kurzfristige Volatilität per Investing.com.\n" +
                "- Bollinger Bands: No numeric band values...\n" +
                "\n" +
                "Makro-Risiken:\n" +
                "- VIX Index: ≈16–17 (normal/low...)\n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "CSV_SIGNAL: BUY\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Consensus Text: " + data.getAnalystConsensus());
        System.out.println("Consensus Numbers: " + data.getConsensusNumbers());
        System.out.println("RSI: " + data.getRsi());
        System.out.println("ATR: " + data.getAtr());
        System.out.println("VIX: " + data.getVix());
    }
}
