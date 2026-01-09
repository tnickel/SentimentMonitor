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
    private final ReportParser historyParser = new ReportParser(); // Keep for history parsing if needed

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
            if (analysis.getPanicStatus() != null && analysis.getPanicStatus().toUpperCase().contains("PANIC")) {
                signal = "PANIC";
            } else {
                // Logic based on probabilities or CSV Signal
                // Priorities: CSV Signal > Probabilities?
                // Using CSV Signal:
                /*
                 * String csv = analysis.getCsvSignal().toUpperCase();
                 * if (csv.contains("LONG") || csv.contains("BUY")) signal = "STEIGT";
                 * else if (csv.contains("SHORT") || csv.contains("SELL")) signal = "FAELLT";
                 * else signal = "SEITWAERTS";
                 */

                // Better approach: Logic based on probability fields (which we extracted)
                // However, user said "CsvSignal" is reliable? "CSV_SIGNAL: NEUTRAL".
                // Let's use Probabilities for direction if CSV is Neutral? Or use CSV Signal?
                // User asked for "steigt, seitwärts, fällt".

                int up = parsePercentage(analysis.getUpProbability());
                int down = parsePercentage(analysis.getDownProbability());
                int side = parsePercentage(analysis.getSidewaysProbability());

                if (up > side && up > down)
                    signal = "STEIGT";
                else if (down > side && down > up)
                    signal = "FAELLT";
                else
                    signal = "SEITWAERTS";

                // Fallback / Override from CSV if needed
                // If CSV_SIGNAL says LONG but probability slightly side?
                // Let's stick to probability dominance for "Trend".
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
