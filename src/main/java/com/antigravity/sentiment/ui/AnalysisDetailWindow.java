package com.antigravity.sentiment.ui;

import com.antigravity.sentiment.logic.FullAnalysisParser;
import com.antigravity.sentiment.model.FullAnalysisData;
import com.antigravity.sentiment.model.FullAnalysisData.ComponentScore;
import com.antigravity.sentiment.model.FullAnalysisData.TradingSetup;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Detail window showing full analysis for a specific forecast date.
 */
public class AnalysisDetailWindow {

    private Stage stage;
    private FullAnalysisData analysisData;

    public void show(String assetName, String date, String filePath) {

        // Load and parse the file
        try {
            String content = Files.readString(new File(filePath).toPath());
            FullAnalysisParser parser = new FullAnalysisParser();
            analysisData = parser.parseFullAnalysis(content);
        } catch (IOException e) {
            showError("Fehler beim Laden der Datei: " + e.getMessage());
            return;
        }

        stage = new Stage();
        stage.setTitle("Analyse Details: " + assetName + " - " + date);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create 4 tabs
        Tab tab1 = new Tab("Übersicht", createOverviewTab());
        Tab tab2 = new Tab("Herleitung", createDerivationTab());
        Tab tab3 = new Tab("Trading Setup", createTradingSetupTab());
        Tab tab4 = new Tab("Begründungen", createReasoningTab());

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4);

        Scene scene = new Scene(tabPane, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Tab 1: Overview with statistics
     */
    private VBox createOverviewTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = new Label("Statistische Wahrscheinlichkeiten");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        // Bar chart for probabilities
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Wahrscheinlichkeit (%)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Prognose-Wahrscheinlichkeiten");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Parse percentages safely
        int upVal = parsePercentage(analysisData.getUpProbability());
        int sideVal = parsePercentage(analysisData.getSidewaysProbability());
        int downVal = parsePercentage(analysisData.getDownProbability());

        series.getData().add(new XYChart.Data<>("Steigt", upVal));
        series.getData().add(new XYChart.Data<>("Seitwärts", sideVal));
        series.getData().add(new XYChart.Data<>("Fällt", downVal));

        barChart.getData().add(series);

        // Apply colors to bars
        barChart.setStyle(".default-color0.chart-bar { -fx-bar-fill: green; }" +
                ".default-color1.chart-bar { -fx-bar-fill: gray; }" +
                ".default-color2.chart-bar { -fx-bar-fill: red; }");

        vbox.getChildren().add(barChart);

        // Metadata section
        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(15);
        metaGrid.setVgap(10);
        metaGrid.setPadding(new Insets(20));

        Label dateLabel = new Label("Datum:");
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label dateValue = new Label(analysisData.getDate());

        Label spotLabel = new Label("Spot-Preis:");
        spotLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label spotValue = new Label(analysisData.getSpotPrice());

        Label sourcesLabel = new Label("Quellen:");
        sourcesLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label sourcesValue = new Label(analysisData.getSources());
        sourcesValue.setWrapText(true);
        sourcesValue.setMaxWidth(600);

        metaGrid.add(dateLabel, 0, 0);
        metaGrid.add(dateValue, 1, 0);
        metaGrid.add(spotLabel, 0, 1);
        metaGrid.add(spotValue, 1, 1);
        metaGrid.add(sourcesLabel, 0, 2);
        metaGrid.add(sourcesValue, 1, 2);

        vbox.getChildren().add(metaGrid);

        return vbox;
    }

    /**
     * Tab 2: Numerical derivation with pie chart and table
     */
    private VBox createDerivationTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Numerische Herleitung");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        // Pie chart for weights
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Gewichtungsverteilung");
        pieChart.setMaxHeight(250);

        ComponentScore chart = analysisData.getCharttechnik();
        ComponentScore sent = analysisData.getSentiment();
        ComponentScore makro = analysisData.getMakro();

        pieChart.getData().add(new PieChart.Data("Charttechnik (" + chart.getWeight() + ")",
                parsePercentage(chart.getWeight())));
        pieChart.getData().add(new PieChart.Data("Sentiment (" + sent.getWeight() + ")",
                parsePercentage(sent.getWeight())));
        pieChart.getData().add(new PieChart.Data("Makro/News (" + makro.getWeight() + ")",
                parsePercentage(makro.getWeight())));

        vbox.getChildren().add(pieChart);

        // Table with component scores
        TableView<ComponentScore> table = new TableView<>();
        table.setPrefHeight(160);

        TableColumn<ComponentScore, String> nameCol = new TableColumn<>("Komponente");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(150);

