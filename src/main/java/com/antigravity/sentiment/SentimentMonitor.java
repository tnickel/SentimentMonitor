package com.antigravity.sentiment;

import com.antigravity.sentiment.config.ConfigManager;
import com.antigravity.sentiment.logic.SentimentCrawler;
import com.antigravity.sentiment.model.ForecastData;
import com.antigravity.sentiment.model.HistoryData;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.List;

public class SentimentMonitor extends Application {

    private ConfigManager configManager;
    private SentimentCrawler crawler;
    private TableView<ForecastData> table;
    private ObservableList<ForecastData> tableData;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        configManager = new ConfigManager();
        crawler = new SentimentCrawler();
        tableData = FXCollections.observableArrayList();

        primaryStage.setTitle("Sentiment Monitor - JavaFX");

        BorderPane root = new BorderPane();

        // Menu
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // Table
        table = createTable();
        table.setItems(tableData);
        root.setCenter(table);

        Scene scene = new Scene(root, 1050, 600); // Widened slightly for new column
        primaryStage.setScene(scene);
        primaryStage.show();

        // Auto-load if config exists
        String rootPath = configManager.getRootPath();
        if (rootPath != null && !rootPath.isEmpty()) {
            loadData(rootPath);
        }
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu menuSettings = new Menu("Einstellungen");
        MenuItem itemSetRoot = new MenuItem("Root-Verzeichnis wählen...");

        itemSetRoot.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Wähle das Root-Verzeichnis für Forecasts");

            String currentPath = configManager.getRootPath();
            if (currentPath != null) {
                File f = new File(currentPath);
                if (f.exists())
                    chooser.setInitialDirectory(f);
            }

