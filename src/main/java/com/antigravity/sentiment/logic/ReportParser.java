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

    /**
     * Regex to capture ISO Date: "Datum: 2026-01-04"
     */
    private static final Pattern DATE_PATTERN_ISO = Pattern.compile(
            "Datum:\\s*(\\d{4})-(\\d{1,2})-(\\d{1,2})");

    /**
     * Regex to capture German Numeric Date: "Datum: 05.01.2026"
     */
    private static final Pattern DATE_PATTERN_NUMERIC_DE = Pattern.compile(
            "Datum:\\s*(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})");

    // Matches percentages for specific keywords
    // Matches percentages for specific keywords (Case insensitive)
    // Strategy: Try PATTERN 1, if fail try PATTERN 2, etc. (Fallback logic)

    private static final List<Pattern> UP_PATTERNS = new ArrayList<>();
    private static final List<Pattern> SIDE_PATTERNS = new ArrayList<>();
    private static final List<Pattern> DOWN_PATTERNS = new ArrayList<>();

    static {
        // UP Patterns
        UP_PATTERNS.add(Pattern.compile("(?i)Wahrscheinlichkeit.*?steigt.*?:?\\s*(\\d+)%"));
        UP_PATTERNS.add(Pattern.compile("(?i)steigt.*?:?\\s*(\\d+)%"));
        UP_PATTERNS.add(Pattern.compile("(?i)bullisch.*?:?\\s*(\\d+)%"));
        UP_PATTERNS.add(Pattern.compile("(?i)up.*?:?\\s*(\\d+)%"));

        // SIDE Patterns
        SIDE_PATTERNS.add(Pattern.compile("(?i)Wahrscheinlichkeit.*?seitwärts.*?:?\\s*(\\d+)%"));
        SIDE_PATTERNS.add(Pattern.compile("(?i)seitwärts.*?:?\\s*(\\d+)%"));
        SIDE_PATTERNS.add(Pattern.compile("(?i)neutral.*?:?\\s*(\\d+)%"));
        SIDE_PATTERNS.add(Pattern.compile("(?i)flat.*?:?\\s*(\\d+)%"));

        // DOWN Patterns
        DOWN_PATTERNS.add(Pattern.compile("(?i)Wahrscheinlichkeit.*?fällt.*?:?\\s*(\\d+)%"));
        DOWN_PATTERNS.add(Pattern.compile("(?i)fällt.*?:?\\s*(\\d+)%"));
        DOWN_PATTERNS.add(Pattern.compile("(?i)bärisch.*?:?\\s*(\\d+)%"));
        DOWN_PATTERNS.add(Pattern.compile("(?i)down.*?:?\\s*(\\d+)%"));
    }

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

            // Check for explicit "Datum:" ISO format first
            Matcher isoMatcher = DATE_PATTERN_ISO.matcher(line);
            Matcher deMatcher = DATE_PATTERN_NUMERIC_DE.matcher(line);

            if (isoMatcher.find()) {
                String year = isoMatcher.group(1);
                String month = isoMatcher.group(2);
                String day = isoMatcher.group(3);

                if (currentForecast != null && currentForecast.hasData()) {
                    results.add(currentForecast);
                }

                currentForecast = new DayForecast();
                // Format: 4.1.26 (Removing leading zero from month for consistency)
                int m = Integer.parseInt(month);
                currentForecast.date = day + "." + m + "." + year.substring(2);
            }
            // Check for explicit "Datum:" German Numeric format "05.01.2026"
            else if (deMatcher.find()) {
                String day = deMatcher.group(1);
                String month = deMatcher.group(2);
                String year = deMatcher.group(3);

                if (currentForecast != null && currentForecast.hasData()) {
                    results.add(currentForecast);
                }

                currentForecast = new DayForecast();
                int d = Integer.parseInt(day);
                int m = Integer.parseInt(month);
                String shortYear = year.length() == 4 ? year.substring(2) : year;
                currentForecast.date = d + "." + m + "." + shortYear;
            }
            // Check textual format
            else {
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
            }

            // Try to match Stats (always check, in case it's on the same line or subsequent
            // lines)
            if (currentForecast != null) {
                // Check Up
                String upVal = findFirstMatch(line, UP_PATTERNS);
                if (upVal != null)
                    currentForecast.up = upVal + "%";

                // Check Side
                String sideVal = findFirstMatch(line, SIDE_PATTERNS);
                if (sideVal != null)
                    currentForecast.sideways = sideVal + "%";

                // Check Down
                String downVal = findFirstMatch(line, DOWN_PATTERNS);
                if (downVal != null)
                    currentForecast.down = downVal + "%";
            }
        }

        // Add final
        if (currentForecast != null && currentForecast.hasData()) {
            results.add(currentForecast);
        }

        return results;
    }

    private String findFirstMatch(String line, List<Pattern> patterns) {
        for (Pattern p : patterns) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
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
