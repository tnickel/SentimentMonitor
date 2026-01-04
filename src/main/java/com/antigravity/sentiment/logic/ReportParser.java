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
     * "**Montag, 5. Januar 2026:**" or "Datum: 4. Januar 2026"
     * 
     * Groups:
     * 1: Optional Prefix/Weekday (e.g. "Montag", "Datum", "Am")
     * 2: Day (e.g. 5)
     * 3: Month Name (e.g. Januar)
     * 4: Year (e.g. 2026)
     */
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?:\\*\\*)?\\s*([A-Za-zäöüÄÖÜß]+).*?\\s*(\\d{1,2})\\.\\s*([A-Za-zäöüÄÖÜß]+)\\s*(\\d{4})(?:.*)?$");

    // Matches percentages for specific keywords
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
        // Short forms
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
        // Split by new line, but mostly processed line-by-line
        String[] lines = content.split("\\R");

        DayForecast currentForecast = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            // Try to match Date
            Matcher dateMatcher = DATE_PATTERN.matcher(line);
            if (dateMatcher.find()) {
                String prefix = dateMatcher.group(1); // Montag, Datum, etc
                String day = dateMatcher.group(2); // 5
                String month = dateMatcher.group(3); // Januar
                String year = dateMatcher.group(4); // 2026

                // If "bis" is found, it's likely a range summary line -> Skip
                if (line.toLowerCase().contains(" bis "))
                    continue;

                // Save previous forecast if exists
                if (currentForecast != null) {
                    if (currentForecast.hasData())
                        results.add(currentForecast);
                }

                currentForecast = new DayForecast();
                currentForecast.date = formatDate(prefix, day, month, year);

                // CRITICAL FIX: Do NOT continue here.
                // The probability data might be on the SAME line as the date.
            }

            // Try to match Stats (always check, in case it's on the same line or subsequent
            // lines)
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

    private String formatDate(String prefix, String day, String monthName, String year) {
        // "Montag" -> "Mo", "Datum" -> ""
        String weekday = "";
        if (prefix != null && !prefix.equalsIgnoreCase("Datum") && !prefix.equalsIgnoreCase("Am")) {
            weekday = prefix.length() >= 2 ? prefix.substring(0, 2) + " " : "";
        }

        String monthNum = MONTH_MAP.getOrDefault(monthName, monthName);
        String shortYear = year.length() == 4 ? year.substring(2) : year;

        // Result: "Mo 5.1.26" or "5.1.26"
        return weekday + day + "." + monthNum + "." + shortYear;
    }
}
