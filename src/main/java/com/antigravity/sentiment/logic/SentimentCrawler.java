package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.ForecastData;
import com.antigravity.sentiment.model.HistoryData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SentimentCrawler {

    private final ReportParser parser = new ReportParser();

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

        // Check if this directory contains .txt files (new structure: files directly in
        // asset folder)
        File[] textFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles != null && textFiles.length > 0) {
            // This is an asset folder with forecast files
            processAssetFolder(directory, results);
        }

        // Recursively continue searching subdirectories
        for (File f : files) {
            if (f.isDirectory()) {
                findAssetFolders(f, results);
            }
        }
    }

    private void processAssetFolder(File assetDir, List<ForecastData> results) {
        String assetName = assetDir.getName();

        // Find newest .txt file in the asset directory
        File[] textFiles = assetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null || textFiles.length == 0)
            return;

        // Sort by last modified descending
        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified).reversed());
        File newestFile = textFiles[0];

        try {
            String content = Files.readString(newestFile.toPath());
            content = content.replaceAll("(?i)Gold", assetName);

            List<ReportParser.DayForecast> forecasts = parser.parseContent(content);

            if (forecasts.size() >= 1) {
                ReportParser.DayForecast d1 = forecasts.get(0);
                ReportParser.DayForecast d2 = (forecasts.size() > 1) ? forecasts.get(1)
                        : new ReportParser.DayForecast();
                ReportParser.DayForecast d6m = (forecasts.size() > 2) ? forecasts.get(2)
                        : new ReportParser.DayForecast();

                ForecastData data = new ForecastData(
                        assetName, assetDir.getAbsolutePath(),
                        d1.date, d1.up, d1.sideways, d1.down,
                        d2.date, d2.up, d2.sideways, d2.down,
                        d6m.date, d6m.up, d6m.sideways, d6m.down);
                results.add(data);
            }

        } catch (IOException e) {
            System.err.println("Error reading file " + newestFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    public List<HistoryData> loadHistory(String assetPath) {
        List<HistoryData> history = new ArrayList<>();
        File assetDir = new File(assetPath);

        if (!assetDir.exists() || !assetDir.isDirectory())
            return history;

        // Read .txt files directly from asset directory
        File[] textFiles = assetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null)
            return history;

        // Sort by last modified descending
        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified).reversed());

        for (File file : textFiles) {
            try {
                String content = Files.readString(file.toPath());
                content = content.replaceAll("(?i)Gold", assetDir.getName());

                List<ReportParser.DayForecast> parsed = parser.parseContent(content);
                for (ReportParser.DayForecast pf : parsed) {
                    history.add(new HistoryData(
                            pf.date, pf.up, pf.sideways, pf.down, file.getAbsolutePath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return history;
    }
}
