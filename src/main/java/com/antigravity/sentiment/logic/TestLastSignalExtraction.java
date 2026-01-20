package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.ForecastData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TestLastSignalExtraction {

    public static void main(String[] args) {
        // Create temp structure
        File tempRoot = new File("temp_test_root");
        if (tempRoot.exists())
            deleteDir(tempRoot);
        tempRoot.mkdirs();

        File assetDir = new File(tempRoot, "TEST_ASSET");
        assetDir.mkdirs();

        // File 1: Old (Yesterday)
        String oldContent = "### SECTION_0_DATEN_BASIS\n" +
                "Ratio (Long/Short): 50% / 50%\n" +
                "### SECTION_1_RISIKO_PROFIL\n" +
                "Datum: 2026-01-19\n" +
                "Wahrscheinlichkeit Range/Reversion: 60%\n" +
                "Wahrscheinlichkeit Trend-Start: 40%\n" +
                "### SECTION_2_LOGISCHE_HERLEITUNG\n" +
                "Ergebnis Bias: NEUTRAL\n" +
                "### SECTION_3_ROBOTER_STEUERUNG\n" +
                "CSV_SIGNAL: BUY\n";

        File oldFile = new File(assetDir, "report_old.txt");
        try {
            Files.writeString(oldFile.toPath(), oldContent);
            oldFile.setLastModified(System.currentTimeMillis() - 86400000); // Yesterday
        } catch (IOException e) {
            e.printStackTrace();
        }

        // File 2: New (Today)
        String newContent = "### SECTION_0_DATEN_BASIS\n" +
                "Ratio (Long/Short): 60% / 40%\n" +
                "### SECTION_1_RISIKO_PROFIL\n" +
                "Datum: 2026-01-20\n" +
                "Wahrscheinlichkeit Range/Reversion: 50%\n" +
                "Wahrscheinlichkeit Trend-Start: 50%\n" +
                "### SECTION_2_LOGISCHE_HERLEITUNG\n" +
                "Ergebnis Bias: SHORT\n" +
                "### SECTION_3_ROBOTER_STEUERUNG\n" +
                "CSV_SIGNAL: SELL\n";

        File newFile = new File(assetDir, "report_new.txt");
        try {
            Files.writeString(newFile.toPath(), newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Test Crawler
        SentimentCrawler crawler = new SentimentCrawler();
        List<ForecastData> results = crawler.crawl(tempRoot.getAbsolutePath());

        if (results.size() > 0) {
            ForecastData data = results.get(0);
            System.out.println("Asset: " + data.getAsset());
            System.out.println("Current Signal: " + data.getSignal());
            System.out.println("Last Signal Field: " + data.getLastSignal());

            if (data.getLastSignal().contains("2026-01-19") && data.getLastSignal().contains("STEIGT")) {
                System.out.println("TEST PASSED: Last signal correctly identified as STEIGT (BUY) from 2026-01-19.");
            } else {
                System.out.println("TEST FAILED: Last signal expected to contain 2026-01-19 and STEIGT.");
            }
        } else {
            System.out.println("TEST FAILED: No results found.");
        }

        // Cleanup
        // deleteDir(tempRoot);
    }

    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}
