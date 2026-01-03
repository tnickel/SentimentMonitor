package com.antigravity.sentiment.logic;

import java.util.List;

public class TestParser {
    public static void main(String[] args) {
        // Simulating the input described by the user (Markdown style)
        String testContent = "**Montag, 5. Januar 2026:**\n" +
                "\n" +
                "Wahrscheinlichkeit, dass Gold steigt: 35%\n" +
                "Wahrscheinlichkeit, dass Gold seitwärts geht: 40%\n" +
                "Wahrscheinlichkeit, dass Gold fällt: 25%\n" +
                "\n" +
                "**Dienstag, 6. Januar 2026:**\n" +
                "\n" +
                "Wahrscheinlichkeit, dass Gold steigt: 40%\n" +
                "Wahrscheinlichkeit, dass Gold seitwärts geht: 30%\n" +
                "Wahrscheinlichkeit, dass Gold fällt: 30%\n" +
                "\n" +
                "Für die kommende Woche vom 05.01. bis 10.01.2026 gilt:"; // This garbage line might be matched by old
                                                                          // regex

        System.out.println("Processing content...");
        ReportParser parser = new ReportParser();
        List<ReportParser.DayForecast> results = parser.parseContent(testContent);

        for (ReportParser.DayForecast df : results) {
            System.out.println("Result -> Date: '" + df.date + "' | Up: " + df.up + " | Side: " + df.sideways
                    + " | Down: " + df.down);
        }
    }
}
