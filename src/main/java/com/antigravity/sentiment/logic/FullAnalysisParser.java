package com.antigravity.sentiment.logic;

import com.antigravity.sentiment.model.FullAnalysisData;
import com.antigravity.sentiment.model.FullAnalysisData.ComponentScore;
import com.antigravity.sentiment.model.FullAnalysisData.TradingSetup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for full analysis files with robust error handling.
 * Designed to handle variations in AI-generated content.
 */
public class FullAnalysisParser {

    // Patterns for TEIL 1 (with tolerance for variations)
    private static final Pattern SPOT_PRICE_PATTERN = Pattern.compile(
            "(?:Spot[‑-]?(?:Referenz)?[‑-]?preis|Referenzpreis).*?[~≈]?\\s*([\\d,\\.]+)\\s*(?:USD|\\$)",
            Pattern.CASE_INSENSITIVE);

    // Patterns for TEIL 2 (Component scores)
    private static final Pattern CHARTTECHNIK_PATTERN = Pattern.compile(
            "Charttechnik.*?\\((\\d+)%.*?Gewicht\\).*?(?:Bull[‑-]?score|Score)\\s*(\\d+)%.*?Beitrag.*?(\\d+\\.\\d+)%",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SENTIMENT_PATTERN = Pattern.compile(
            "Sentiment.*?\\((\\d+)%.*?Gewicht\\).*?(?:Bull[‑-]?score|Score)\\s*(\\d+)%.*?Beitrag.*?(\\d+\\.\\d+)%",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern MAKRO_PATTERN = Pattern.compile(
            "Makro.*?\\((\\d+)%.*?Gewicht\\).*?(?:Bull[‑-]?score|Score)\\s*(\\d+)%.*?Beitrag.*?(\\d+\\.\\d+)%",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern TOTAL_BULL_PATTERN = Pattern.compile(
            "Gesamt\\s+Bullwahrscheinlichkeit.*?=\\s*([\\d\\.]+)%",
            Pattern.CASE_INSENSITIVE);

    // Patterns for TEIL 3 (Trading Setup)
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "Entry.*?:\\s*([\\d,\\.]+)\\s*(?:USD|\\$)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern STOP_LOSS_PATTERN = Pattern.compile(
            "Stop\\s*Loss.*?:\\s*([\\d,\\.]+)\\s*(?:USD|\\$)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TAKE_PROFIT_PATTERN = Pattern.compile(
            "Take\\s*Profit.*?:\\s*([\\d,\\.]+)\\s*(?:USD|\\$)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern RISK_REWARD_PATTERN = Pattern.compile(
            "(?:Risiko[/\\s]*Chance|CRV|Risk[/\\s]*Reward).*?:\\s*([\\d\\.]+)\\s*:?\\s*1",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern RISK_PATTERN = Pattern.compile(
            "Risk.*?[≈~]?\\s*([\\d,\\.]+)\\s*(?:USD|\\$)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern REWARD_PATTERN = Pattern.compile(
            "Reward.*?[≈~]?\\s*([\\d,\\.]+)\\s*(?:USD|\\$)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Parse the full analysis file content.
     * Uses safe defaults if sections are missing or malformed.
     */
    public FullAnalysisData parseFullAnalysis(String content) {
        FullAnalysisData data = new FullAnalysisData();
        data.setRawContent(content);

        try {
            // Parse TEIL 1 (Statistical data)
            parseTeil1(content, data);

            // Parse TEIL 2 (Numerical derivation)
            parseTeil2(content, data);

            // Parse TEIL 3 (Trading setup)
            parseTeil3(content, data);

            // Parse TEIL 4 (Detailed reasoning)
            parseTeil4(content, data);

        } catch (Exception e) {
            System.err.println("Error parsing full analysis: " + e.getMessage());
            e.printStackTrace();
            // Data object already has safe defaults
        }

        return data;
    }

    private void parseTeil1(String content, FullAnalysisData data) {
        try {
            // Find TEIL 1 section
            int teil1Start = content.indexOf("TEIL 1");
            int teil2Start = content.indexOf("TEIL 2");

            String teil1Content = (teil2Start > teil1Start && teil1Start != -1)
                    ? content.substring(teil1Start, teil2Start)
                    : (teil1Start != -1 ? content.substring(teil1Start) : content);

            // Extract first date entry (format: "Datum: 4. Januar 2026" OR "Datum:
            // 2026-01-04")
            Pattern datePattern = Pattern.compile("Datum:\\s*([\\d]+\\.\\s*[A-Za-zäöüÄÖÜß]+\\s*[\\d]{4})",
                    Pattern.CASE_INSENSITIVE);
            Matcher dateMatcher = datePattern.matcher(teil1Content);

            if (dateMatcher.find()) {
                data.setDate(dateMatcher.group(1).trim());
            } else {
                // Determine ISO format
                Pattern isoPattern = Pattern.compile("Datum:\\s*(\\d{4}-\\d{1,2}-\\d{1,2})");
                Matcher isoMatcher = isoPattern.matcher(teil1Content);
                if (isoMatcher.find()) {
                    data.setDate(isoMatcher.group(1).trim());
                }
            }

            // Extract probabilities from first entry
            // Pattern: "Wahrscheinlichkeit, dass Gold steigt: 44%"
            Pattern upPattern = Pattern.compile(
                    "Wahrscheinlichkeit.*?steigt.*?:\\s*(\\d+)%",
                    Pattern.CASE_INSENSITIVE);
            Pattern sidePattern = Pattern.compile(
                    "Wahrscheinlichkeit.*?seitwärts.*?:\\s*(\\d+)%",
                    Pattern.CASE_INSENSITIVE);
            Pattern downPattern = Pattern.compile(
                    "Wahrscheinlichkeit.*?fällt.*?:\\s*(\\d+)%",
                    Pattern.CASE_INSENSITIVE);

            Matcher upMatcher = upPattern.matcher(teil1Content);
            if (upMatcher.find()) {
                data.setUpProbability(upMatcher.group(1) + "%");
            }

            Matcher sideMatcher = sidePattern.matcher(teil1Content);
            if (sideMatcher.find()) {
                data.setSidewaysProbability(sideMatcher.group(1) + "%");
            }

            Matcher downMatcher = downPattern.matcher(teil1Content);
            if (downMatcher.find()) {
                data.setDownProbability(downMatcher.group(1) + "%");
            }

            // Extract spot price
            Matcher spotMatcher = SPOT_PRICE_PATTERN.matcher(content);
            if (spotMatcher.find()) {
                data.setSpotPrice(spotMatcher.group(1) + " USD");
            }

            // Extract sources (look for common source mentions)
            StringBuilder sources = new StringBuilder();
            if (content.contains("FXSSI"))
                sources.append("FXSSI, ");
            if (content.contains("TradingView"))
                sources.append("TradingView, ");
            if (content.contains("Fed"))
                sources.append("Fed Minutes, ");
            if (content.contains("J.P. Morgan") || content.contains("JPM"))
                sources.append("J.P. Morgan, ");

            String sourcesStr = sources.toString();
            if (sourcesStr.endsWith(", ")) {
                sourcesStr = sourcesStr.substring(0, sourcesStr.length() - 2);
            }
            data.setSources(sourcesStr);

        } catch (Exception e) {
            System.err.println("Error parsing TEIL 1: " + e.getMessage());
        }
    }

    private void parseTeil2(String content, FullAnalysisData data) {
        try {
            // Find TEIL 2 section
            int teil2Start = content.indexOf("TEIL 2");
            int teil3Start = content.indexOf("TEIL 3");

            if (teil2Start == -1)
                return;

            String teil2Content = (teil3Start > teil2Start)
                    ? content.substring(teil2Start, teil3Start)
                    : content.substring(teil2Start);

            // Parse Charttechnik
            Matcher chartMatcher = CHARTTECHNIK_PATTERN.matcher(teil2Content);
            if (chartMatcher.find()) {
                ComponentScore chart = new ComponentScore(
                        "Charttechnik",
                        chartMatcher.group(1) + "%",
                        chartMatcher.group(2) + "%",
                        chartMatcher.group(3) + "%");
                data.setCharttechnik(chart);
            }

            // Parse Sentiment
            Matcher sentMatcher = SENTIMENT_PATTERN.matcher(teil2Content);
            if (sentMatcher.find()) {
                ComponentScore sent = new ComponentScore(
                        "Sentiment",
                        sentMatcher.group(1) + "%",
                        sentMatcher.group(2) + "%",
                        sentMatcher.group(3) + "%");
                data.setSentiment(sent);
            }

            // Parse Makro
            Matcher makroMatcher = MAKRO_PATTERN.matcher(teil2Content);
            if (makroMatcher.find()) {
                ComponentScore makro = new ComponentScore(
                        "Makro/News",
                        makroMatcher.group(1) + "%",
                        makroMatcher.group(2) + "%",
                        makroMatcher.group(3) + "%");
                data.setMakro(makro);
            }

            // Parse total bull probability
            Matcher totalMatcher = TOTAL_BULL_PATTERN.matcher(teil2Content);
            if (totalMatcher.find()) {
                data.setTotalBullProbability(totalMatcher.group(1) + "%");
            }

        } catch (Exception e) {
            System.err.println("Error parsing TEIL 2: " + e.getMessage());
        }
    }

    private void parseTeil3(String content, FullAnalysisData data) {
        try {
            // Find TEIL 3 section
            int teil3Start = content.indexOf("TEIL 3");
            int teil4Start = content.indexOf("TEIL 4");

            if (teil3Start == -1)
                return;

            String teil3Content = (teil4Start > teil3Start)
                    ? content.substring(teil3Start, teil4Start)
                    : content.substring(teil3Start);

            TradingSetup setup = new TradingSetup();

            // Parse Entry
            Matcher entryMatcher = ENTRY_PATTERN.matcher(teil3Content);
            if (entryMatcher.find()) {
                setup.setEntry(entryMatcher.group(1) + " USD");
            }

            // Parse Stop Loss
            Matcher slMatcher = STOP_LOSS_PATTERN.matcher(teil3Content);
            if (slMatcher.find()) {
                setup.setStopLoss(slMatcher.group(1) + " USD");
            }

            // Parse Take Profit
            Matcher tpMatcher = TAKE_PROFIT_PATTERN.matcher(teil3Content);
            if (tpMatcher.find()) {
                setup.setTakeProfit(tpMatcher.group(1) + " USD");
            }

            // Parse Risk/Reward
            Matcher rrMatcher = RISK_REWARD_PATTERN.matcher(teil3Content);
            if (rrMatcher.find()) {
                setup.setRiskReward(rrMatcher.group(1) + " : 1");
            }

            // Parse Risk
            Matcher riskMatcher = RISK_PATTERN.matcher(teil3Content);
            if (riskMatcher.find()) {
                setup.setRisk(riskMatcher.group(1) + " USD");
            }

            // Parse Reward
            Matcher rewardMatcher = REWARD_PATTERN.matcher(teil3Content);
            if (rewardMatcher.find()) {
                setup.setReward(rewardMatcher.group(1) + " USD");
            }

            // Parse Rationale (text after "Rationale:")
            Pattern rationalPattern = Pattern.compile("Rationale?:\\s*(.+?)(?=\\n\\n|TEIL|$)", Pattern.DOTALL);
            Matcher rationalMatcher = rationalPattern.matcher(teil3Content);
            if (rationalMatcher.find()) {
                setup.setRationale(rationalMatcher.group(1).trim());
            }

            // Determine Direction
            // 1. Try to find explicit mention
            Pattern dirPattern = Pattern.compile("(?:Trade[‑-]Setup).*?((?:SELL|SHORT|BUY|LONG))",
                    Pattern.CASE_INSENSITIVE);
            Matcher dirMatcher = dirPattern.matcher(teil3Content);
            if (dirMatcher.find()) {
                String found = dirMatcher.group(1).toUpperCase();
                if (found.equals("SELL") || found.equals("SHORT"))
                    setup.setDirection("SHORT");
                else if (found.equals("BUY") || found.equals("LONG"))
                    setup.setDirection("LONG");
            }

            // 2. Fallback: Calculation based on prices
            if (setup.getDirection().equals("N/A") && !setup.getEntry().equals("N/A")
                    && !setup.getTakeProfit().equals("N/A")) {
                try {
                    double entryPrice = Double.parseDouble(setup.getEntry().replaceAll("[^\\d.]", ""));
                    double tpPrice = Double.parseDouble(setup.getTakeProfit().replaceAll("[^\\d.]", ""));
                    if (entryPrice > 0 && tpPrice > 0) {
                        if (tpPrice < entryPrice)
                            setup.setDirection("SHORT");
                        else if (tpPrice > entryPrice)
                            setup.setDirection("LONG");
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            data.setTradingSetup(setup);

        } catch (Exception e) {
            System.err.println("Error parsing TEIL 3: " + e.getMessage());
        }
    }

    private void parseTeil4(String content, FullAnalysisData data) {
        try {
            // Find TEIL 4 section
            int teil4Start = content.indexOf("TEIL 4");
            if (teil4Start == -1)
                return;

            String teil4Content = content.substring(teil4Start);

            // Extract sections with flexible matching
            data.setCharttechnikDetail(extractSection(teil4Content,
                    "(?:1\\.|Detail)\\s*Charttechnik", "(?:2\\.|Sentiment)"));

            data.setSentimentDetail(extractSection(teil4Content,
                    "(?:2\\.|Sentiment)", "(?:3\\.|Fundamental)"));

            data.setFundamentalDetail(extractSection(teil4Content,
                    "(?:3\\.|Fundamental)", "(?:4\\.|Zusatz|Risiko)"));

            data.setRiskNotes(extractSection(teil4Content,
                    "(?:Zusatzhinweise|Risiko)", "(?:Wenn du|$)"));

        } catch (Exception e) {
            System.err.println("Error parsing TEIL 4: " + e.getMessage());
        }
    }

    /**
     * Extract a section of text between two markers.
     */
    private String extractSection(String content, String startMarker, String endMarker) {
        try {
            Pattern pattern = Pattern.compile(
                    startMarker + ".*?:(.*?)(?=" + endMarker + ")",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting section: " + e.getMessage());
        }
        return "";
    }

}
