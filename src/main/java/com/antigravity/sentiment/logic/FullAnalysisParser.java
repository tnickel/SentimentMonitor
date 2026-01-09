package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for new format full analysis files (Sections 0-4).
 * Updated to support "Refined Format" variations.
 */
public class FullAnalysisParser {

    // Regex for FXSSI (Old & New)
    private static final Pattern FXSSI_RATIO_OLD = Pattern
            .compile("Ratio \\(Long/Short\\):\\s*(\\d+)%\\s*/\\s*(\\d+)%");
    private static final Pattern FXSSI_LONG_NEW = Pattern.compile("-\\s*Long Position:\\s*(\\d+)%");
    private static final Pattern FXSSI_SHORT_NEW = Pattern.compile("-\\s*Short Position:\\s*(\\d+)%");

    // Regex for probabilities in Section 1 (Old & New)
    private static final Pattern TREND_START_PATTERN = Pattern.compile(
            "(?i)(?:Wahrscheinlichkeit Trend-Start|Trend-Fortsetzungs-Risiko|Runaway-Trend Risiko).*?:\\s*(\\d+)%");
    private static final Pattern RANGE_REV_PATTERN = Pattern
            .compile("(?i)(?:Wahrscheinlichkeit Range/Reversion|Reversion-Wahrscheinlichkeit).*?:\\s*(\\d+)%");
    private static final Pattern RANGE_STAB_PATTERN = Pattern
            .compile("(?i)(?:Erwartete Range Stabilitaet|Range-Trading Chance).*?:\\s*(\\d+)%");

    // Regex for basic properties
    private static final Pattern CSV_SIGNAL_PATTERN = Pattern.compile("CSV_SIGNAL:\\s*(\\w+)");
    private static final Pattern BIAS_PATTERN = Pattern.compile("(?:Ergebnis|Finaler)\\s*Bias:\\s*(.*?)(\\(|$)");
    private static final Pattern DATE_PATTERN_ISO = Pattern.compile("Datum:\\s*(\\d{4}-\\d{2}-\\d{2})");

