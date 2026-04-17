package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import managers.AGVManager;
import managers.TaskManager;
import model.*;

import java.util.*;

public class AGVWarehouseSystem extends Application {
    private TaskManager taskManager;
    private AGVManager agvManager;
    private List<StorageZone> storageZones;
    private StorageZone unsortedArea;
    private List<ChargingStation> chargingStations;
    private Position agvRestArea;

    private Pane warehousePane;
    private TextArea logArea;
    private ListView<String> taskQueueView;
    private ListView<String> activeTasksView;
    private Label statusLabel;
    private LogViewer logViewer;

    @Override
    public void start(Stage primaryStage) {
        initializeSystem();
        
        // Initialize logging system
        managers.SystemLogger logger = managers.SystemLogger.getInstance();
        logger.logSystemEvent("System Startup", "AGV Warehouse Management System initialized");

        BorderPane root = new BorderPane();

        // Create TabPane for main content
        TabPane mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Warehouse Operations
        Tab operationsTab = new Tab(" Warehouse Operations");
        BorderPane operationsPane = new BorderPane();
        
        // Left: Task Creation Panel
        VBox leftPanel = createTaskCreationPanel();
        operationsPane.setLeft(leftPanel);

        // Center: Warehouse Visualization
        VBox centerPanel = createWarehousePanel();
        operationsPane.setCenter(centerPanel);

        // Right: Status Panel
        VBox rightPanel = createStatusPanel();
        operationsPane.setRight(rightPanel);
        
        operationsTab.setContent(operationsPane);

        // Tab 2: Log Viewer
        Tab logViewerTab = new Tab(" Log Viewer");
        logViewer = new LogViewer();
        logViewerTab.setContent(logViewer);

        // Tab 3: Console Output
        Tab consoleTab = new Tab(" Console Output");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(700);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        consoleTab.setContent(logArea);

        mainTabs.getTabs().addAll(operationsTab, logViewerTab, consoleTab);

        root.setCenter(mainTabs);

        Scene scene = new Scene(root, 1500, 850);
        primaryStage.setTitle("Automatic Cold Chain Logistics and Storage System");
        primaryStage.setScene(scene);
        primaryStage.show();

        redirectOutput();
        startUpdateTimer();

        // Auto-create initial sorting tasks after 2 seconds
        Timer initTimer = new Timer(true);
        initTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                createInitialSortingTasks();
            }
        }, 2000);
        
        // Cleanup on close
        primaryStage.setOnCloseRequest(e -> {
            logger.closeLogFiles();
            System.out.println(" System shutdown complete");
        });
    }

    private void initializeSystem() {
        agvManager = new AGVManager();
        taskManager = new TaskManager(agvManager);  // Removed TaskScheduler parameter
        storageZones = new ArrayList<>();
        chargingStations = new ArrayList<>();
        agvRestArea = new Position(100, 50);

        // Charging Stations
        chargingStations.add(new ChargingStation("CS-1", new Position(200, 700)));
        chargingStations.add(new ChargingStation("CS-2", new Position(350, 700)));
        taskManager.setChargingStations(chargingStations);

        // Create AGVs
        AGV agv001 = new AGV("001", new Position(80, 50));
        agv001.setBatteryLevel(40.0);
        agvManager.addAGV(agv001);
        agvManager.addAGV(new AGV("002", new Position(100, 50)));
        agvManager.addAGV(new AGV("003", new Position(120, 50)));

        // Temperature-Controlled Zones
        // Cold Zones (-20C)
        storageZones.add(new StorageZone("Cold-Zone-A", -20.0, 10, new Position(250, 150)));
        storageZones.add(new StorageZone("Cold-Zone-B", -20.0, 10, new Position(850, 150)));

        // Room Temperature Zones (20C)
        storageZones.add(new StorageZone("Room-Zone-A", 20.0, 10, new Position(250, 350)));
        storageZones.add(new StorageZone("Room-Zone-B", 20.0, 10, new Position(850, 350)));

        // Hot Zones (30)
        storageZones.add(new StorageZone("Hot-Zone-A", 30.0, 10, new Position(250, 550)));
        storageZones.add(new StorageZone("Hot-Zone-B", 30.0, 10, new Position(850, 550)));

        // Unsorted Area (Initial Drop Zone)
        unsortedArea = new StorageZone("Unsorted-Area", 25.0, 50, new Position(550, 300));

        // Add items to unsorted area
        String[] itemTypes = {"Frozen", "Fresh", "Ambient", "Hot", "Frozen", "Fresh",
                "Ambient", "Hot", "Frozen", "Fresh", "Ambient", "Hot",
                "Frozen", "Fresh", "Ambient"};
        double[] reqTemps = {-20, 20, 20, 30, -20, 20, 20, 30, -20, 20, 20, 30, -20, 20, 20};

        try {
            for (int i = 0; i < 15; i++) {
                Item item = new Item("ITEM-" + String.format("%02d", i + 1),
                        itemTypes[i] + "-Pkg-" + (i + 1),
                        5.0 + i,
                        new Date(),
                        reqTemps[i]);
                unsortedArea.addItem(item);
            }
            System.out.println(" " + unsortedArea.getItems().size() + " items loaded in Unsorted Area");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createInitialSortingTasks() {
        System.out.println("\n AUTO-SORTING: Creating tasks for " + unsortedArea.getItems().size() + " unsorted items...\n");

        List<Item> itemsToSort = new ArrayList<>(unsortedArea.getItems());

        // Group storage zones by temperature
        Map<Double, List<StorageZone>> tempZoneMap = new HashMap<>();
        for (StorageZone zone : storageZones) {
            tempZoneMap.putIfAbsent(zone.getTemperature(), new ArrayList<>());
            tempZoneMap.get(zone.getTemperature()).add(zone);
        }

        Map<Double, Integer> zoneIndexMap = new HashMap<>();
        zoneIndexMap.put(-20.0, 0);
        zoneIndexMap.put(20.0, 0);
        zoneIndexMap.put(30.0, 0);

        for (Item item : itemsToSort) {
            double reqTemp = item.getRequiredTemperature();
            List<StorageZone> zones = tempZoneMap.get(reqTemp);

            if (zones != null && !zones.isEmpty()) {
                int index = zoneIndexMap.get(reqTemp);
                StorageZone targetZone = zones.get(index % zones.size());
                zoneIndexMap.put(reqTemp, index + 1);

                TransportTask task = new TransportTask(5, item, unsortedArea, targetZone);
                taskManager.addTask(task);
            }
        }

        Platform.runLater(() -> showStatus(" " + itemsToSort.size() + " auto-sorting tasks created!", false));
    }

    private VBox createTaskCreationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(300);
        panel.setStyle("-fx-background-color: #f0f0f0;");

        Label title = new Label(" Manual Task Creation");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        statusLabel.setPrefHeight(40);

        Button autoSortBtn = new Button(" Auto-Sort All Items");
        autoSortBtn.setPrefWidth(280);
        autoSortBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        autoSortBtn.setOnAction(e -> createInitialSortingTasks());

        Separator sep1 = new Separator();
        Label manualLabel = new Label("Manual Transport Task:");
        manualLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label priorityLabel = new Label("Priority (1-10, higher = urgent):");
        Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, 5);
        prioritySpinner.setPrefWidth(280);

        Label sourceZoneLabel = new Label("From Zone:");
        ComboBox<StorageZone> sourceZoneCombo = new ComboBox<>();
        sourceZoneCombo.getItems().add(unsortedArea);
        sourceZoneCombo.getItems().addAll(storageZones);
        sourceZoneCombo.setPrefWidth(280);

        Label itemLabel = new Label("Select Item:");
        ComboBox<Item> itemCombo = new ComboBox<>();
        itemCombo.setPrefWidth(280);

        Label targetZoneLabel = new Label("To Zone:");
        ComboBox<StorageZone> targetZoneCombo = new ComboBox<>();
        targetZoneCombo.getItems().addAll(storageZones);
        targetZoneCombo.setPrefWidth(280);

        sourceZoneCombo.setOnAction(e -> {
            itemCombo.getItems().clear();
            StorageZone selected = sourceZoneCombo.getValue();
            if (selected != null) {
                itemCombo.getItems().addAll(selected.getItems());
                if (!selected.getItems().isEmpty()) {
                    itemCombo.setValue(selected.getItems().get(0));
                }
            }
        });

        Button createButton = new Button("Create Transport Task");
        createButton.setPrefWidth(280);
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        createButton.setOnAction(e -> {
            try {
                StorageZone source = sourceZoneCombo.getValue();
                StorageZone target = targetZoneCombo.getValue();
                Item item = itemCombo.getValue();
                int priority = prioritySpinner.getValue();

                if (source == null || target == null || item == null) {
                    showStatus(" Please select source zone, target zone, and item", true);
                    return;
                }
                if (source.equals(target)) {
                    showStatus(" Source and target zones must be different", true);
                    return;
                }
                if (!source.getItems().contains(item)) {
                    showStatus(" Item not found in source zone", true);
                    return;
                }

                double tempDiff = Math.abs(target.getTemperature() - item.getRequiredTemperature());
                if (tempDiff > 5) {
                    showStatus(String.format("Temperature mismatch! Item needs %.0fC but target zone is %.0fC",
                            item.getRequiredTemperature(), target.getTemperature()), true);
                    return;
                }

                TransportTask task = new TransportTask(priority, item, source, target);
                taskManager.addTask(task);

                String message = String.format(" Transport task: %s from %s to %s (Priority: %d)",
                        item.getName(), source.getZoneName(), target.getZoneName(), priority);
                showStatus(message, false);

            } catch (Exception ex) {
                showStatus(" Error: " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        });

        panel.getChildren().addAll(title, statusLabel, autoSortBtn, sep1, manualLabel,
                priorityLabel, prioritySpinner, sourceZoneLabel, sourceZoneCombo,
                itemLabel, itemCombo, targetZoneLabel, targetZoneCombo, createButton);

        return panel;
    }

    private VBox createWarehousePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        Label title = new Label(" Warehouse Map - Temperature Zones");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        warehousePane = new Pane();
        warehousePane.setPrefSize(1000, 650);
        warehousePane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc;");

        panel.getChildren().addAll(title, warehousePane);
        return panel;
    }

    private VBox createStatusPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(300);
        panel.setStyle("-fx-background-color: #f0f0f0;");

        Label queueLabel = new Label(" Task Queue (Priority Order):");
        queueLabel.setStyle("-fx-font-weight: bold;");
        taskQueueView = new ListView<>();
        taskQueueView.setPrefHeight(200);

        Label activeLabel = new Label(" Active Tasks:");
        activeLabel.setStyle("-fx-font-weight: bold;");
        activeTasksView = new ListView<>();
        activeTasksView.setPrefHeight(200);

        Label agvLabel = new Label(" AGV Status:");
        agvLabel.setStyle("-fx-font-weight: bold;");
        ListView<String> agvListView = new ListView<>();
        agvListView.setPrefHeight(150);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    agvListView.getItems().clear();
                    for (AGV agv : agvManager.getAgvList()) {
                       
                        agvListView.getItems().add(String.format("%s AGV-%s [%s] %.0f%%",
                                "battery", agv.getId(), agv.getStatus(), agv.getBatteryLevel()));
                    }
                });
            }
        }, 0, 500);

        panel.getChildren().addAll(queueLabel, taskQueueView,
                activeLabel, activeTasksView,
                agvLabel, agvListView);

        return panel;
    }

    private void updateWarehouseVisualization() {
        warehousePane.getChildren().clear();

        // Draw AGV Rest Area
        Rectangle restRect = new Rectangle(agvRestArea.getX() - 50, agvRestArea.getY() - 30, 100, 60);
        restRect.setFill(Color.LIGHTGRAY);
        restRect.setStroke(Color.DARKGRAY);
        restRect.setStrokeWidth(2);
        restRect.getStrokeDashArray().addAll(5.0, 5.0);
        warehousePane.getChildren().addAll(restRect, new Text(agvRestArea.getX() - 35, agvRestArea.getY() + 5, "AGV Rest"));

        // Draw Unsorted Area
        Rectangle unsortedRect = new Rectangle(unsortedArea.getPosition().getX() - 60,
                unsortedArea.getPosition().getY() - 50, 120, 100);
        unsortedRect.setFill(Color.LIGHTYELLOW);
        unsortedRect.setStroke(Color.ORANGE);
        unsortedRect.setStrokeWidth(3);
        Text unsortedLabel = new Text(unsortedArea.getPosition().getX() - 55,
                unsortedArea.getPosition().getY() - 25, "  " + unsortedArea.getZoneName());
        Text unsortedCount = new Text(unsortedArea.getPosition().getX() - 25,
                unsortedArea.getPosition().getY() + 5, unsortedArea.getItems().size() + " items");
        unsortedCount.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        warehousePane.getChildren().addAll(unsortedRect, unsortedLabel, unsortedCount);

        // Draw Storage Zones
        
        Color[] colors = {Color.LIGHTBLUE, Color.LIGHTCYAN, Color.LIGHTGREEN, Color.PALEGREEN, Color.LIGHTCORAL, Color.LIGHTSALMON};
        for (int i = 0; i < storageZones.size(); i++) {
            StorageZone zone = storageZones.get(i);
            Rectangle rect = new Rectangle(zone.getPosition().getX() - 60,
                    zone.getPosition().getY() - 40, 120, 80);
            rect.setFill(colors[i]);
            rect.setStroke(Color.DARKBLUE);
            rect.setStrokeWidth(2);

            Text label = new Text(zone.getPosition().getX() - 55, zone.getPosition().getY() - 20, " " + zone.getZoneName());
            Text temp = new Text(zone.getPosition().getX() - 55, zone.getPosition().getY(),
                    String.format("%.0fC | %d items", zone.getTemperature(), zone.getItems().size()));
            temp.setStyle("-fx-font-size: 10px;");

            warehousePane.getChildren().addAll(rect, label, temp);
        }

        // Draw Charging Stations
        for (ChargingStation station : chargingStations) {
            Circle circle = new Circle(station.getPosition().getX(), station.getPosition().getY(), 25);
            circle.setFill(station.isOccupied() ? Color.RED : Color.LIGHTGREEN);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            warehousePane.getChildren().addAll(circle, new Text(station.getPosition().getX() - 10, station.getPosition().getY() + 5, ""+station.getId()));
        }

        // Draw AGVs
        for (AGV agv : agvManager.getAgvList()) {
            Position pos = agv.getPosition();
            Rectangle body = new Rectangle(pos.getX() - 15, pos.getY() - 10, 30, 20);
            Color color = agv.getBatteryLevel() <= 20 ? Color.RED :
                    agv.isBusy() ? Color.ORANGE : Color.LIGHTGREEN;
            body.setFill(color);
            body.setStroke(Color.BLACK);
            body.setStrokeWidth(2);

            Text id = new Text(pos.getX() - 10, pos.getY() + 4, agv.getId());
            id.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");

            warehousePane.getChildren().addAll(body, id);
        }
    }

    private void startUpdateTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    updateWarehouseVisualization();
                    updateTaskLists();
                });
            }
        }, 0, 200);
    }

    private void updateTaskLists() {
        taskQueueView.getItems().clear();
        for (Task task : taskManager.getTaskQueue()) {
           
            String priorityIndicator = task.getPriority() >= 8 ? "high priority" : 
                                      task.getPriority() >= 5 ? "medium" : "low";
            taskQueueView.getItems().add(priorityIndicator +  " Task-" + task.getId() + ": " + task.getName() +
                    " [P:" + task.getPriority() + "]");
        }

        activeTasksView.getItems().clear();
        for (Task task : taskManager.getActiveTasks()) {
            String icon = task instanceof ChargeTask ? "charge" : " ";
            activeTasksView.getItems().add(icon + " Task-" + task.getId() + ": " + task.getName() +
                    " [AGV: " + (task.getAssignedAGV() != null ? task.getAssignedAGV().getId() : "None") + "]");
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: red; -fx-font-weight: bold;"
                : "-fx-text-fill: green; -fx-font-weight: bold;");
        System.out.println(message);
    }

    private void redirectOutput() {
        System.setOut(new java.io.PrintStream(System.out) {
            @Override
            public void println(String x) {
                super.println(x);
                Platform.runLater(() -> logArea.appendText(x + "\n"));
            }
        });
        System.setErr(new java.io.PrintStream(System.err) {
            @Override
            public void println(String x) {
                super.println(x);
                Platform.runLater(() -> logArea.appendText(" " + x + "\n"));
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}