# Sentiment Monitor

Eine JavaFX-Anwendung zur automatischen Visualisierung von Prognose-Textdateien aus einer tiefen Ordnerstruktur.

## Features

### 📊 Hauptfunktionen

- **Automatisches Einlesen**: Rekursives Scannen von Asset-Ordnern (z.B. XAUUSD, EURUSD)
- **Intelligentes Parsing**: Extraktion von Prognose-Daten aus Textdateien mittels Regex
- **Übersichtliche Darstellung**: TableView mit Prognosen für zwei Tage
- **Ampel-System**: Visuelle Indikatoren (Grün/Blau) basierend auf konfigurierbaren Schwellwerten
- **Interaktive Charts**: Historische Verlaufsdarstellung mit ein-/ausschaltbaren Kurven
- **Konfigurierbar**: Persistente Einstellungen für Pfade und Schwellwerte

### 🎯 Haupttabelle

Die Hauptansicht zeigt für jedes Asset:
- **Asset-Name** (z.B. XAUUSD, EURUSD)
- **Ampel-Indikator**: 
  - 🟢 Grün wenn "Steigt" > Schwellwert
  - 🔵 Blau wenn "Fällt" > Schwellwert
- **Tag 1 & Tag 2**: Datum, Steigt %, Seitwärts %, Fällt %

### 📈 Historie-Ansicht (Doppelklick)

Bei Doppelklick auf ein Asset öffnet sich ein geteiltes Fenster:

**Oberer Bereich - Interaktiver Chart:**
- 3 Kurven: Steigt (Grün), Seitwärts (Grau), Fällt (Rot)
- Toggle-Buttons zum Ein-/Ausblenden einzelner Kurven
- X-Achse: Datum, Y-Achse: Wahrscheinlichkeit (0-100%)

**Unterer Bereich - Datentabelle:**
- Vollständige Historie aller Prognosen
- Sortiert nach Aktualität

## Technische Details

### Anforderungen

- **Java**: OpenJDK 17+
- **JavaFX**: 17.0.2
- **Build-Tool**: Maven

### Projektstruktur

```
SentimentMonitor/
├── src/main/java/com/antigravity/sentiment/
│   ├── SentimentMonitor.java          # Haupt-GUI-Klasse
│   ├── SentimentMonitorLauncher.java  # Launcher (JavaFX-Workaround)
│   ├── config/
│   │   └── ConfigManager.java         # Konfigurationsverwaltung
│   ├── logic/
│   │   ├── ReportParser.java          # Regex-basierter Parser
│   │   └── SentimentCrawler.java      # Datei-Crawler
│   └── model/
│       ├── ForecastData.java          # Datenmodell für Haupttabelle
│       └── HistoryData.java           # Datenmodell für Historie
├── config/
│   └── settings.properties            # Persistente Einstellungen
└── pom.xml
```

### Datenformat

Die Anwendung erwartet Textdateien im folgenden Format:

```
**Montag, 5. Januar 2026:**

Wahrscheinlichkeit, dass Gold steigt: 35%
Wahrscheinlichkeit, dass Gold seitwärts geht: 40%
Wahrscheinlichkeit, dass Gold fällt: 25%

**Dienstag, 6. Januar 2026:**

Wahrscheinlichkeit, dass Gold steigt: 40%
Wahrscheinlichkeit, dass Gold seitwärts geht: 30%
Wahrscheinlichkeit, dass Gold fällt: 30%
```

### Ordnerstruktur

```
RootPath/
├── XAUUSD/
│   └── 2tage/
│       ├── prognose_2026-01-05.txt
│       └── prognose_2026-01-04.txt
├── EURUSD/
│   └── 2tage/
│       └── prognose_2026-01-05.txt
└── ...
```

Die Anwendung:
1. Sucht rekursiv nach Ordnern mit einem `2tage`-Unterordner
2. Liest die **neueste** Datei (nach Änderungsdatum) für die Hauptansicht
3. Liest **alle** Dateien für die Historie-Ansicht

## Installation & Start

### Mit Maven

```bash
# Kompilieren
mvn clean compile

# Starten
mvn javafx:run
```

### In Eclipse/IntelliJ

1. Projekt als Maven-Projekt importieren
2. **Wichtig**: `SentimentMonitorLauncher.java` als Main-Class verwenden (nicht `SentimentMonitor.java`)
3. Run As → Java Application

> **Hinweis**: Der Launcher ist notwendig, um den JavaFX-Runtime-Fehler zu vermeiden.

## Konfiguration

### Einstellungen-Menü

**Root-Verzeichnis wählen:**
- Setzt den Pfad zum Scan-Verzeichnis
- Wird in `config/settings.properties` gespeichert

**Schwellwerte konfigurieren:**
- **Grün ab (%)**: Schwellwert für grünen Indikator
- **Blau ab (%)**: Schwellwert für blauen Indikator
- Standard: jeweils 50%

### settings.properties

```properties
root.path=C:\\Users\\username\\.n8n-files
threshold.up=50
threshold.down=50
```

## Parser-Features

### Datum-Formatierung

Eingabe: `**Montag, 5. Januar 2026:**`  
Ausgabe: `Mo 5.1.26`

- Erste 2 Buchstaben des Wochentags
- Tag.Monat.Jahr (2-stellig)

### Dynamisches Mapping

Der Begriff "Gold" in den Textdateien wird automatisch durch den Asset-Namen ersetzt (z.B. "XAUUSD").

### Fehlerbehandlung

- Leere Ordner werden übersprungen
- Fehlende `2tage`-Verzeichnisse werden ignoriert
- Falsch formatierte Dateien führen zu 0%-Werten

## Lizenz

Dieses Projekt ist für den internen Gebrauch entwickelt.

## Autor

Entwickelt mit Antigravity AI