    public FullAnalysisData parseFullAnalysis(String content) {
        FullAnalysisData data = new FullAnalysisData();
        data.setRawContent(content);

        try {
            parseSection0(content, data);
            parseSection1(content, data);
            parseSection2And3(content, data); // Derivation & Control
            parseSection4(content, data); // Rationales
        } catch (Exception e) {
            System.err.println("Error parsing full analysis: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    private void parseSection0(String content, FullAnalysisData data) {
        // Try precise block first, fallback to loose check
        String section = extractSectionBlock(content, "### SECTION_0", "### SECTION_1");
        // Fallback for ## SECTION_0 (if user used double hash instead of triple)
        if (section.isEmpty())
            section = extractSectionBlock(content, "## SECTION_0", "## SECTION_1");

        if (section.isEmpty())
            return;

        // Try Old Format
        Matcher mOld = FXSSI_RATIO_OLD.matcher(section);
        if (mOld.find()) {
            data.setFxssiLong(mOld.group(1) + "%");
            data.setFxssiShort(mOld.group(2) + "%");
            return;
        }

        // Try New Format
        Matcher mLong = FXSSI_LONG_NEW.matcher(section);
        Matcher mShort = FXSSI_SHORT_NEW.matcher(section);

        if (mLong.find())
            data.setFxssiLong(mLong.group(1) + "%");
        if (mShort.find())
            data.setFxssiShort(mShort.group(1) + "%");

        // Extract Analyst Consensus
        // Pattern: Starts with "ANALYSTEN-KONSENS" and goes until "KONFLIKT-ANALYSE" or
        // "Technische Überdehnung"
        Pattern consensusPattern = Pattern.compile(
                "(?s)(ANALYSTEN-KONSENS.*?)(?=(?:KONFLIKT-ANALYSE|Technische Überdehnung|### SECTION|## SECTION|$))");
        Matcher cm = consensusPattern.matcher(section);
        if (cm.find()) {
            data.setAnalystConsensus(cm.group(1).trim());

            // Extract numeric consensus (e.g. "2 von 3")
            Pattern numPat = Pattern.compile("Konsens:.*?(\\d+\\s+von\\s+\\d+)");
            Matcher numM = numPat.matcher(cm.group(1));
            if (numM.find()) {
                data.setConsensusNumbers(numM.group(1));
            }
        }

        // VIX
        Matcher vixM = Pattern.compile("VIX Index:.*?([\\d\\.\\-]+)").matcher(section);
        if (vixM.find())
            data.setVix(vixM.group(1).trim());

        // RSI
        Matcher rsiM = Pattern.compile("RSI.*?:\\s*([\\d\\.]+)").matcher(section);
        if (rsiM.find())
            data.setRsi(rsiM.group(1).trim());

        // ATR
        Matcher atrM = Pattern.compile("ATR:.*?([\\d\\.]+)").matcher(section);
        if (atrM.find())
            data.setAtr(atrM.group(1).trim());
    }

    private void parseSection1(String content, FullAnalysisData data) {
        String section = extractSectionBlock(content, "### SECTION_1", "### SECTION_2");
        if (section.isEmpty())
            section = extractSectionBlock(content, "## SECTION_1", "## SECTION_2"); // Fallback

        if (section.isEmpty())
            return;

        // Date - Also check definition in Section 0 if missing here
        Matcher dm = DATE_PATTERN_ISO.matcher(content); // Check full content for date to be safe
        if (dm.find()) {
            data.setDate(dm.group(1));
        }

        // Probabilities
        Matcher rm = RANGE_REV_PATTERN.matcher(section);
        Matcher tm = TREND_START_PATTERN.matcher(section);
        Matcher rs = RANGE_STAB_PATTERN.matcher(section);

        int side = 0;
        int trend = 0;

        StringBuilder calculationLog = new StringBuilder("Berechnungsgrundlage:\n");

        // Priority for Sideways: Range-Trading Chance > Range Stabilitaet > Reversion
        if (rs.find()) {
            side = parsePercentage(rs.group(1));
            calculationLog.append("- Gefunden: 'Range-Trading Chance/Stabilität' (").append(side)
                    .append("%) -> Interpretiert als SEITWÄRTS.\n");
        } else if (rm.find()) {
            side = parsePercentage(rm.group(1));
            calculationLog.append("- Gefunden: 'Reversion/Range' (").append(side)
                    .append("%) -> Interpretiert als SEITWÄRTS.\n");
        }

        if (tm.find()) {
            trend = parsePercentage(tm.group(1));
            calculationLog.append("- Gefunden: 'Trend/Runaway Risiko' (").append(trend)
                    .append("%) -> Aufgeteilt auf STEIGT/FÄLLT.\n");
        }

        data.setSidewaysProbability(side + "%");

        if (trend > 0) {
            int split = trend / 2;
            data.setUpProbability(split + "%");
            data.setDownProbability(split + "%");
            calculationLog.append("  -> ").append(split).append("% Steigt, ").append(split)
                    .append("% Fällt (Split).\n");
        } else {
            // Remainder split
            int remainder = 100 - side;
            if (remainder > 0) {
                int split = remainder / 2;
                data.setUpProbability(split + "%");
                data.setDownProbability(split + "%");
                calculationLog.append("- Rest (").append(remainder)
                        .append("%) -> Aufgeteilt auf STEIGT/FÄLLT (Split).\n");
                calculationLog.append("  -> ").append(split).append("% Steigt, ").append(split).append("% Fällt.\n");
            }
        }

        data.setProbabilityCalculation(calculationLog.toString());
    }

    private void parseSection2And3(String content, FullAnalysisData data) {
        String sec2 = extractSectionBlock(content, "### SECTION_2", "### SECTION_3");
        if (sec2.isEmpty())
            sec2 = extractSectionBlock(content, "## SECTION_2", "## SECTION_3");

        String sec3 = extractSectionBlock(content, "### SECTION_3", "### SECTION_4");
        if (sec3.isEmpty())
            sec3 = extractSectionBlock(content, "## SECTION_3", "## SECTION_4");

        StringBuilder combined = new StringBuilder();
        if (!sec2.isEmpty()) {
            combined.append("HERLEITUNG:\n").append(sec2).append("\n\n");

            Matcher bm = BIAS_PATTERN.matcher(sec2);
            if (bm.find())
                data.setBias(bm.group(1).trim());

            // Panic Check
            Matcher pm = Pattern.compile("(?s)Panic Check:.*?Status:\\s*(.*?)(?=\\n)").matcher(sec2);
            if (pm.find()) {
                data.setPanicStatus(pm.group(1).trim());
            }
        }

        if (!sec3.isEmpty()) {
            combined.append("STEUERUNG:\n").append(sec3);

            Matcher cm = CSV_SIGNAL_PATTERN.matcher(sec3);
            if (cm.find())
                data.setCsvSignal(cm.group(1).trim());
        }

        data.setDerivationText(combined.toString());
    }

    private void parseSection4(String content, FullAnalysisData data) {
        String section = extractSectionBlock(content, "### SECTION_4", "END_OF_FILE");
        if (section.isEmpty())
            section = extractSectionBlock(content, "## SECTION_4", "END_OF_FILE");

        if (section.isEmpty())
            return;

        Map<String, String> rationals = new LinkedHashMap<>();

        // Split by "N) Title"
        Pattern p = Pattern.compile("(?m)^\\d+\\)\\s*(.*?)$");
        Matcher m = p.matcher(section);

        int lastMatchEnd = -1;
        String lastTitle = null;

        while (m.find()) {
            if (lastTitle != null) {
                String body = section.substring(lastMatchEnd, m.start()).trim();
                rationals.put(lastTitle, body);
            }
            lastTitle = m.group(1).trim();
            lastMatchEnd = m.end();
        }

        if (lastTitle != null) {
            String body = section.substring(lastMatchEnd).trim();
            rationals.put(lastTitle, body);
        }

        data.setRationales(rationals);
    }

    private String extractSectionBlock(String content, String startTag, String nextTag) {
        int start = content.indexOf(startTag);
        if (start == -1)
            return "";

        int end = content.indexOf(nextTag, start);
        if (end == -1)
            end = content.length();

        int headerEnd = content.indexOf('\n', start);
        if (headerEnd != -1 && headerEnd < end) {
            return content.substring(headerEnd + 1, end).trim();
        }

        return content.substring(start + startTag.length(), end).trim();
    }

    private int parsePercentage(String s) {
        try {
            return Integer.parseInt(s.replace("%", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
