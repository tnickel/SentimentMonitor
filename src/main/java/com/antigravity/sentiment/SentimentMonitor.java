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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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

        // Export Button
        Button btnExport = new Button("Export CSV");
        btnExport.setStyle("-fx-font-weight: bold; -fx-base: #e0e0e0;");
        btnExport.setOnAction(e -> exportToCsv());

        BorderPane bottomPane = new BorderPane();
        bottomPane.setRight(btnExport);
        bottomPane.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        root.setBottom(bottomPane);

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

        // CSV Export Config
        MenuItem itemCsvConfig = new MenuItem("Last Known Signals Verzeichnis konfigurieren...");
        itemCsvConfig.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Wähle Verzeichnis für Last Known Signals");
            File currentDir = new File(configManager.getCsvExportPath());
            if (currentDir.exists())
                chooser.setInitialDirectory(currentDir);
            File result = chooser.showDialog(stage); // Use 'stage' here
            if (result != null) {
                configManager.setCsvExportPath(result.getAbsolutePath());
            }
        });

        MenuItem itemThresholds = new MenuItem("Schwellwerte konfigurieren...");
        itemThresholds.setOnAction(e -> showThresholdDialog(stage));

        menuSettings.getItems().addAll(itemSetRoot, itemThresholds, itemCsvConfig); // Combined add calls

        menuBar.getMenus().add(menuSettings);

        return menuBar;
    }

    private void exportToCsv() {
        String path = configManager.getCsvExportPath();
        if (path == null || path.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Bitte konfigurieren Sie zuerst den CSV Export Pfad unter 'Einstellungen'.");
            alert.showAndWait();
            return;
        }

        File file = new File(path, "last_known_signals.csv");

        StringBuilder sb = new StringBuilder();
        sb.append("Waehrungspaar;Letztes_Signal\n");

        for (ForecastData fd : tableData) {
            String sig = fd.getSignal().toUpperCase();
            String outSig = "NEUTRAL";
            if (sig.equals("STEIGT"))
                outSig = "BUY";
            else if (sig.equals("FAELLT"))
                outSig = "SELL";
            else if (sig.equals("SEITWAERTS"))
                outSig = "NEUTRAL";
            else if (sig.equals("PANIC"))
                outSig = "STOP"; // Match UI display

            String assetName = fd.getAsset();
            if (assetName.equalsIgnoreCase("XAUUSD"))
                assetName = "GOLD";
            if (assetName.equalsIgnoreCase("XAGUSD"))
                assetName = "SILVER";

            sb.append(assetName).append(";").append(outSig).append("\n"); // Changed getAssetName to getAsset
        }

        try {
            java.nio.file.Files.writeString(file.toPath(), sb.toString());
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "CSV erfolgreich exportiert nach:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Fehler beim Speichern:\n" + ex.getMessage());
            alert.showAndWait();
        }
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
        // Table columns
        TableColumn<ForecastData, String> assetCol = new TableColumn<>("Asset");
        assetCol.setCellValueFactory(new PropertyValueFactory<>("asset"));
        assetCol.setPrefWidth(100);

        TableColumn<ForecastData, String> signalCol = new TableColumn<>("Signal");
        signalCol.setCellValueFactory(new PropertyValueFactory<>("signal"));
        signalCol.setPrefWidth(80);
        signalCol.setCellFactory(column -> new TableCell<ForecastData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // ▲ (Up), ▼ (Down), ▶ (Sideways)
                    Text icon = new Text();
                    icon.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;"); // Larger icon

                    switch (item.toUpperCase()) {
                        case "STEIGT":
                            icon.setText("▲");
                            icon.setFill(Color.GREEN);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "FAELLT":
                            icon.setText("▼");
                            icon.setFill(Color.RED);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "SEITWAERTS":
                            icon.setText("▶");
                            icon.setFill(Color.GRAY);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "PANIC":
                            setText("STOP"); // Text is clearer for Panic than a shape
                            setTextFill(Color.WHITE);
                            setStyle("-fx-background-color: red; -fx-alignment: center; -fx-font-weight: bold;");
                            setGraphic(null);
                            break;
                        default:
                            setText(item);
                            setGraphic(null);
                            setStyle("");
                            break;
                    }
                    if (!"PANIC".equals(item.toUpperCase())) {
                        setStyle("-fx-alignment: center;"); // Center align icons
                    }
                }
            }
        });

        TableColumn<ForecastData, String> lastSignalCol = new TableColumn<>("Last Signal");
        lastSignalCol.setCellValueFactory(new PropertyValueFactory<>("lastSignal"));
        lastSignalCol.setPrefWidth(80); // Reduced width since it's just an icon now
        lastSignalCol.setCellFactory(column -> new TableCell<ForecastData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Reuse the same logic as Signal column for consistency
                    Text icon = new Text();
                    icon.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

                    switch (item.toUpperCase()) {
                        case "STEIGT":
                            icon.setText("▲");
                            icon.setFill(Color.GREEN);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "FAELLT":
                            icon.setText("▼");
                            icon.setFill(Color.RED);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "SEITWAERTS":
                            icon.setText("▶");
                            icon.setFill(Color.GRAY);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "PANIC":
                            setText("STOP");
                            setTextFill(Color.WHITE);
                            setStyle("-fx-background-color: red; -fx-alignment: center; -fx-font-weight: bold;");
                            setGraphic(null);
                            break;
                        default:
                            setText(item);
                            setGraphic(null);
                            setStyle("");
                            break;
                    }
                    if (!"PANIC".equals(item.toUpperCase())) {
                        setStyle("-fx-alignment: center;");
                    }
                }
            }
        });

        TableColumn<ForecastData, String> fxssiCol = new TableColumn<>("FXSSI");
        fxssiCol.setCellValueFactory(new PropertyValueFactory<>("fxssiSignal"));
        fxssiCol.setPrefWidth(80);
        fxssiCol.setCellFactory(column -> new TableCell<ForecastData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Text icon = new Text();
                    icon.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

                    String s = item.toUpperCase();
                    if (s.contains("BUY") || s.contains("LONG")) {
                        icon.setText("▲");
                        icon.setFill(Color.GREEN);
                        setGraphic(icon);
                        setText(null);
                    } else if (s.contains("SELL") || s.contains("SHORT")) {
                        icon.setText("▼");
                        icon.setFill(Color.RED);
                        setGraphic(icon);
                        setText(null);
                    } else if (s.contains("NEUTRAL") || s.contains("SEITWAERTS")) {
                        icon.setText("▶");
                        icon.setFill(Color.GRAY);
                        setGraphic(icon);
                        setText(null);
                    } else {
                        setText(item);
                        setGraphic(null);
                        setStyle("-fx-alignment: center;");
                        setTextFill(Color.BLACK);
                        return; // Exit
                    }
                    setStyle("-fx-alignment: center;");
                }
            }
        });

        TableColumn<ForecastData, String> dateCol = new TableColumn<>("Datum");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<ForecastData, String> sentimentCol = new TableColumn<>("Sentiment (L/S)");
        sentimentCol.setCellValueFactory(new PropertyValueFactory<>("sentiment"));
        sentimentCol.setPrefWidth(120);

        TableColumn<ForecastData, String> explCol = new TableColumn<>("Signal-Erklärung");
        explCol.setCellValueFactory(new PropertyValueFactory<>("explanation"));
        // explCol.setPrefWidth(400); // Removed fixed width
        // Bind width to remaining space: Table Width - (Asset(100) + Signal(80) +
        // Last(80) + FXSSI(80) +
        // Date(100) + Sentiment(120) + Scrollbar(~20))
        explCol.prefWidthProperty().bind(table.widthProperty().subtract(500));
        // Enable text wrapping
        explCol.setCellFactory(tc -> {
            TableCell<ForecastData, String> cell = new TableCell<>();
            javafx.scene.text.Text text = new javafx.scene.text.Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(explCol.widthProperty().subtract(10));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        // Add columns to table
        table.getColumns().addAll(assetCol, signalCol, lastSignalCol, fxssiCol, dateCol, sentimentCol, explCol);

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

        TableColumn<HistoryData, java.time.LocalDate> colDate = new TableColumn<>("Datum");
        colDate.setCellValueFactory(new PropertyValueFactory<>("sortableDate"));
        colDate.setCellFactory(column -> new TableCell<HistoryData, java.time.LocalDate>() {
            @Override
            protected void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.equals(java.time.LocalDate.MIN)) {
                    setText(null);
                } else {
                    // Match original format: d.M.yy (e.g. 4.1.26)
                    setText(item.format(java.time.format.DateTimeFormatter.ofPattern("d.M.yy")));
                }
            }
        });
        colDate.setPrefWidth(150);
        colDate.setSortType(TableColumn.SortType.DESCENDING);

        TableColumn<HistoryData, String> colSignal = new TableColumn<>("Signal");
        colSignal.setCellValueFactory(new PropertyValueFactory<>("signal"));
        colSignal.setPrefWidth(80);
        colSignal.setCellFactory(column -> new TableCell<HistoryData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Text icon = new Text();
                    icon.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    switch (item.toUpperCase()) {
                        case "STEIGT":
                            icon.setText("▲");
                            icon.setFill(Color.GREEN);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "FAELLT":
                            icon.setText("▼");
                            icon.setFill(Color.RED);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "SEITWAERTS":
                            icon.setText("▶");
                            icon.setFill(Color.GRAY);
                            setGraphic(icon);
                            setText(null);
                            break;
                        case "PANIC":
                            setText("STOP");
                            setTextFill(Color.WHITE);
                            setStyle("-fx-background-color: red; -fx-alignment: center; -fx-font-weight: bold;");
                            setGraphic(null);
                            return; // Exit here to avoid overriding style below
                        default:
                            setText(item);
                            setGraphic(null);
                            break;
                    }
                    setStyle("-fx-alignment: center;");
                }
            }
        });

        TableColumn<HistoryData, String> colUp = new TableColumn<>("Steigt");
        colUp.setCellValueFactory(new PropertyValueFactory<>("up"));

        TableColumn<HistoryData, String> colSide = new TableColumn<>("Seitwärts");
        colSide.setCellValueFactory(new PropertyValueFactory<>("sideways"));

        TableColumn<HistoryData, String> colDown = new TableColumn<>("Fällt");
        colDown.setCellValueFactory(new PropertyValueFactory<>("down"));

        historyTable.getColumns().addAll(colDate, colSignal, colUp, colSide, colDown);
        historyTable.setItems(FXCollections.observableArrayList(history));
        historyTable.getSortOrder().add(colDate);

        // Add click handler for detailed analysis and style rows
        historyTable.setRowFactory(tv -> {
            TableRow<HistoryData> row = new TableRow<>() {
                @Override
                protected void updateItem(HistoryData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                    } else {
                        // Check if date is in the future
                        if (item.getSortableDate().isAfter(java.time.LocalDate.now())) {
                            setStyle("-fx-opacity: 0.5;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };

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
        // Use visibility to show/hide series without removing from chart to avoid
        // exceptions
        if (series.getNode() != null) {
            series.getNode().setVisible(show);
            for (javafx.scene.chart.XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setVisible(show);
                }
            }
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
