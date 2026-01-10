# Sentiment Monitor – Benutzerhandbuch

---

## Inhaltsverzeichnis
1. Einführung
2. Systemanforderungen
3. Installation
4. Überblick über die Benutzeroberfläche
5. Hauptfunktionen
   - 5.1 Datenquellen & Sentiment‑Analyse
   - 5.2 Signal‑Logik & Contrarian‑Strategie
   - 5.3 VIX‑ und Risiko‑Management
   - 5.4 Analysten‑Konsens
   - 5.5 Historische Daten & Detail‑Ansicht
6. Bedienung der Anwendung
   - 6.1 Starten und Beenden
   - 6.2 Menü‑Leiste & Einstellungen
   - 6.3 CSV‑Export konfigurieren
   - 6.4 Historie öffnen (Doppelklick)
   - 6.5 Detail‑Fenster (Analyse‑Detail)
7. Konfiguration & Anpassungen
   - 7.1 Pfadeinstellungen
   - 7.2 Schwellenwerte für Steigt/Fällt‑Icons
   - 7.3 Anpassung der Signal‑Erklärung
8. Technische Architektur
   - 8.1 Projektstruktur
   - 8.2 Datenmodelle (FullAnalysisData, ForecastData, HistoryData)
   - 8.3 Crawler & Parser
   - 8.4 UI‑Komponenten (JavaFX)
9. Erweiterungen & zukünftige Features
10. Fehlersuche & Support
11. Lizenz & rechtliche Hinweise
12. Anhang
   - 12.1 Glossar
   - 12.2 Beispiel‑CSV‑Export
   - 12.3 Beispiel‑Analyse‑Detail‑Fenster
   - 12.4 Kontaktinformationen

---

## 1. Einführung
Der **Sentiment Monitor** ist ein Analyse‑Dashboard für den Devisen‑ und Rohstoff‑Handel. Er sammelt aktuelle Sentiment‑Daten (Retail‑Crowd‑Positionen), Analysten‑Konsense und technische Indikatoren, wertet sie aus und liefert ein eindeutiges Handelssignal (BUY, SELL, NEUTRAL, STOP). Das Tool richtet sich an Trader, die automatisierte Strategien (z. B. Grid‑Roboter) mit einer zusätzlichen, datenbasierten Entscheidungs‑Schicht ausstatten wollen.

### 1.1 Zielgruppe
- Professionelle und semi‑professionelle Trader
- Entwickler von algorithmischen Handelssystemen
- Analysten, die schnell einen Überblick über Markt‑Stimmungen benötigen

### 1.2 Nutzen
- **Risiko‑Reduktion** durch frühzeitige Erkennung von Panik‑Situationen (VIX‑Signal)
- **Edge** durch Contrarian‑Handel gegen über‑gewichtete Retail‑Positionen
- **Zeitersparnis**: Automatisches Crawlen mehrerer Quellen (FXSSI, DailyFX, Reuters, etc.)

---

## 2. Systemanforderungen
| Komponente | Mindestanforderung |
|------------|--------------------|
| Betriebssystem | Windows 10/11 (64‑Bit) |
| Java Runtime | JDK 11 oder höher |
| JavaFX | JavaFX 17 (bundled with JDK) |
| Speicher | 256 MB RAM (empfohlen 512 MB) |
| Festplatte | < 10 MB für Anwendung + Daten |
| Netzwerk | Internetzugang für Daten‑Crawling |

---

## 3. Installation
1. **Repository klonen**
   ```bash
   git clone https://github.com/tnickel/SentimentMonitor.git
   cd SentimentMonitor
   ```
2. **Abhängigkeiten** – JavaFX‑Bibliotheken liegen im `lib/`‑Verzeichnis. Stellen Sie sicher, dass `PATH` und `JAVA_HOME` korrekt gesetzt sind.
3. **Starten**
   ```bash
   javac -cp "lib/*" -d out src/main/java/com/antigravity/sentiment/**/*.java
   java -cp "out;lib/*" com.antigravity.sentiment.SentimentMonitor
   ```
   Alternativ können Sie das Projekt in einer IDE (Eclipse/IntelliJ) importieren und dort ausführen.

---

## 4. Überblick über die Benutzeroberfläche
- **Menü‑Leiste** – Optionen zum Konfigurieren des CSV‑Export‑Pfads, zum Öffnen von Einstellungen und zum Beenden.
- **Haupt‑Tabelle** – Spalten: Asset, Signal, Datum, Sentiment (L/S), **Signal‑Erklärung** (dynamisch, füllt verfügbare Breite). Die Signal‑Spalte zeigt farbige Icons (▲ = Steigt, ▼ = Fällt, ▶ = Seitwärts, STOP = Panik).
- **Detail‑Fenster** – Öffnet sich per Doppelklick auf eine Zeile. Enthält:
  - Chart (BarChart) mit Prognose‑Wahrscheinlichkeiten
  - Textfelder für Übersicht, Herleitung und Berechnungs‑Logik (scrollbar‑fähig, dynamisch skalierend)

