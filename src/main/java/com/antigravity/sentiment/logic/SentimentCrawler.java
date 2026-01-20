package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.ForecastData;
import com.antigravity.sentiment.model.FullAnalysisData;
import com.antigravity.sentiment.model.HistoryData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SentimentCrawler {

    private final FullAnalysisParser parser = new FullAnalysisParser();

    public List<ForecastData> crawl(String rootPath) {
        List<ForecastData> dataList = new ArrayList<>();
        File root = new File(rootPath);

        if (!root.exists() || !root.isDirectory()) {
            return dataList;
        }

        findAssetFolders(root, dataList);
        return dataList;
    }

    private void findAssetFolders(File directory, List<ForecastData> results) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        File[] textFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles != null && textFiles.length > 0) {
            processAssetFolder(directory, results);
        }

        for (File f : files) {
            if (f.isDirectory()) {
                findAssetFolders(f, results);
            }
        }
    }

    private void processAssetFolder(File assetDir, List<ForecastData> results) {
        String assetName = assetDir.getName();

        File[] textFiles = assetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null || textFiles.length == 0)
            return;

        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified).reversed());
        File newestFile = textFiles[0];

        try {
            String content = Files.readString(newestFile.toPath());
            FullAnalysisData analysis = parser.parseFullAnalysis(content);

            // Determine Signal
            String signal = determineSignal(analysis);

            // Determine Last Signal (Previous File)
            String lastSignal = "";
            if (textFiles.length > 1) {
                try {
                    java.time.LocalDate currentDate = HistoryData.parseDate(analysis.getDate());

                    for (int i = 1; i < textFiles.length; i++) {
                        File lastFile = textFiles[i];
                        String lastContent = Files.readString(lastFile.toPath());
                        FullAnalysisData lastAnalysis = parser.parseFullAnalysis(lastContent);
                        java.time.LocalDate lastDate = HistoryData.parseDate(lastAnalysis.getDate());

                        // Condition: Must be at least 1 day older (strictly before current date)
                        // Also checks if dates are valid (not MIN)
                        if (lastDate != java.time.LocalDate.MIN && currentDate != java.time.LocalDate.MIN
                                && lastDate.isBefore(currentDate)) {
                            String lastSig = determineSignal(lastAnalysis);
                            lastSignal = lastSig;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Keep empty or error
                    System.err.println("Error determining last signal: " + e.getMessage());
                }
            }

            String sentiment = analysis.getFxssiLong() + " L / " + analysis.getFxssiShort() + " S";
            String indicators = "RSI=" + analysis.getRsi() + ", ATR=" + analysis.getAtr();

            // Extract explanation (first 3 sentences from reason "1)")
            String explanation = "Keine Begründung verfügbar";
            Map<String, String> rationales = analysis.getRationales();
            if (rationales != null) {
                for (Map.Entry<String, String> entry : rationales.entrySet()) {
                    if (entry.getKey().toLowerCase().contains("begründung")
                            && entry.getKey().toLowerCase().contains("csv_signal")) {
                        String text = entry.getValue();
                        // Split by sentence delimiters
                        String[] sentences = text.split("(?<=[.!?])\\s+");
                        StringBuilder sb = new StringBuilder();
                        int count = 0;
                        for (String sentence : sentences) {
                            if (count >= 3)
                                break;
                            sb.append(sentence).append(" ");
                            count++;
                        }
                        explanation = sb.toString().trim();
                        break;
                    }
                }
            }
            // Fallback: use Derivation text summary if no specific section found
            if ("Keine Begründung verfügbar".equals(explanation) && analysis.getDerivationText() != null) {
                // Try to pick bias
                String derivation = analysis.getDerivationText();
                if (derivation.contains("Finaler Bias:")) {
                    int start = derivation.indexOf("Finaler Bias:");
                    int end = derivation.indexOf("\n", start);
                    if (end == -1)
                        end = derivation.length();
                    explanation = derivation.substring(start, end).trim();
                }
            }

            ForecastData fd = new ForecastData(
                    assetName,
                    assetDir.getAbsolutePath(),
                    analysis.getDate(),
                    signal,
                    lastSignal,
                    sentiment,
                    analysis.getVix(),
                    analysis.getConsensusNumbers(),
                    indicators,
                    analysis.getUpProbability(),
                    analysis.getSidewaysProbability(),
                    analysis.getDownProbability(),
                    explanation);

            results.add(fd);

        } catch (IOException e) {
            System.err.println("Error reading " + newestFile.getName());
        }
    }

    public List<HistoryData> loadHistory(String assetPath) {
        List<HistoryData> history = new ArrayList<>();
        File assetDir = new File(assetPath);

        if (!assetDir.exists() || !assetDir.isDirectory())
            return history;

        File[] textFiles = assetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null)
            return history;

        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified).reversed());

        for (File file : textFiles) {
            try {
                String content = Files.readString(file.toPath());
                FullAnalysisData fad = parser.parseFullAnalysis(content);
                String sig = determineSignal(fad);

                history.add(new HistoryData(
                        fad.getDate(),
                        fad.getUpProbability(),
                        fad.getSidewaysProbability(),
                        fad.getDownProbability(),
                        sig,
                        file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return history;
    }

    private String determineSignal(FullAnalysisData analysis) {
        String signal = "NEUTRAL";

        // 1. Check Panic
        String panicStatus = analysis.getPanicStatus();
        if (panicStatus != null) {
            String p = panicStatus.toUpperCase();
            // Ignore "Kein Panic", "Sicher", "Safe"
            boolean isSafe = p.contains("KEIN PANIC") || p.contains("NO PANIC") || p.contains("SICHER")
                    || p.contains("SAFE");
            if (p.contains("PANIC") && !isSafe) {
                return "PANIC";
            }
        }

        // 2. If not Panic, use CSV Signal or Probabilities
        String csvSignal = analysis.getCsvSignal();
        boolean signalFound = false;

        if (csvSignal != null) {
            csvSignal = csvSignal.toUpperCase();
            if (csvSignal.contains("BUY") || csvSignal.contains("LONG")) {
                signal = "STEIGT";
                signalFound = true;
            } else if (csvSignal.contains("SELL") || csvSignal.contains("SHORT")) {
                signal = "FAELLT";
                signalFound = true;
            } else if (csvSignal.contains("NEUTRAL")) {
                signal = "SEITWAERTS";
                signalFound = true;
            }
        }

        // Fallback to probabilities if no clear CSV signal found
        if (!signalFound) {
            int up = parsePercentage(analysis.getUpProbability());
            int down = parsePercentage(analysis.getDownProbability());
            int side = parsePercentage(analysis.getSidewaysProbability());

            if (up > side && up > down)
                signal = "STEIGT";
            else if (down > side && down > up)
                signal = "FAELLT";
            else
                signal = "SEITWAERTS";
        }
        return signal;
    }

    private int parsePercentage(String s) {
        try {
            return Integer.parseInt(s.replace("%", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