        TableColumn<ComponentScore, String> weightCol = new TableColumn<>("Gewicht");
        weightCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getWeight()));
        weightCol.setPrefWidth(100);

        TableColumn<ComponentScore, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getScore()));
        scoreCol.setPrefWidth(100);

        TableColumn<ComponentScore, String> contribCol = new TableColumn<>("Beitrag");
        contribCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getContribution()));
        contribCol.setPrefWidth(100);

        @SuppressWarnings("unchecked")
        TableColumn<ComponentScore, String>[] columns = new TableColumn[] { nameCol, weightCol, scoreCol, contribCol };
        table.getColumns().addAll(columns);
        table.getItems().addAll(chart, sent, makro);

        vbox.getChildren().add(table);

        // Total probability
        Label totalLabel = new Label("Gesamt Bullwahrscheinlichkeit: " + analysisData.getTotalBullProbability());
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        vbox.getChildren().add(totalLabel);

        // Calculation Details from text
        String calcDetail = analysisData.getCalculationDetail();
        if (calcDetail != null && !calcDetail.isEmpty()) {
            Label calcLabel = new Label("Details zur Berechnung:");
            calcLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            TextArea calcArea = new TextArea(calcDetail);
            calcArea.setEditable(false);
            calcArea.setWrapText(true);
            calcArea.setPrefHeight(200);
            VBox.setVgrow(calcArea, Priority.ALWAYS);

            vbox.getChildren().addAll(calcLabel, calcArea);
        }

        return vbox;
    }

    /**
     * Tab 3: Trading setup with levels and visual
     */
    private VBox createTradingSetupTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));

        Label title = new Label("Intraday Trading Levels");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        TradingSetup setup = analysisData.getTradingSetup();

        // Direction Label
        String direction = setup.getDirection();
        Label directionLabel = new Label("Trade Direction: " + direction);
        directionLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        directionLabel.setPadding(new Insets(0, 0, 10, 0));

        if ("LONG".equals(direction)) {
            directionLabel.setTextFill(Color.GREEN);
            directionLabel.setText("Trade Direction: LONG (Auf steigende Kurse setzen)");
        } else if ("SHORT".equals(direction)) {
            directionLabel.setTextFill(Color.RED);
            directionLabel.setText("Trade Direction: SHORT (Auf fallende Kurse setzen)");
        } else {
            directionLabel.setTextFill(Color.BLACK);
        }
        vbox.getChildren().add(directionLabel);

        // Create HBox for table and visual
        HBox hbox = new HBox(30);
        hbox.setAlignment(Pos.CENTER);

        // Left side: Table
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");

        addSetupRow(grid, 0, "Entry:", setup.getEntry());
        addSetupRow(grid, 1, "Stop Loss:", setup.getStopLoss());
        addSetupRow(grid, 2, "Take Profit:", setup.getTakeProfit());
        addSetupRow(grid, 3, "Risk/Reward:", setup.getRiskReward());
        addSetupRow(grid, 4, "Risk:", setup.getRisk());
        addSetupRow(grid, 5, "Reward:", setup.getReward());

        hbox.getChildren().add(grid);

        // Right side: Visual representation
        VBox visualBox = createTradingVisual(setup);
        hbox.getChildren().add(visualBox);

        vbox.getChildren().add(hbox);

        // Rationale
        Label rationaleLabel = new Label("Rationale:");
        rationaleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        vbox.getChildren().add(rationaleLabel);

        TextArea rationaleText = new TextArea(setup.getRationale());
        rationaleText.setWrapText(true);
        rationaleText.setEditable(false);
        rationaleText.setPrefRowCount(3);
        vbox.getChildren().add(rationaleText);

        return vbox;
    }

    private void addSetupRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("Courier New", 12));

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private VBox createTradingVisual(TradingSetup setup) {
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefWidth(250);
        vbox.setPrefHeight(300);
        vbox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        vbox.setPadding(new Insets(20));

        // Create visual lines
        addPriceLine(vbox, "SL: " + setup.getStopLoss(), Color.RED);
        vbox.getChildren().add(createSpacer(50));

        addPriceLine(vbox, "Entry: " + setup.getEntry(), Color.BLUE);
        vbox.getChildren().add(createSpacer(50));

        addPriceLine(vbox, "TP: " + setup.getTakeProfit(), Color.GREEN);

        return vbox;
    }

    private void addPriceLine(VBox container, String text, Color color) {
        HBox line = new HBox(10);
        line.setAlignment(Pos.CENTER);

        Region leftLine = new Region();
        leftLine.setPrefHeight(2);
        leftLine.setPrefWidth(50);
        leftLine.setStyle("-fx-background-color: " + toRgbString(color) + ";");

        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        label.setTextFill(color);

        Region rightLine = new Region();
        rightLine.setPrefHeight(2);
        rightLine.setPrefWidth(50);
        rightLine.setStyle("-fx-background-color: " + toRgbString(color) + ";");

        line.getChildren().addAll(leftLine, label, rightLine);
        container.getChildren().add(line);
    }

    private Region createSpacer(double height) {
        Region spacer = new Region();
        spacer.setPrefHeight(height);
        return spacer;
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Tab 4: Detailed reasoning
     */
    private ScrollPane createReasoningTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label title = new Label("Detaillierte Begründungen");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        // Section 1: Charttechnik
        addReasoningSection(vbox, "1. Detail Charttechnik", analysisData.getCharttechnikDetail());

        // Section 2: Sentiment
        addReasoningSection(vbox, "2. Sentiment & FXSSI Analyse", analysisData.getSentimentDetail());

        // Section 3: Fundamental
        addReasoningSection(vbox, "3. Fundamentaldaten & News", analysisData.getFundamentalDetail());

        // Section 4: Risk
        addReasoningSection(vbox, "4. Zusatzhinweise / Risiko", analysisData.getRiskNotes());

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private void addReasoningSection(VBox container, String sectionTitle, String content) {
        Label titleLabel = new Label(sectionTitle);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        container.getChildren().add(titleLabel);

        TextArea textArea = new TextArea(content.isEmpty() ? "Keine Daten verfügbar" : content);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setPrefRowCount(5);
        textArea.setStyle("-fx-background-color: #f9f9f9;");
        container.getChildren().add(textArea);
    }

    /**
     * Helper: Parse percentage string to int
     */
    private int parsePercentage(String percentStr) {
        try {
            return Integer.parseInt(percentStr.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
