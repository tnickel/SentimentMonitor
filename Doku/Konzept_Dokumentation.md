# Sentiment Monitor - Konzept & Dokumentation

## 1. Einleitung: Was macht das Programm?
Der **Sentiment Monitor** ist ein spezialisiertes Analyse-Dashboard für den Forex- und Rohstoff-Handel. Es aggregiert komplexe Markt-Daten aus verschiedenen Quellen – überwiegend Sentiment-Daten (Retail Crowd Positioning), Analysten-Meinungen und technische Indikatoren – und verdichtet diese zu einem klaren Handels-Signal (**BUY**, **SELL**, **NEUTRAL**, **STOP**). Es dient als "Entscheidungs-Zentrale" für den (halb-)automatischen Handel (z.B. für Grid-Roboter).

## 2. Die Idee (Die Philosophie)
Die Kern-Idee basiert auf dem **Contrarian Trading** (Handeln gegen die Masse).

Statistiken zeigen, dass die Mehrheit der privaten Trader (Retail Crowd) im Forex-Markt langfristig Geld verliert (oft >70%). Das liegt an emotionalen Entscheidungen, Herdenverhalten und mangelndem Risikomanagement.
*   **Die These:** Wenn eine extreme Mehrheit der Kleinanleger (z.B. >65-70%) auf "Steigende Kurse" (Long) setzt, ist die Wahrscheinlichkeit hoch, dass der Kurs tatsächlich fällt. Smart Money (Banken/Institutionelle) bewegt den Markt oft in die Gegenrichtung, um Liquidität abzugreifen.

Der Sentiment Monitor automatisiert das Erkennen dieser Extreme. Er verlässt sich aber nicht blind darauf, sondern prüft zusätzlich:
1.  **Fundamentaldaten**: Was sagen Analysten und News? (Vermeidung von Trades gegen echte News-Schocks).
2.  **Technik**: Ist der Markt bereits überdehnt (RSI, Bollinger, ATR)?
3.  **Volatilität (VIX)**: Herrscht Panik? (Dann Trading stoppen -> "Panic/Stop").

## 3. Warum man diese Analyse braucht (Das "Warum")
Warum reicht ein einfacher Chart oder ein Standard-Roboter nicht aus?

1.  **Vermeidung von "Fallen"**:
    Ein normaler Grid-Roboter kauft oft stur nach, wenn der Kurs fällt (Martingale). Wenn der Kurs aber aufgrund fundamentaler Gründe (Krieg, Zinsentscheid) abstürzt, löscht das das Konto aus.
    *   **Lösung**: Der Sentiment Monitor erkennt "Panik" oder "Runaway-Trends" und schaltet den Roboter ab (Signal: STOP/NEUTRAL), *bevor* das Konto gefährdet ist.

2.  **Objektivität statt Emotion**:
    Menschen neigen dazu, Trends hinterherzulaufen ("FOMO"). Das Programm liefert nüchterne, datenbasierte Fakten: "70% der Leute kaufen gerade – also verkaufen wir."

3.  **Edge (Marktvorteil)**:
    Durch das Handeln *gegen* die Masse (Counter-Retail) in Kombination mit *mit* den Banken (Analysten-Konsens) entsteht ein statistischer Vorteil ("Edge"), den technische Analyse allein oft nicht bietet. Chartmuster lügen oft, aber Positionierungs-Daten zeigen, wo das "dumme Geld" liegt.

4.  **Effizienz**:
    Statt jeden Morgen 10 Webseiten (FXSSI, Investing, DailyFX, Reuters) manuell zu prüfen, liefert der Monitor (via Crawler) sofort den Status Quo aller Währungspaare auf einen Blick.

---

**Zusammenfassung**: Der Sentiment Monitor ist der "Sicherheitsgurt" und das "Navi" für den algorithmischen Handel. Er verhindert Fahrten in den Abgrund (Stop bei Panik) und zeigt die profitabelste Route (Contrarian).
