package com.antigravity.sentiment.model;

/**
 * Represents the complete analysis data from a forecast file.
 * Designed to handle variations in AI-generated content with safe defaults.
 */
public class FullAnalysisData {

    // TEIL 1: Statistical Data (already parsed by ReportParser)
    private String date;
    private String upProbability;
    private String sidewaysProbability;
    private String downProbability;
    private String spotPrice;
    private String sources;

    // TEIL 2: Numerical Derivation
    private ComponentScore charttechnik;
    private ComponentScore sentiment;
    private ComponentScore makro;
    private String totalBullProbability;

    // TEIL 3: Trading Setup
    private TradingSetup tradingSetup;

    // TEIL 4: Detailed Reasoning
    private String charttechnikDetail;
    private String sentimentDetail;
    private String fundamentalDetail;
    private String riskNotes;

    // Full raw content for fallback display
    private String rawContent;

    public FullAnalysisData() {
        // Safe defaults
        this.date = "";
        this.upProbability = "0%";
        this.sidewaysProbability = "0%";
        this.downProbability = "0%";
        this.spotPrice = "N/A";
        this.sources = "";

        this.charttechnik = new ComponentScore();
        this.sentiment = new ComponentScore();
        this.makro = new ComponentScore();
        this.totalBullProbability = "0%";

        this.tradingSetup = new TradingSetup();

        this.charttechnikDetail = "";
        this.sentimentDetail = "";
        this.fundamentalDetail = "";
        this.riskNotes = "";

        this.rawContent = "";
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUpProbability() {
        return upProbability;
    }

    public void setUpProbability(String upProbability) {
        this.upProbability = upProbability;
    }

    public String getSidewaysProbability() {
        return sidewaysProbability;
    }

    public void setSidewaysProbability(String sidewaysProbability) {
        this.sidewaysProbability = sidewaysProbability;
    }

    public String getDownProbability() {
        return downProbability;
    }

    public void setDownProbability(String downProbability) {
        this.downProbability = downProbability;
    }

    public String getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(String spotPrice) {
        this.spotPrice = spotPrice;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public ComponentScore getCharttechnik() {
        return charttechnik;
    }

    public void setCharttechnik(ComponentScore charttechnik) {
        this.charttechnik = charttechnik;
    }

    public ComponentScore getSentiment() {
        return sentiment;
    }

    public void setSentiment(ComponentScore sentiment) {
        this.sentiment = sentiment;
    }

    public ComponentScore getMakro() {
        return makro;
    }

    public void setMakro(ComponentScore makro) {
        this.makro = makro;
    }

    public String getTotalBullProbability() {
        return totalBullProbability;
    }

    public void setTotalBullProbability(String totalBullProbability) {
        this.totalBullProbability = totalBullProbability;
    }

    public TradingSetup getTradingSetup() {
        return tradingSetup;
    }

    public void setTradingSetup(TradingSetup tradingSetup) {
        this.tradingSetup = tradingSetup;
    }

    public String getCharttechnikDetail() {
        return charttechnikDetail;
    }

    public void setCharttechnikDetail(String charttechnikDetail) {
        this.charttechnikDetail = charttechnikDetail;
    }

    public String getSentimentDetail() {
        return sentimentDetail;
    }

    public void setSentimentDetail(String sentimentDetail) {
        this.sentimentDetail = sentimentDetail;
    }

    public String getFundamentalDetail() {
        return fundamentalDetail;
    }

    public void setFundamentalDetail(String fundamentalDetail) {
        this.fundamentalDetail = fundamentalDetail;
    }

    public String getRiskNotes() {
        return riskNotes;
    }

    public void setRiskNotes(String riskNotes) {
        this.riskNotes = riskNotes;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    /**
     * Inner class for component scores (TEIL 2)
     */
    public static class ComponentScore {
        private String name;
        private String weight;
        private String score;
        private String contribution;

        public ComponentScore() {
            this.name = "";
            this.weight = "0%";
            this.score = "0%";
            this.contribution = "0%";
        }

        public ComponentScore(String name, String weight, String score, String contribution) {
            this.name = name;
            this.weight = weight;
            this.score = score;
            this.contribution = contribution;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getContribution() {
            return contribution;
        }

        public void setContribution(String contribution) {
            this.contribution = contribution;
        }
    }

    /**
     * Inner class for trading setup (TEIL 3)
     */
    public static class TradingSetup {
        private String direction; // "LONG" or "SHORT"
        private String entry;
        private String stopLoss;
        private String takeProfit;
        private String riskReward;
        private String risk;
        private String reward;
        private String rationale;

        public TradingSetup() {
            this.direction = "N/A";
            this.entry = "N/A";
            this.stopLoss = "N/A";
            this.takeProfit = "N/A";
            this.riskReward = "N/A";
            this.risk = "N/A";
            this.reward = "N/A";
            this.rationale = "";
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getEntry() {
            return entry;
        }

        public void setEntry(String entry) {
            this.entry = entry;
        }

        public String getStopLoss() {
            return stopLoss;
        }

        public void setStopLoss(String stopLoss) {
            this.stopLoss = stopLoss;
        }

        public String getTakeProfit() {
            return takeProfit;
        }

        public void setTakeProfit(String takeProfit) {
            this.takeProfit = takeProfit;
        }

        public String getRiskReward() {
            return riskReward;
        }

        public void setRiskReward(String riskReward) {
            this.riskReward = riskReward;
        }

        public String getRisk() {
            return risk;
        }

        public void setRisk(String risk) {
            this.risk = risk;
        }

        public String getReward() {
            return reward;
        }

        public void setReward(String reward) {
            this.reward = reward;
        }

        public String getRationale() {
            return rationale;
        }

        public void setRationale(String rationale) {
            this.rationale = rationale;
        }
    }
}
