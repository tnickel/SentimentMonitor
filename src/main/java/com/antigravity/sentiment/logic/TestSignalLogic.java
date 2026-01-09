package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestSignalLogic {
    public static void main(String[] args) {
        String content = "### SECTION_1_MEAN_REVERSION_SETUP\n" +
                "Reversion-Wahrscheinlichkeit: 65%  \n" +
                "Trend-Fortsetzungs-Risiko: 35%  \n" +
                "\n" +
                "### SECTION_3_ROBOTER_SIGNAL\n" +
                "CSV_SIGNAL: BUY\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Up: " + data.getUpProbability());
        System.out.println("Down: " + data.getDownProbability());
        System.out.println("Sideways: " + data.getSidewaysProbability());
        System.out.println("CSV Signal: " + data.getCsvSignal());

        // Simulation of SentimentCrawler logic
        String signal = "NEUTRAL";

        int up = parse(data.getUpProbability());
        int down = parse(data.getDownProbability());
        int side = parse(data.getSidewaysProbability());

        System.out.println("Parsed Ints -> Up: " + up + ", Down: " + down + ", Side: " + side);

        if (up > side && up > down)
            signal = "STEIGT";
        else if (down > side && down > up)
            signal = "FAELLT";
        else
            signal = "SEITWAERTS";

        System.out.println("Calculated Signal (Probabilities only): " + signal);
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s.replace("%", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
