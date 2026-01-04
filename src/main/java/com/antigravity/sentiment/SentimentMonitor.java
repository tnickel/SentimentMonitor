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

        Scene scene = new Scene(root, 1350, 600); // Widened for 6 Monate column
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

        // 6 Monate
        TableColumn<ForecastData, String> col6m = new TableColumn<>("6 Monate");
        TableColumn<ForecastData, String> colDate6m = new TableColumn<>("Datum");
        colDate6m.setCellValueFactory(new PropertyValueFactory<>("date6m"));
        TableColumn<ForecastData, String> colUp6m = new TableColumn<>("Steigt %");
        colUp6m.setCellValueFactory(new PropertyValueFactory<>("up6m"));
        TableColumn<ForecastData, String> colSide6m = new TableColumn<>("Seitw. %");
        colSide6m.setCellValueFactory(new PropertyValueFactory<>("sideways6m"));
        TableColumn<ForecastData, String> colDown6m = new TableColumn<>("Fällt %");
        colDown6m.setCellValueFactory(new PropertyValueFactory<>("down6m"));

        col6m.getColumns().addAll(colDate6m, colUp6m, colSide6m, colDown6m);

        // Add columns (including new Ampel)
        table.getColumns().addAll(colAsset, colAmpel, colDay1, colDay2, col6m);

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

        // Create toggle buttons
        CheckBox cbSteigt = new CheckBox("Steigt (Grün)");
        cbSteigt.setSelected(true);
        cbSteigt.setStyle("-fx-text-fill: green;");
        cbSteigt.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggleSeries(lineChart, seriesSteigt, newVal);
        });

        CheckBox cbSeitwaerts = new CheckBox("Seitwärts (Grau)");
        cbSeitwaerts.setSelected(true);
        cbSeitwaerts.setStyle("-fx-text-fill: gray;");
        cbSeitwaerts.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggleSeries(lineChart, seriesSeitwaerts, newVal);
        });

        CheckBox cbFaellt = new CheckBox("Fällt (Rot)");
        cbFaellt.setSelected(true);
        cbFaellt.setStyle("-fx-text-fill: red;");
        cbFaellt.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggleSeries(lineChart, seriesFaellt, newVal);
        });

        // Safe styling using helper
        setSeriesStyle(seriesSteigt, "-fx-stroke: green; -fx-background-color: green, white;");
        setSeriesStyle(seriesSeitwaerts, "-fx-stroke: gray; -fx-background-color: gray, white;");
        setSeriesStyle(seriesFaellt, "-fx-stroke: red; -fx-background-color: red, white;");

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

        // Add click handler for detailed analysis
        historyTable.setRowFactory(tv -> {
            TableRow<HistoryData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    HistoryData rowData = row.getItem();
                    String filePath = rowData.getSourceFilePath();
                    if (filePath != null && !filePath.isEmpty()) {
                        com.antigravity.sentiment.ui.AnalysisDetailWindow detailWindow = new com.antigravity.sentiment.ui.AnalysisDetailWindow();
                        detailWindow.show(data.getAsset(), rowData.dateProperty().get(), filePath);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Keine Details");
                        alert.setHeaderText(null);
                        alert.setContentText("Für diesen Eintrag sind keine Detail-Informationen verfügbar.");
                        alert.showAndWait();
                    }
                }
            });
            return row;
        });

        // Create button bar
        javafx.scene.layout.HBox buttonBar = new javafx.scene.layout.HBox();
        buttonBar.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        buttonBar.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        Button btnShowSource = new Button("Quelltext anzeigen");
        btnShowSource.setOnAction(e -> {
            HistoryData selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showSourceFile(selected.getSourceFilePath());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Keine Auswahl");
                alert.setHeaderText(null);
                alert.setContentText("Bitte wählen Sie einen Eintrag aus der Tabelle.");
                alert.showAndWait();
            }
        });
        buttonBar.getChildren().add(btnShowSource);

        // Container for table and button
        javafx.scene.layout.VBox bottomContainer = new javafx.scene.layout.VBox();
        bottomContainer.setPadding(new javafx.geometry.Insets(10));
        bottomContainer.getChildren().addAll(historyTable, buttonBar);
        javafx.scene.layout.VBox.setVgrow(historyTable, javafx.scene.layout.Priority.ALWAYS);

        // Create split pane
        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(chartContainer, bottomContainer);
        splitPane.setDividerPositions(0.6);

        Scene scene = new Scene(splitPane, 800, 700);
        historyStage.setScene(scene);
        historyStage.show();
    }

    private void showSourceFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Keine Datei");
            alert.setHeaderText(null);
            alert.setContentText("Kein Dateipfad verfügbar.");
            alert.showAndWait();
            return;
        }

        try {
            String content = java.nio.file.Files.readString(new File(filePath).toPath());

            Stage sourceStage = new Stage();
            sourceStage.setTitle("Quelltext: " + new File(filePath).getName());

            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setFont(javafx.scene.text.Font.font("Monospaced", 14));

            Scene scene = new Scene(new javafx.scene.layout.StackPane(textArea), 600, 800);
            sourceStage.setScene(scene);
            sourceStage.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setHeaderText("Fehler beim Laden der Datei");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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

    private void toggleSeries(javafx.scene.chart.LineChart<String, Number> chart,
            javafx.scene.chart.XYChart.Series<String, Number> series, boolean show) {
        if (show) {
            if (!chart.getData().contains(series)) {
                try {
                    chart.getData().add(series);
                } catch (IllegalArgumentException e) {
                    // Ignorieren: Series ist bereits verbunden
                }
            }
        } else {
            chart.getData().remove(series);
        }
    }

    private void setSeriesStyle(javafx.scene.chart.XYChart.Series<String, Number> series, String style) {
        if (series.getNode() != null) {
            series.getNode().setStyle(style);
        } else {
            series.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(style);
                }
            });
        }
    }
}
