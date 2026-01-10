package com.antigravity.sentiment.ui;

import com.antigravity.sentiment.logic.FullAnalysisParser;
import com.antigravity.sentiment.model.FullAnalysisData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * Detail window showing full analysis for a specific forecast date.
 * Updated for new SECTION-format.
 */
public class AnalysisDetailWindow {

    private Stage stage;
    private FullAnalysisData analysisData;

    public void show(String assetName, String date, String filePath) {
        try {
            String content = Files.readString(new File(filePath).toPath());
            FullAnalysisParser parser = new FullAnalysisParser();
            analysisData = parser.parseFullAnalysis(content);
        } catch (IOException e) {
            showError("Fehler beim Laden der Datei: " + e.getMessage());
            return;
        }

        stage = new Stage();
        stage.setTitle("Analyse Details: " + assetName + " - " + analysisData.getDate());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tabs
        Tab tab1 = new Tab("Übersicht", createOverviewTab());
        Tab tab2 = new Tab("Herleitung & FXSSI", createDerivationTab());
        Tab tab3 = new Tab("Begründungen", createReasoningTab());

        tabPane.getTabs().addAll(tab1, tab2, tab3);

        Scene scene = new Scene(tabPane, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createOverviewTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Statistische Wahrscheinlichkeiten");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Wahrscheinlichkeit (%)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Prognose für " + analysisData.getDate());
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(250); // Hard limit to approx 1/3 of screen
        barChart.setMaxHeight(250);
        barChart.setMinHeight(200);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        series.getData().add(new XYChart.Data<>("Steigt", parsePercentage(analysisData.getUpProbability())));
        series.getData().add(new XYChart.Data<>("Seitwärts", parsePercentage(analysisData.getSidewaysProbability())));
        series.getData().add(new XYChart.Data<>("Fällt", parsePercentage(analysisData.getDownProbability())));

        barChart.getData().add(series);

        // Colors
        barChart.setStyle(".default-color0.chart-bar { -fx-bar-fill: green; }" +
                ".default-color1.chart-bar { -fx-bar-fill: gray; }" +
                ".default-color2.chart-bar { -fx-bar-fill: red; }");

        vbox.getChildren().add(barChart);

        // Analyst Consensus
        Label consTitle = new Label("Analysten Meinung:");
        consTitle.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        TextArea consArea = new TextArea(analysisData.getAnalystConsensus());
        consArea.setEditable(false);
        consArea.setWrapText(true);
        consArea.setPrefRowCount(5);
        consArea.setStyle("-fx-control-inner-background: #f4f4f4; -fx-font-family: Monospaced;");

        vbox.getChildren().addAll(consTitle, consArea);
        VBox.setVgrow(consArea, Priority.ALWAYS); // Grow vertically

        // Calculation Details
        Label calcTitle = new Label("Berechnungsdetails:");
        calcTitle.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        TextArea calcArea = new TextArea(analysisData.getProbabilityCalculation());
        calcArea.setEditable(false);
        calcArea.setWrapText(true);
        // calcArea.setPrefRowCount(4); // Removed fixed preference
        calcArea.setStyle("-fx-control-inner-background: #f4f4f4; -fx-font-family: Monospaced;");

        vbox.getChildren().addAll(calcTitle, calcArea);
        VBox.setVgrow(calcArea, Priority.ALWAYS); // Grow vertically

        return vbox;
    }

    private VBox createDerivationTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);

        // SECTION: FXSSI
        Label fxTitle = new Label("FXSSI Sentiment Analysis");
        fxTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        vbox.getChildren().add(fxTitle);

        HBox fxBox = new HBox(0);
        fxBox.setAlignment(Pos.CENTER);
        fxBox.setPrefHeight(40);
        fxBox.setPrefWidth(500);

        int longVal = parsePercentage(analysisData.getFxssiLong());
        int shortVal = parsePercentage(analysisData.getFxssiShort());

        // Ensure total is 100 for visual if parsing fails
        if (longVal + shortVal == 0) {
            longVal = 50;
            shortVal = 50;
        }

        StackPane longPane = new StackPane(new Label("Long: " + longVal + "%"));
        longPane.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        longPane.setPrefWidth(longVal * 5); // Simple scaling

        StackPane shortPane = new StackPane(new Label("Short: " + shortVal + "%"));
        shortPane.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        shortPane.setPrefWidth(shortVal * 5);

        fxBox.getChildren().addAll(longPane, shortPane);
        vbox.getChildren().add(fxBox);

        // SECTION: DERIVATION TEXT
        Label derTitle = new Label("Logische Herleitung & Steuerung");
        derTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        vbox.getChildren().add(derTitle);

        TextArea textArea = new TextArea(analysisData.getDerivationText());
        textArea.setWrapText(true);
        textArea.setEditable(false);
        // textArea.setPrefHeight(400); // Dynamic height now
        textArea.setFont(Font.font("Monospaced", 13));

        vbox.getChildren().add(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS); // Fill remaining space

        return vbox;
    }

    private ScrollPane createReasoningTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label title = new Label("Detaillierte Begründungen");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        Map<String, String> rationals = analysisData.getRationales();

        if (rationals.isEmpty()) {
            vbox.getChildren().add(new Label("Keine detaillierten Begründungen gefunden."));
        } else {
            for (Map.Entry<String, String> entry : rationals.entrySet()) {
                VBox sectionBox = new VBox(5);
                Label secTitle = new Label(entry.getKey());
                secTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

                TextArea secText = new TextArea(entry.getValue());
                secText.setWrapText(true);
                secText.setEditable(false);
                secText.setPrefRowCount(4); // Compact

                sectionBox.getChildren().addAll(secTitle, secText);
                vbox.getChildren().add(sectionBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private int parsePercentage(String percentStr) {
        try {
            return Integer.parseInt(percentStr.replace("%", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