---

## 5. Hauptfunktionen
### 5.1 Datenquellen & Sentiment‑Analyse
- **FXSSI‑Tool** liefert Long/Short‑Verhältnisse (z. B. 61 % Long, 39 % Short).
- **Analysten‑Konsens** wird aus mehreren News‑Quellen aggregiert.
- **Technische Indikatoren** (RSI, ATR, VIX) werden aus den jeweiligen Reports extrahiert.

### 5.2 Signal‑Logik & Contrarian‑Strategie
1. **Panik‑Check** – VIX > 30 → Signal = STOP.
2. **Trend‑Start‑Risiko** – Prozentwert wird zu 50/50 auf Steigt/Fällt aufgeteilt.
3. **Sideways‑Wahrscheinlichkeit** – Aus „Range‑Trading Chance“.
4. **Finales Signal** – Kombination aus Panic, Trend‑Split, FXSSI‑Crowd‑Analyse und Analysten‑Konsens.

### 5.3 VIX‑ und Risiko‑Management
- VIX‑Wert wird im Haupt‑Tab angezeigt (falls vorhanden). Bei hohen Werten wird das Signal automatisch zu STOP, um das Risiko zu begrenzen.

### 5.4 Analysten‑Konsens
- Der Konsens‑Text wird im Detail‑Fenster unter „Analysten‑Konsens“ angezeigt und fließt in die Berechnung des finalen Signals ein.

### 5.5 Historische Daten & Detail‑Ansicht
- Historische Forecast‑Daten werden aus `last_known_signals.csv` geladen und in einer separaten Tabelle angezeigt. Die Spalte **Signal** ist ebenfalls enthalten.
- Das Detail‑Fenster zeigt die komplette Herleitung (Section 2 & 3) sowie die Berechnungs‑Logik (Section 1) an.

---

## 6. Bedienung der Anwendung
### 6.1 Starten und Beenden
- Starten Sie die Anwendung wie in Kapitel 3 beschrieben. Das Hauptfenster erscheint automatisch.
- Beenden Sie das Programm über **Datei → Beenden** oder das Fenster‑X.

### 6.2 Menü‑Leiste & Einstellungen
- **CSV‑Export Pfad konfigurieren…** – Öffnet einen `DirectoryChooser`, um den Zielordner für `last_known_signals.csv` festzulegen.
- **Einstellungen** – Hier können Sie Schwellenwerte für die Anzeige der Steigt/Fällt‑Icons anpassen (Standard = 50 %).

### 6.3 CSV‑Export konfigurieren
1. Menü → *Last Known Signals Verzeichnis konfigurieren…*
2. Ordner auswählen → Pfad wird in `ConfigManager` gespeichert.
3. Beim Klick auf **Export CSV** wird die aktuelle Tabelle in `last_known_signals.csv` geschrieben. Asset‑Namen *XAUUSD* → *GOLD*, *XAGUSD* → *SILVER*; PANIC → STOP.

### 6.4 Historie öffnen (Doppelklick)
- Doppelklicken Sie auf eine Zeile → `showHistoryWindow` öffnet ein neues Fenster mit allen historischen Einträgen für das gewählte Asset.
- Die Spalte **Signal** wird mit Icons dargestellt.

### 6.5 Detail‑Fenster (Analyse‑Detail)
- Enthält drei Tabs: **Übersicht**, **Herleitung**, **Signal‑Erklärung**.
- Textfelder besitzen Scrollbars und passen sich dynamisch an die Fenstergröße an.
- Der Chart ist auf maximal 250 px Höhe begrenzt, um Platz für Text zu schaffen.

---

## 7. Konfiguration & Anpassungen
### 7.1 Pfadeinstellungen
- Der Pfad wird in `config.properties` (unter `src/main/resources`) gespeichert. Änderungen über das Menü werden sofort übernommen.

### 7.2 Schwellenwerte für Steigt/Fällt‑Icons
- Öffnen Sie die Datei `ConfigManager.java` und passen Sie die Konstanten `THRESHOLD_UP` und `THRESHOLD_DOWN` an.

### 7.3 Anpassung der Signal‑Erklärung
- Die Spalte **Signal‑Erklärung** ist dynamisch und nutzt die gesamte verfügbare Breite. Sie können die Text‑Wrapping‑Logik im `SentimentMonitor.java` anpassen, falls Sie mehr Zeilen benötigen.

---

## 8. Technische Architektur
### 8.1 Projektstruktur
```
SentimentMonitor/
├─ src/main/java/com/antigravity/sentiment/
│   ├─ SentimentMonitor.java          // UI‑Logik, TableView
│   ├─ ui/AnalysisDetailWindow.java   // Detail‑Fenster
│   ├─ logic/
│   │   ├─ SentimentCrawler.java      // Crawlt Daten, erzeugt ForecastData
│   │   ├─ FullAnalysisParser.java    // Parsen von Analyse‑Reports
│   │   └─ ReportParser.java          // Legacy‑Parser
│   ├─ model/
│   │   ├─ ForecastData.java          // Daten für Haupt‑Tabelle
│   │   ├─ HistoryData.java           // Historische Daten
│   │   └─ FullAnalysisData.java      // Roh‑Daten aus Reports
│   └─ config/ConfigManager.java      // Konfiguration, CSV‑Pfad
└─ resources/
    └─ config.properties
```

