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
            String signal = "NEUTRAL";

            // 1. Check Panic
            String panicStatus = analysis.getPanicStatus();
            if (panicStatus != null) {
                String p = panicStatus.toUpperCase();
                // Ignore "Kein Panic", "Sicher", "Safe"
                boolean isSafe = p.contains("KEIN PANIC") || p.contains("NO PANIC") || p.contains("SICHER")
                        || p.contains("SAFE");
                if (p.contains("PANIC") && !isSafe) {
                    signal = "PANIC";
                }
            }

            // 2. If not Panic, use CSV Signal or Probabilities
            if (!"PANIC".equals(signal)) {
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
                        // Allow probabilities to override Neutral if they are very strong?
                        // For now, trust the robot's "Neutral" verdict.
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
            }

            String sentiment = analysis.getFxssiLong() + " L / " + analysis.getFxssiShort() + " S";
            String indicators = "RSI=" + analysis.getRsi() + ", ATR=" + analysis.getAtr();

            ForecastData fd = new ForecastData(
                    assetName,
                    assetDir.getAbsolutePath(),
                    analysis.getDate(),
                    signal,
                    sentiment,
                    analysis.getVix(),
                    analysis.getConsensusNumbers(),
                    indicators,
                    analysis.getUpProbability(),
                    analysis.getSidewaysProbability(),
                    analysis.getDownProbability());

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
                // For history we can use simple ReportParser or FullAnalysisParser.
                // Using ReportParser for speed/simplicity as it extracts probabilities directly
                // But ReportParser might fail on new format?
                // Let's us FullAnalysisParser for consistency!
                FullAnalysisData fad = parser.parseFullAnalysis(content);
                history.add(new HistoryData(
                        fad.getDate(),
                        fad.getUpProbability(),
                        fad.getSidewaysProbability(),
                        fad.getDownProbability(),
                        file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return history;
    }

    private int parsePercentage(String s) {
        try {
            return Integer.parseInt(s.replace("%", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
