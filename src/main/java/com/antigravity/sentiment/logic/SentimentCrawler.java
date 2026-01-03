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

        // Check if this directory is an Asset folder (has "2tage" subdir)
        File subDir = new File(directory, "2tage");
        if (subDir.exists() && subDir.isDirectory()) {
            processAssetFolder(directory, subDir, results);
        }

        // Recursively continue
        for (File f : files) {
            if (f.isDirectory() && !f.getName().equals("2tage")) {
                findAssetFolders(f, results);
            }
        }
    }

    private void processAssetFolder(File assetDir, File twoDaysDir, List<ForecastData> results) {
        String assetName = assetDir.getName();

        // Find newest file in 2tage
        File[] textFiles = twoDaysDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
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

                ForecastData data = new ForecastData(
                        assetName, assetDir.getAbsolutePath(),
                        d1.date, d1.up, d1.sideways, d1.down,
                        d2.date, d2.up, d2.sideways, d2.down);
                results.add(data);
            }

        } catch (IOException e) {
            System.err.println("Error reading file " + newestFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    public List<HistoryData> loadHistory(String assetPath) {
        List<HistoryData> history = new ArrayList<>();
        File assetDir = new File(assetPath);
        File twoDaysDir = new File(assetDir, "2tage");

        if (!twoDaysDir.exists() || !twoDaysDir.isDirectory())
            return history;

        File[] textFiles = twoDaysDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null)
            return history;

        // We can optionally sort files by name or date, but we will just read all.
        // Let's sort by name (date usually) if possible, or last modified.
        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified).reversed());

        for (File file : textFiles) {
            try {
                String content = Files.readString(file.toPath());
                content = content.replaceAll("(?i)Gold", assetDir.getName());

                List<ReportParser.DayForecast> parsed = parser.parseContent(content);
                for (ReportParser.DayForecast pf : parsed) {
                    history.add(new HistoryData(
                            pf.date, pf.up, pf.sideways, pf.down));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return history;
    }
}
