package com.antigravity.sentiment.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportParser {

    /**
     * Regex to capture Date from lines like:
     * "**Montag, 5. Januar 2026:**" or "Dienstag, 6. Januar 2026"
     * 
     * Groups:
     * 1: Weekday (e.g. Montag)
     * 2: Day (e.g. 5)
     * 3: Month Name (e.g. Januar)
     * 4: Year (e.g. 2026)
     */
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?:\\*\\*)?\\s*([A-Za-zäöüÄÖÜß]+),?\\s*(\\d{1,2})\\.\\s*([A-Za-zäöüÄÖÜß]+)\\s*(\\d{4})(?:.*)?$");

    // Matches percentages for specific keywords
    // We allow "steigt", "seitwärts", "fällt" and find the number % typically at
    // the end.
    private static final Pattern UP_PATTERN = Pattern.compile("steigt.*?:?\\s*(\\d+)%");
    private static final Pattern SIDE_PATTERN = Pattern.compile("seitwärts.*?:?\\s*(\\d+)%");
    private static final Pattern DOWN_PATTERN = Pattern.compile("fällt.*?:?\\s*(\\d+)%");

    // Month mapping for formatting
    private static final Map<String, String> MONTH_MAP = new HashMap<>();
    static {
        MONTH_MAP.put("Januar", "1");
        MONTH_MAP.put("Februar", "2");
        MONTH_MAP.put("März", "3");
        MONTH_MAP.put("April", "4");
        MONTH_MAP.put("Mai", "5");
        MONTH_MAP.put("Juni", "6");
        MONTH_MAP.put("Juli", "7");
        MONTH_MAP.put("August", "8");
        MONTH_MAP.put("September", "9");
        MONTH_MAP.put("Oktober", "10");
        MONTH_MAP.put("November", "11");
        MONTH_MAP.put("Dezember", "12");
        // Short forms just in case
        MONTH_MAP.put("Jan", "1");
        MONTH_MAP.put("Feb", "2");
        MONTH_MAP.put("Mrz", "3");
        MONTH_MAP.put("Apr", "4");
        MONTH_MAP.put("Jun", "6");
        MONTH_MAP.put("Jul", "7");
        MONTH_MAP.put("Aug", "8");
        MONTH_MAP.put("Sep", "9");
        MONTH_MAP.put("Okt", "10");
        MONTH_MAP.put("Nov", "11");
        MONTH_MAP.put("Dez", "12");
    }

    public static class DayForecast {
        public String date = "";
        public String up = "0%";
        public String sideways = "0%";
        public String down = "0%";

        public boolean hasData() {
            return !up.equals("0%") || !sideways.equals("0%") || !down.equals("0%");
        }
    }

    public List<DayForecast> parseContent(String content) {
        List<DayForecast> results = new ArrayList<>();
        String[] lines = content.split("\\R");

        DayForecast currentForecast = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            // Try to match Date
            Matcher dateMatcher = DATE_PATTERN.matcher(line);
            if (dateMatcher.find()) { // find() allows matching even if surrounded by other chars, but regex is
                                      // anchored reasonably
                // Filter out lines that might look like dates but are sentences ("... bis
                // 10.01.2026 gilt:")
                // The regex enforces "Weekday, Day. Month Year".
                // Let's verify we captured groups.
                String weekday = dateMatcher.group(1); // Montag
                String day = dateMatcher.group(2); // 5
                String month = dateMatcher.group(3); // Januar
                String year = dateMatcher.group(4); // 2026

                // If the line contains "bis", likely a range summary -> Skip
                if (line.toLowerCase().contains(" bis "))
                    continue;

                if (currentForecast != null) {
                    if (currentForecast.hasData())
                        results.add(currentForecast);
                }

                currentForecast = new DayForecast();
                currentForecast.date = formatDate(weekday, day, month, year);
                continue;
            }

            // Try to match Stats if we have an active date
            if (currentForecast != null) {
                // Check Up
                Matcher upM = UP_PATTERN.matcher(line);
                if (upM.find())
                    currentForecast.up = upM.group(1) + "%";

                // Check Side
                Matcher sideM = SIDE_PATTERN.matcher(line);
                if (sideM.find())
                    currentForecast.sideways = sideM.group(1) + "%";

                // Check Down
                Matcher downM = DOWN_PATTERN.matcher(line);
                if (downM.find())
                    currentForecast.down = downM.group(1) + "%";
            }
        }

        // Add final
        if (currentForecast != null && currentForecast.hasData()) {
            results.add(currentForecast);
        }

        return results;
    }

    private String formatDate(String weekday, String day, String monthName, String year) {
        // Target: "Mo 5.1.26"
        String shortDay = weekday.length() >= 2 ? weekday.substring(0, 2) : weekday;
        String monthNum = MONTH_MAP.getOrDefault(monthName, monthName);
        String shortYear = year.length() == 4 ? year.substring(2) : year;

        return shortDay + " " + day + "." + monthNum + "." + shortYear;
    }
}
