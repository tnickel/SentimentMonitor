package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;

public class TestPanicLogic {
    public static void main(String[] args) {
        String content = "### SECTION_2_SIGNAL_HERLEITUNG\n" +
                "SCHRITT 1 - Panic Check:\n" +
                "Status: Sicher (kein PANIC). VIX ≈ 14–15 → Normal/ruhig, keine akute Panik.\n";

        FullAnalysisParser parser = new FullAnalysisParser();
        FullAnalysisData data = parser.parseFullAnalysis(content);

        System.out.println("Extracted Panic Status: '" + data.getPanicStatus() + "'");

        boolean isPanicRaw = data.getPanicStatus().toUpperCase().contains("PANIC");
        System.out.println("Logic 'contains(PANIC)': " + isPanicRaw);

        if (isPanicRaw) {
            System.out.println("RESULT: FALSE POSITIVE PANIC DETECTED!");
        } else {
            System.out.println("RESULT: Correctly identified as safe.");
        }
    }
}
