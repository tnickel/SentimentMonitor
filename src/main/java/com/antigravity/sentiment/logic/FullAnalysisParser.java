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
            "(?:Spot[‑-]?(?:Referenz)?[‑-]?preis|Referenzpreis).*?[~≈]?\\s*([\\d,\\.]+)(?:\\s*(?:USD|\\$|EUR|€|Punkte|Points))?",
            Pattern.CASE_INSENSITIVE);

    // Patterns for TEIL 2 (Component scores)
    private static final Pattern TOTAL_BULL_PATTERN = Pattern.compile(
            "Gesamt\\s+Bullwahrscheinlichkeit.*?=\\s*([\\d\\.]+)%",
            Pattern.CASE_INSENSITIVE);

    // Patterns for TEIL 3 (Trading Setup)
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "Entry.*?:\\s*([\\d,\\.]+)(?:\\s*(?:USD|\\$|EUR|€|Punkte|Points))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern STOP_LOSS_PATTERN = Pattern.compile(
            "Stop\\s*Loss.*?:\\s*([\\d,\\.]+)(?:\\s*(?:USD|\\$|EUR|€|Punkte|Points))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TAKE_PROFIT_PATTERN = Pattern.compile(
            "Take\\s*Profit.*?:\\s*([\\d,\\.]+)(?:\\s*(?:USD|\\$|EUR|€|Punkte|Points))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern RISK_REWARD_PATTERN = Pattern.compile(
            "(?:Risiko[/\\s]*Chance|CRV|Risk[/\\s]*Reward).*?:\\s*([\\d\\.]+)\\s*:?\\s*1",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern RISK_PATTERN = Pattern.compile(
            "Risk.*?[≈~]?\\s*([\\d,\\.]+)(?:\\s*(?:USD|\\$|EUR|€|Punkte|Points))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern REWARD_PATTERN = Pattern.compile(
            "Reward.*?[≈~]?\\s*([\\d,\\.]+)(?:\\s*(?:USD|\\$|EUR|€|Punkte|Points))?",
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
            // 2026-01-04" OR "Datum: 05.01.2026")
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
                } else {
                    // Determine German numeric format (DD.MM.YYYY)
                    Pattern dePattern = Pattern.compile("Datum:\\s*(\\d{1,2}\\.\\d{1,2}\\.\\d{4})");
                    Matcher deMatcher = dePattern.matcher(teil1Content);
                    if (deMatcher.find()) {
                        data.setDate(deMatcher.group(1).trim());
                    }
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
                // Try to guess unit if not present
                String raw = spotMatcher.group(1);
                String fullMatch = spotMatcher.group(0);
                String unit = "USD"; // Default
                if (fullMatch.contains("EUR") || fullMatch.contains("€"))
                    unit = "EUR";
                else if (fullMatch.contains("Punkte") || fullMatch.contains("Points"))
                    unit = "Punkte";

                data.setSpotPrice(raw + " " + unit);
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

            // Add BME for Spain
            if (content.contains("BME") || content.contains("IBEX"))
                sources.append("BME/IBEX, ");

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
            // Matches until next Header (Sentiment, Makro, Gesamt) starts at a new line
            // with optional bullet
            Pattern chartBlockPattern = Pattern.compile(
                    "(?:^|\\n)\\s*[-•]?\\s*Charttechnik.*?(?=(?:^|\\n)\\s*[-•]?\\s*(?:Sentiment|Makro|Gesamt)|$)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher cbM = chartBlockPattern.matcher(teil2Content);
            if (cbM.find()) {
                String block = cbM.group(0);
                Pattern weightP = Pattern.compile("\\((\\d+)%.*?Gewicht\\)", Pattern.CASE_INSENSITIVE);
                Pattern scoreP = Pattern.compile("(?:Bull[‑-]?score|Score|Up)\\s*(\\d+)%", Pattern.CASE_INSENSITIVE);
                // Also support "0.60" format -> 60%
                Pattern decScoreP = Pattern.compile("(?:Bull[‑-]?score|Score|Up|bullisch).*?→\\s*([0-9]\\.[0-9]+)",
                        Pattern.CASE_INSENSITIVE);

                Matcher wM = weightP.matcher(block);
                Matcher sM = scoreP.matcher(block);
                Matcher dSM = decScoreP.matcher(block);

                if (wM.find()) {
                    double weight = Double.parseDouble(wM.group(1));
                    double score = 0;
                    if (sM.find()) {
                        score = Double.parseDouble(sM.group(1));
                    } else if (dSM.find()) {
                        score = Double.parseDouble(dSM.group(1)) * 100;
                    }

                    double contribution = weight * score / 100.0;
                    data.setCharttechnik(new ComponentScore("Charttechnik", (int) weight + "%", (int) score + "%",
                            String.format("%.1f%%", contribution).replace(",", ".")));
                }
            }

            // Parse Sentiment
            Pattern sentBlockPattern = Pattern.compile(
                    "(?:^|\\n)\\s*[-•]?\\s*Sentiment.*?(?=(?:^|\\n)\\s*[-•]?\\s*(?:Makro|Gesamt)|$)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher sbM = sentBlockPattern.matcher(teil2Content);
            if (sbM.find()) {
                String block = sbM.group(0);
                Pattern weightP = Pattern.compile("\\((\\d+)%.*?Gewicht\\)", Pattern.CASE_INSENSITIVE);
                Pattern scoreP = Pattern.compile("(?:Bull[‑-]?score|Score|Up)\\s*(\\d+)%", Pattern.CASE_INSENSITIVE);
                Pattern decScoreP = Pattern.compile("(?:Bull[‑-]?score|Score|Up|bullisch).*?→\\s*([0-9]\\.[0-9]+)",
                        Pattern.CASE_INSENSITIVE);

                Matcher wM = weightP.matcher(block);
                Matcher sM = scoreP.matcher(block);
                Matcher dSM = decScoreP.matcher(block);

                if (wM.find()) {
                    double weight = Double.parseDouble(wM.group(1));
                    double score = 0;
                    if (sM.find()) {
                        score = Double.parseDouble(sM.group(1));
                    } else if (dSM.find()) {
                        score = Double.parseDouble(dSM.group(1)) * 100;
                    }

                    double contribution = weight * score / 100.0;
                    data.setSentiment(new ComponentScore("Sentiment", (int) weight + "%", (int) score + "%",
                            String.format("%.1f%%", contribution).replace(",", ".")));
                }
            }

            // Parse Makro
            Pattern makroBlockPattern = Pattern.compile(
                    "(?:^|\\n)\\s*[-•]?\\s*Makro.*?(?=(?:^|\\n)\\s*[-•]?\\s*(?:Gesamt)|$)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher mbM = makroBlockPattern.matcher(teil2Content);
            if (mbM.find()) {
                String block = mbM.group(0);
                Pattern weightP = Pattern.compile("\\((\\d+)%.*?Gewicht\\)", Pattern.CASE_INSENSITIVE);
                Pattern scoreP = Pattern.compile("(?:Bull[‑-]?score|Score|Up)\\s*(\\d+)%", Pattern.CASE_INSENSITIVE);
                Pattern decScoreP = Pattern.compile("(?:Bull[‑-]?score|Score|Up|bullisch).*?→\\s*([0-9]\\.[0-9]+)",
                        Pattern.CASE_INSENSITIVE);

                Matcher wM = weightP.matcher(block);
                Matcher sM = scoreP.matcher(block);
                Matcher dSM = decScoreP.matcher(block);

                if (wM.find()) {
                    double weight = Double.parseDouble(wM.group(1));
                    double score = 0;
                    if (sM.find()) {
                        score = Double.parseDouble(sM.group(1));
                    } else if (dSM.find()) {
                        score = Double.parseDouble(dSM.group(1)) * 100;
                    }

                    double contribution = weight * score / 100.0;
                    data.setMakro(new ComponentScore("Makro/News", (int) weight + "%", (int) score + "%",
                            String.format("%.1f%%", contribution).replace(",", ".")));
                }
            }

            // Parse total bull probability
            Matcher totalMatcher = TOTAL_BULL_PATTERN.matcher(teil2Content);
            if (totalMatcher.find()) {
                data.setTotalBullProbability(totalMatcher.group(1) + "%");
            } else {
                // FALLBACK: Calculate from contributions
                double total = 0.0;
                total += parsePercentage(data.getCharttechnik().getContribution());
                total += parsePercentage(data.getSentiment().getContribution());
                total += parsePercentage(data.getMakro().getContribution());

                if (total > 0) {
                    data.setTotalBullProbability(String.format("%.1f%%", total).replace(",", "."));
                }
            }

            // Extract detailed calculation text - USER REQUEST: FULL CONTENT WITHOUT
            // FILTERING
            // We strip the "TEIL 2" header if present to avoid redundancy, but keep
            // everything else.
            String rawCalculation = teil2Content.replaceFirst("(?i)TEIL\\s*2.*?[\\r\\n]+", "").trim();
            data.setCalculationDetail(rawCalculation);

        } catch (Exception e) {
            System.err.println("Error parsing TEIL 2: " + e.getMessage());
        }
    }

    private double parsePercentage(String val) {
        if (val == null || val.isEmpty() || !val.contains("%"))
            return 0.0;
        try {
            return Double.parseDouble(val.replace("%", "").trim());
        } catch (Exception e) {
            return 0.0;
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
                setup.setEntry(entryMatcher.group(1) + " " + determineUnit(entryMatcher.group(0)));
            }

            // Parse Stop Loss
            Matcher slMatcher = STOP_LOSS_PATTERN.matcher(teil3Content);
            if (slMatcher.find()) {
                setup.setStopLoss(slMatcher.group(1) + " " + determineUnit(slMatcher.group(0)));
            }

            // Parse Take Profit
            Matcher tpMatcher = TAKE_PROFIT_PATTERN.matcher(teil3Content);
            if (tpMatcher.find()) {
                setup.setTakeProfit(tpMatcher.group(1) + " " + determineUnit(tpMatcher.group(0)));
            }

            // Parse Risk/Reward
            Matcher rrMatcher = RISK_REWARD_PATTERN.matcher(teil3Content);
            if (rrMatcher.find()) {
                setup.setRiskReward(rrMatcher.group(1) + " : 1");
            }

            // Parse Risk
            Matcher riskMatcher = RISK_PATTERN.matcher(teil3Content);
            if (riskMatcher.find()) {
                setup.setRisk(riskMatcher.group(1) + " " + determineUnit(riskMatcher.group(0)));
            }

            // Parse Reward
            Matcher rewardMatcher = REWARD_PATTERN.matcher(teil3Content);
            if (rewardMatcher.find()) {
                setup.setReward(rewardMatcher.group(1) + " " + determineUnit(rewardMatcher.group(0)));
            }

            // Parse Rationale/Logic
            // Priority 1: Detailed logic ("Kurz zur Logik", "Rationale")
            Pattern detailedLogicPattern = Pattern.compile(
                    "(?:Rationale|Kurz\\s*zur\\s*Logik|Logic).*?:\\s*(.+?)(?=\\n\\n|TEIL|$)",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher detailedMatcher = detailedLogicPattern.matcher(teil3Content);

            if (detailedMatcher.find()) {
                setup.setRationale(detailedMatcher.group(1).trim());
            } else {
                // Priority 2: Short setup summary ("Setup-Ansatz")
                Pattern setupPattern = Pattern.compile(
                        "(?:Setup[‑-]Ansatz|Setup).*?:\\s*(.+?)(?=\\n\\n|TEIL|$)",
                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher setupMatcher = setupPattern.matcher(teil3Content);
                if (setupMatcher.find()) {
                    setup.setRationale(setupMatcher.group(1).trim());
                }
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

    /**
     * Helper to determine unit from matching text.
     */
    private String determineUnit(String text) {
        if (text == null)
            return "USD";
        String lower = text.toLowerCase();
        if (lower.contains("punkte") || lower.contains("points"))
            return "Punkte";
        if (lower.contains("eur") || lower.contains("€"))
            return "EUR";
        return "USD";
    }

}
