package com.antigravity.sentiment.logic;

import java.util.List;

public class TestNewFormat {
    public static void main(String[] args) {
        String content = "### SECTION_0_DATEN_BASIS\n" +
                "Quelle Sentiment: https://fxssi.com/tools/ (FXSSI Web Tools). ([fxssi.com](https://fxssi.com/tools/))  \n"
                +
                "Quelle News: Reuters - \"Slower US job growth expected in December; unemployment rate likely dipped\". ([reuters.com](https://www.reuters.com/world/us/slower-us-job-growth-expected-december-unemployment-rate-likely-dipped-2026-01-09/?utm_source=openai))  \n"
                +
                "Gepruefte Live-Daten:\n" +
                "- FXSSI Ratio (Long/Short): 41% / 59%. ([fxssi.com](https://fxssi.com/tools/open-interest))\n" +
                "- Open Interest Trend: Seitwärts (keine klaren, zugänglichen OI-Delta-Signale ohne Pro-Snapshot). ([fxssi.com](https://fxssi.com/tools/open-interest))\n"
                +
                "- Wichtigste News heute: U.S. Employment Situation / Nonfarm Payrolls (Dez. 2025) – hohe Marktrelevanz. ([ebc.com](https://www.ebc.com/forex/nonfarm-payrolls-today-release-time-and-key-signals?utm_source=openai))\n"
                +
                "- High Impact Events (<24h): \n" +
                "  - US Nonfarm Payrolls (Dec) — Veröffentlichung heute, 08:30 ET. ([ebc.com](https://www.ebc.com/forex/nonfarm-payrolls-today-release-time-and-key-signals?utm_source=openai))\n"
                +
                "  - Univ. of Michigan Preliminary Inflation Expectations (Prelim UoM) — heute (Nachmittag/Abend, marktbewegend). ([fxgt.com](https://fxgt.com/blog/the-week-ahead-key-economic-events-to-watch-for-january-5-9-2026/?utm_source=openai))\n"
                +
                "  - China CPI (Dez) — frühe Asia-Releases (heute), Einfluss auf Risiko-Sentiment. ([investinglive.com](https://investinglive.com/news/economic-and-event-calendar-in-asia-friday-january-9-2026-inflation-data-from-china-20260108/?utm_source=openai))\n"
                +
                "\n" +
                "### SECTION_1_RISIKO_PROFIL\n" +
                "Instrument: EURUSD  \n" +
                "Datum: 2026-01-09  \n" +
                "Volatilitaets-Warnung: MITTEL (spot VIX / Marktvolatilität aktuell niedrig-mittel; NFP kann kurzfristig Volatilität hoch treiben). ([ycharts.com](https://ycharts.com/indicators/vix_volatility_index?utm_source=openai))  \n"
                +
                "Wahrscheinlichkeit Trend-Start (Gefahr): 25% (vor NFP gering, nach NFP Spike möglich)  \n" +
                "Wahrscheinlichkeit Range/Reversion (Gut): 75% (aktuelles Sentiment + OI deuten eher auf Reversion/Range)  \n"
                +
                "\n" +
                "Datum: 2026-01-11  \n" +
                "Erwartete Range Stabilitaet: 60% (wenn NFP ohne Überraschung)  \n" +
                "Crash-Risiko: 5% (keine Black-Swan-Indikatoren; geopolitische Konflikte bestehen, aber kein akutes Markt-Panik-Trigger heute) . ([en.wikipedia.org](https://en.wikipedia.org/wiki/Middle_Eastern_crisis_%282023%E2%80%93present%29?utm_source=openai))\n"
                +
                "\n" +
                "### SECTION_2_LOGISCHE_HERLEITUNG\n" +
                "Instrument: EURUSD  \n" +
                "Makro/Geopolitik (Veto-Check): Sicher (keine neuen Black-Swan-Ereignisse / VIX << 30). -> Status: Keine Immediate-EXIT-Veto; aber anhaltende regionale Konflikte erhöhen strukturelles Risiko. ([ycharts.com](https://ycharts.com/indicators/vix_volatility_index?utm_source=openai))  \n"
                +
                "Sentiment/FXSSI (Contrarian): Crowd Short (~59%) -> Implikation: leichtes Contrarian-Bias zu LONG, aber unter der 60%-Schwelle (nicht extrem). ([fxssi.com](https://fxssi.com/tools/open-interest))  \n"
                +
                "Charttechnik (Mean Reversion): Neutral — Preis um kurzfristiges Konsolidierungsband (kein klarer extremes Überkauft/Überverkauft auf Tagesbasis laut aktuellen Preisdaten ~1.17). ([tradingeconomics.com](https://tradingeconomics.com/euro-area/currency?utm_source=openai))  \n"
                +
                "Ergebnis Bias: Neutral (keine neuen Entries bis Clearance). ([investing.com](https://www.investing.com/currencies/eur-usd-exchange-rate-cash-futures-historical-data?utm_source=openai))  \n"
                +
                "\n" +
                "### SECTION_3_ROBOTER_STEUERUNG\n" +
                "Instrument: EURUSD  \n" +
                "CSV_SIGNAL: NEUTRAL  \n" +
                "Handels-Modus: Pause für neue Grid-Entries bis nach den High-Impact-Releases (NFP + UoM). Erlaube nur automatisches Management existierender Grid-Positionen (Rebalancing/Teil-Schliessen nach vordefinierten Regeln), keine neuen Add-Orders.  \n"
                +
                "Risiko-Level: MEDIUM\n";

        ReportParser parser = new ReportParser();
        List<ReportParser.DayForecast> results = parser.parseContent(content);

        System.out.println("Parsed " + results.size() + " forecasts.");
        for (ReportParser.DayForecast df : results) {
            System.out.println("Date: " + df.date);
            System.out.println("  Up: " + df.up);
            System.out.println("  Side: " + df.sideways);
            System.out.println("  Down: " + df.down);
        }
    }
}