            File selected = chooser.showDialog(stage);
            if (selected != null) {
                configManager.setRootPath(selected.getAbsolutePath());
                loadData(selected.getAbsolutePath());
            }
        });

        menuSettings.getItems().add(itemSetRoot);

        MenuItem itemThresholds = new MenuItem("Schwellwerte konfigurieren...");
        itemThresholds.setOnAction(e -> showThresholdDialog(stage));
        menuSettings.getItems().add(itemThresholds);

        menuBar.getMenus().add(menuSettings);
        return menuBar;
    }

    private void showThresholdDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.setTitle("Schwellwerte konfigurieren");
        dialog.initOwner(owner);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField upField = new TextField(String.valueOf(configManager.getThresholdUp()));
        TextField downField = new TextField(String.valueOf(configManager.getThresholdDown()));

        grid.add(new Label("Grün ab (%):"), 0, 0);
        grid.add(upField, 1, 0);
        grid.add(new Label("Blau ab (%):"), 0, 1);
        grid.add(downField, 1, 1);

        Button btnSave = new Button("Speichern");
        btnSave.setOnAction(e -> {
            try {
                int up = Integer.parseInt(upField.getText());
                int down = Integer.parseInt(downField.getText());
                configManager.setThresholdUp(up);
                configManager.setThresholdDown(down);
                dialog.close();
                table.refresh(); // Refresh traffic lights
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Bitte gültige Ganzzahlen eingeben.");
                alert.showAndWait();
            }
        });

        grid.add(btnSave, 1, 2);

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private TableView<ForecastData> createTable() {
        TableView<ForecastData> table = new TableView<>();

        TableColumn<ForecastData, String> colAsset = new TableColumn<>("Asset");
        colAsset.setCellValueFactory(new PropertyValueFactory<>("asset"));
        colAsset.setPrefWidth(100);

        // --- Ampel Logic Start ---
        TableColumn<ForecastData, Void> colAmpel = new TableColumn<>("Ampel");
        colAmpel.setPrefWidth(60);
        colAmpel.setCellFactory(param -> new TableCell<>() {
            private final Circle circle = new Circle(8);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ForecastData data = getTableRow().getItem();
                    // We check Day 1 forecasts
                    String upStr = data.up1Property().get().replace("%", "").trim();
                    String downStr = data.down1Property().get().replace("%", "").trim();

                    int upVal = 0;
                    int downVal = 0;
                    try {
                        upVal = Integer.parseInt(upStr);
                    } catch (NumberFormatException ignored) {
                    }
                    try {
                        downVal = Integer.parseInt(downStr);
                    } catch (NumberFormatException ignored) {
                    }

                    int thresholdUp = configManager.getThresholdUp();
                    int thresholdDown = configManager.getThresholdDown();

                    if (upVal > thresholdUp) {
                        circle.setFill(Color.GREEN);
                        setGraphic(circle);
                    } else if (downVal > thresholdDown) {
                        circle.setFill(Color.BLUE);
                        setGraphic(circle);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        // --- Ampel Logic End ---

        // Day 1
        TableColumn<ForecastData, String> colDay1 = new TableColumn<>("Tag 1");
        TableColumn<ForecastData, String> colDate1 = new TableColumn<>("Datum");
        colDate1.setCellValueFactory(new PropertyValueFactory<>("date1"));
        TableColumn<ForecastData, String> colUp1 = new TableColumn<>("Steigt %");
        colUp1.setCellValueFactory(new PropertyValueFactory<>("up1"));
        TableColumn<ForecastData, String> colSide1 = new TableColumn<>("Seitw. %");
        colSide1.setCellValueFactory(new PropertyValueFactory<>("sideways1"));
        TableColumn<ForecastData, String> colDown1 = new TableColumn<>("Fällt %");
        colDown1.setCellValueFactory(new PropertyValueFactory<>("down1"));

        colDay1.getColumns().addAll(colDate1, colUp1, colSide1, colDown1);

        // Day 2
        TableColumn<ForecastData, String> colDay2 = new TableColumn<>("Tag 2");
        TableColumn<ForecastData, String> colDate2 = new TableColumn<>("Datum");
        colDate2.setCellValueFactory(new PropertyValueFactory<>("date2"));
        TableColumn<ForecastData, String> colUp2 = new TableColumn<>("Steigt %");
        colUp2.setCellValueFactory(new PropertyValueFactory<>("up2"));
        TableColumn<ForecastData, String> colSide2 = new TableColumn<>("Seitw. %");
        colSide2.setCellValueFactory(new PropertyValueFactory<>("sideways2"));
        TableColumn<ForecastData, String> colDown2 = new TableColumn<>("Fällt %");
        colDown2.setCellValueFactory(new PropertyValueFactory<>("down2"));

        colDay2.getColumns().addAll(colDate2, colUp2, colSide2, colDown2);

        // Add columns (including new Ampel)
        table.getColumns().addAll(colAsset, colAmpel, colDay1, colDay2);

        // Double-click interaction
        table.setRowFactory(tv -> {
            TableRow<ForecastData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ForecastData rowData = row.getItem();
                    showHistoryWindow(rowData);
                }
            });
            return row;
        });

        return table;
    }

    private void showHistoryWindow(ForecastData data) {
        Stage historyStage = new Stage();
        historyStage.setTitle("Historie: " + data.getAsset());

        // Load data first
        List<HistoryData> history = crawler.loadHistory(data.getAssetPath());

        // Create chart
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        xAxis.setLabel("Datum");
        yAxis.setLabel("Wahrscheinlichkeit (%)");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);

        javafx.scene.chart.LineChart<String, Number> lineChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Prognose-Verlauf");
        lineChart.setCreateSymbols(true);
        lineChart.setLegendVisible(true);

        // Create series
        javafx.scene.chart.XYChart.Series<String, Number> seriesSteigt = new javafx.scene.chart.XYChart.Series<>();
        seriesSteigt.setName("Steigt");

        javafx.scene.chart.XYChart.Series<String, Number> seriesSeitwaerts = new javafx.scene.chart.XYChart.Series<>();
        seriesSeitwaerts.setName("Seitwärts");

        javafx.scene.chart.XYChart.Series<String, Number> seriesFaellt = new javafx.scene.chart.XYChart.Series<>();
        seriesFaellt.setName("Fällt");

        // Populate series
        for (HistoryData hd : history) {
            String date = hd.dateProperty().get();
            int upVal = parsePercentage(hd.upProperty().get());
            int sideVal = parsePercentage(hd.sidewaysProperty().get());
            int downVal = parsePercentage(hd.downProperty().get());

            seriesSteigt.getData().add(new javafx.scene.chart.XYChart.Data<>(date, upVal));
            seriesSeitwaerts.getData().add(new javafx.scene.chart.XYChart.Data<>(date, sideVal));
            seriesFaellt.getData().add(new javafx.scene.chart.XYChart.Data<>(date, downVal));
        }

        lineChart.getData().addAll(seriesSteigt, seriesSeitwaerts, seriesFaellt);

        // Apply colors to series
        seriesSteigt.getNode().setStyle("-fx-stroke: green;");
        seriesSeitwaerts.getNode().setStyle("-fx-stroke: gray;");
        seriesFaellt.getNode().setStyle("-fx-stroke: red;");

        // Create toggle buttons
        CheckBox cbSteigt = new CheckBox("Steigt (Grün)");
        cbSteigt.setSelected(true);
        cbSteigt.setStyle("-fx-text-fill: green;");
        cbSteigt.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!lineChart.getData().contains(seriesSteigt)) {
                    lineChart.getData().add(0, seriesSteigt);
                }
            } else {
                lineChart.getData().remove(seriesSteigt);
            }
        });

        CheckBox cbSeitwaerts = new CheckBox("Seitwärts (Grau)");
        cbSeitwaerts.setSelected(true);
        cbSeitwaerts.setStyle("-fx-text-fill: gray;");
        cbSeitwaerts.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!lineChart.getData().contains(seriesSeitwaerts)) {
                    lineChart.getData().add(seriesSeitwaerts);
                }
            } else {
                lineChart.getData().remove(seriesSeitwaerts);
            }
        });

        CheckBox cbFaellt = new CheckBox("Fällt (Rot)");
        cbFaellt.setSelected(true);
        cbFaellt.setStyle("-fx-text-fill: red;");
        cbFaellt.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!lineChart.getData().contains(seriesFaellt)) {
                    lineChart.getData().add(seriesFaellt);
                }
            } else {
                lineChart.getData().remove(seriesFaellt);
            }
        });

        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.setPadding(new javafx.geometry.Insets(10));
        buttonBox.getChildren().addAll(cbSteigt, cbSeitwaerts, cbFaellt);

        javafx.scene.layout.VBox chartContainer = new javafx.scene.layout.VBox();
        chartContainer.getChildren().addAll(buttonBox, lineChart);

        // Create table
        TableView<HistoryData> historyTable = new TableView<>();

        TableColumn<HistoryData, String> colDate = new TableColumn<>("Datum");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(150);

        TableColumn<HistoryData, String> colUp = new TableColumn<>("Steigt");
        colUp.setCellValueFactory(new PropertyValueFactory<>("up"));

        TableColumn<HistoryData, String> colSide = new TableColumn<>("Seitwärts");
        colSide.setCellValueFactory(new PropertyValueFactory<>("sideways"));

        TableColumn<HistoryData, String> colDown = new TableColumn<>("Fällt");
        colDown.setCellValueFactory(new PropertyValueFactory<>("down"));

        historyTable.getColumns().addAll(colDate, colUp, colSide, colDown);
        historyTable.setItems(FXCollections.observableArrayList(history));

        // Create split pane
        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(chartContainer, historyTable);
        splitPane.setDividerPositions(0.6);

        Scene scene = new Scene(splitPane, 800, 700);
        historyStage.setScene(scene);
        historyStage.show();
    }

    private int parsePercentage(String percentStr) {
        try {
            return Integer.parseInt(percentStr.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void loadData(String path) {
        tableData.clear();
        List<ForecastData> data = crawler.crawl(path);

        if (data.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Keine Daten");
            alert.setHeaderText(null);
            alert.setContentText("Es wurden keine gültigen Daten im Verzeichnis gefunden: " + path);
            alert.showAndWait();
        } else {
            tableData.addAll(data);
        }
    }
}