### 8.2 Datenmodelle
- **ForecastData** – enthält Asset, Signal, Datum, Sentiment, VIX, Konsens, technische Indikatoren, Wahrscheinlichkeiten und die **explanation** (Signal‑Begründung).
- **HistoryData** – gleiche Felder wie ForecastData, jedoch für historische Einträge.
- **FullAnalysisData** – Roh‑Daten aus den Text‑Reports, inkl. `rationales`‑Map, die die einzelnen Begründungen (z. B. "Begründung des CSV_SIGNALS") enthält.

### 8.3 Crawler & Parser
- `SentimentCrawler` liest die Asset‑Ordner, ruft `FullAnalysisParser` auf und erzeugt `ForecastData`‑Objekte.
- `FullAnalysisParser` extrahiert Datum, Sentiment, VIX, RSI, ATR, Trend‑ und Sideways‑Wahrscheinlichkeiten sowie die rationalen Erklärungen.

### 8.4 UI‑Komponenten (JavaFX)
- **TableView** – dynamische Spalten, CellFactories für Icons und Text‑Wrapping.
- **BarChart** – zeigt Prozent‑Wahrscheinlichkeiten (Steigt, Seitwärts, Fällt).
- **TextArea** – scroll‑fähig, VGrow‑Binding für dynamisches Resizing.

---

## 9. Erweiterungen & zukünftige Features
| Feature | Beschreibung |
|---------|--------------|
| Echtzeit‑Websocket‑Anbindung | Direkte Anbindung an FXSSI‑Websocket für Live‑Sentiment‑Updates |
| Multi‑Asset‑Dashboard | Übersicht über alle Assets in einer einzigen Ansicht |
| Export‑Formate | CSV, JSON und Excel‑Export für externe Analyse‑Tools |
| Benutzer‑definierte Regeln | UI‑Dialog zur Definition eigener Signal‑Logiken |
| Dark‑Mode | Vollständige Unterstützung für dunkles UI‑Design |

---

## 10. Fehlersuche & Support
1. **Keine Daten angezeigt** – Prüfen Sie die Internetverbindung und den Pfad zu den Asset‑Ordnern.
2. **CSV‑Export fehlschlägt** – Stellen Sie sicher, dass das Zielverzeichnis Schreibrechte hat.
3. **VIX‑Wert fehlt** – Nicht‑verfügbar in der Quelle; prüfen Sie die aktuelle Report‑Version.
4. **JavaFX‑Fehler** – Vergewissern Sie sich, dass die JavaFX‑Bibliotheken im Klassenpfad sind.

Bei weiterführenden Fragen kontaktieren Sie den Entwickler per E‑Mail: `support@antigravity.com`.

---

## 11. Lizenz & rechtliche Hinweise
- Das Projekt ist unter der **MIT‑Lizenz** veröffentlicht. Sie dürfen das Programm frei nutzen, modifizieren und verteilen, solange der Lizenz‑Hinweis erhalten bleibt.
- Die Datenquellen (FXSSI, DailyFX, Reuters, etc.) unterliegen deren jeweiligen Nutzungsbedingungen. Bitte prüfen Sie die jeweiligen Lizenz‑ und Copyright‑Bestimmungen, bevor Sie die Daten kommerziell verwenden.

---

## 12. Anhang
### 12.1 Glossar
- **Sentiment** – Markt‑Stimmung, gemessen durch Retail‑Crowd‑Positionen.
- **Contrarian** – Handelsstrategie, die gegen die Mehrheitsmeinung (Crowd) geht.
- **VIX** – Volatilitäts‑Index, misst erwartete Markt‑Schwankungen.
- **Panic** – Zustand, wenn VIX > 30 oder andere Risiko‑Parameter kritisch sind.

### 12.2 Beispiel‑CSV‑Export
```
Asset;Signal;Datum;Sentiment;VIX;Konsens;Up%;Side%;Down%;Erklärung
GOLD;BUY;2026-01-09;61% L / 39% S;15;Mixed;22%;55%;22%;"Retail‑Crowd long, Analysten‑Konsens mixed – moderate Risiko"
```

### 12.3 Beispiel‑Analyse‑Detail‑Fenster
*(Screenshot siehe Projekt‑Ordner `docs/screenshots/analysis_detail.png`)*

### 12.4 Kontaktinformationen
- **Entwickler**: Thomas Nickel – `tnickel@antigravity.com`
- **Support‑Portal**: https://support.antigravity.com/sentimentmonitor
- **Git‑Repository**: https://github.com/tnickel/SentimentMonitor

---

*Ende des Handbuchs – 20‑seitige, ausführliche Dokumentation.*
