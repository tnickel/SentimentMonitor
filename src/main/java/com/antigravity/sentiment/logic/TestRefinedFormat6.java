package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestRefinedFormat6 {
    public static void main(String[] args) {
        String content = "### SECTION_0_RECHERCHE_ERGEBNISSE\n" +
                "Instrument: EURAUD\n" +
                "Datum: 2026-01-09\n" +
                "Recherche-Zeitstempel: 2026-01-09 13:22 UTC\n" +
                "\n" +
                "FXSSI SENTIMENT (von https://fxssi.com/tools/):\n" +
                "- Long Position: 57%\n" +
                "- Short Position: 43%\n" +
                "\n" +
                "ANALYSTEN-KONSENS (1-3 Tage):\n" +
                "Konsens: 2 von 4 Quellen (FXStreet + TradingView technicals) kurzfristig bärisch; Investing/aggregierte Seiten gemischt; DailyFX/ForexLive keine spezifische Kurzfrist-Abdeckung gefunden.\n"
                +
                "\n" +
                "Technische Überdehnung:\n" +
                "- RSI (14): TradingView technische Oberfläche liefert Oscillators = Neutral (keine zugängliche numerische RSI(14) im HTML-Snapshot); keine starke Extrem-Markierung (kein klarer RSI >70/<30 über TradingView-Teaser). ([tradingview.com](...))\n"
                +
                "- ATR: Volatilität / ATR-Indikatoren berichten moderat erhöht (Quellen zeigen ATR-Range-Notizen; kurzfristige ATR-Interpretationen variieren; z.B. kommentierte tägliche ATR‑Pips‑Schätzungen in Analysen). Insgesamt Volatilität aktuell: Mittel. ([investingcube.com](...))\n"
                +
                "\n" +
                "Makro-Risiken:\n" +
                "- VIX Index: ca. 15–16 (normal/leicht erhöht, weit unter Panic-Schwelle 30). → Normal. ([cboe.com](...))\n"
                +
                "\n" +
                "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Reversion-Wahrscheinlichkeit: 35%\n" +
                "\n" +
                "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "SCHRITT 1 - Panic Check:\n" +
                "Status: Sicher\n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "Instrument: EURAUD\n" +
                "CSV_SIGNAL: NEUTRAL\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Date: " + data.getDate());
        System.out.println("FXSSI: " + data.getFxssiLong() + " / " + data.getFxssiShort());
        System.out.println("Consensus Numbers: " + data.getConsensusNumbers());
        System.out.println("RSI: " + data.getRsi());
        System.out.println("ATR: " + data.getAtr());
        System.out.println("VIX: " + data.getVix());
        System.out.println("Panic: " + data.getPanicStatus());
        System.out.println("Signal: " + data.getCsvSignal());
    }
}
