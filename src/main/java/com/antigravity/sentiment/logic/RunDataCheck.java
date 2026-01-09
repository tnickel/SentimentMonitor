package com.antigravity.sentiment.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RunDataCheck {

    // Use ReportParser directly
    private static final ReportParser parser = new ReportParser();

    public static void main(String[] args) {
        String path = "C:\\Users\\tnickel\\.n8n-files";
        System.out.println("Checking data in: " + path);

        File root = new File(path);
        if (!root.exists()) {
            System.err.println("ERROR: Path does not exist!");
            return;
        }

        System.out.println("Directory exists. Is Directory: " + root.isDirectory());

        // Custom crawl logic to avoid SentimentCrawler -> ForecastData -> JavaFX
        // dependency
        findAssetFolders(root);
    }

    private static void findAssetFolders(File directory) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        // Check for txt files directly
        File[] textFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles != null && textFiles.length > 0) {
            processAssetFolder(directory);
        }

        for (File f : files) {
            if (f.isDirectory()) {
                findAssetFolders(f);
            }
        }
    }

    private static void processAssetFolder(File assetDir) {
        String assetName = assetDir.getName();

        // Find newest .txt
        File[] textFiles = assetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null || textFiles.length == 0)
            return;

        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified).reversed());
        File newestFile = textFiles[0];

        System.out.println("------------------------------------------------");
        System.out.println("ASSET: " + assetName);
        System.out.println("Reading file: " + newestFile.getName());

        try {
            String content = Files.readString(newestFile.toPath());
            content = content.replaceAll("(?i)Gold", assetName); // Mimic crawling logic

            List<ReportParser.DayForecast> forecasts = parser.parseContent(content);

            if (forecasts.isEmpty()) {
                System.out.println("  -> NO DATA PARSED.");
            } else {
                for (int i = 0; i < forecasts.size(); i++) {
                    ReportParser.DayForecast df = forecasts.get(i);
                    String label = (i == 0) ? "Day 1" : (i == 1) ? "Day 2" : "6 Month";
                    System.out.println("  " + label + ": " + df.date);
                    System.out.println("     Up: " + df.up + " | Side: " + df.sideways + " | Down: " + df.down);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + newestFile + ": " + e.getMessage());
        }
    }
}
