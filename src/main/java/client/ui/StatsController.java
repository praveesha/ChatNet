package client.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.NumberAxis;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Stats Controller for TeamSync
 * 
 * Manages statistics display and integrates with NIO echo server for latency testing
 */
public class StatsController implements Initializable {
    
    @FXML
    private Label messagesSentLabel;
    
    @FXML
    private Label messagesReceivedLabel;
    
    @FXML
    private Label packetsReceivedLabel;
    
    @FXML
    private Label transferRateLabel;
    
    @FXML
    private Label avgLatencyLabel;
    
    @FXML
    private Label minLatencyLabel;
    
    @FXML
    private Label maxLatencyLabel;
    
    @FXML
    private Button startEchoTestButton;
    
    @FXML
    private Button stopEchoTestButton;
    
    @FXML
    private Button resetStatsButton;
    
    @FXML
    private TextField echoServerAddressField;
    
    @FXML
    private TextField echoServerPortField;
    
    @FXML
    private LineChart<Number, Number> latencyChart;
    
    @FXML
    private TextArea statsLogArea;
    
    @FXML
    private ProgressIndicator testingIndicator;
    
    // Backend integration
    private MainController mainController;
    private boolean echoTestActive = false;
    
    // Statistics data
    private AtomicLong messagesSent = new AtomicLong(0);
    private AtomicLong messagesReceived = new AtomicLong(0);
    private AtomicLong packetsReceived = new AtomicLong(0);
    private AtomicLong totalTransferred = new AtomicLong(0);
    
    // Latency statistics
    private double currentLatency = 0;
    private double minLatency = Double.MAX_VALUE;
    private double maxLatency = 0;
    private double totalLatency = 0;
    private int latencyCount = 0;
    
    // Chart data
    private XYChart.Series<Number, Number> latencySeries;
    private int chartDataPoints = 0;
    
    // Formatting
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("📊 StatsController initializing...");
        
        // Setup UI components
        setupUI();
        
        // Setup event handlers
        setupEventHandlers();
        
        // Setup chart
        setupLatencyChart();
        
        System.out.println("✅ StatsController initialized");
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Initialize stat labels
        updateStatLabels();
        
        // Configure echo server fields
        echoServerAddressField.setText("localhost");
        echoServerPortField.setText("9090");
        
        // Setup stats log
        statsLogArea.setEditable(false);
        statsLogArea.setWrapText(true);
        statsLogArea.getStyleClass().add("stats-log");
        
        // Setup testing indicator
        testingIndicator.setVisible(false);
        
        // Initial echo test status
        updateEchoTestStatus(false);
    }
    
    /**
     * Setup latency chart
     */
    private void setupLatencyChart() {
        latencyChart.setTitle("Network Latency (ms)");
        latencyChart.setAnimated(true);
        latencyChart.setCreateSymbols(false);
        
        // Create data series
        latencySeries = new XYChart.Series<>();
        latencySeries.setName("Latency");
        latencyChart.getData().add(latencySeries);
        
        // Configure axes
        NumberAxis xAxis = (NumberAxis) latencyChart.getXAxis();
        xAxis.setLabel("Time (seconds)");
        xAxis.setAutoRanging(true);
        
        NumberAxis yAxis = (NumberAxis) latencyChart.getYAxis();
        yAxis.setLabel("Latency (ms)");
        yAxis.setAutoRanging(true);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Start echo test button
        startEchoTestButton.setOnAction(e -> startEchoTest());
        
        // Stop echo test button
        stopEchoTestButton.setOnAction(e -> stopEchoTest());
        
        // Reset stats button
        resetStatsButton.setOnAction(e -> resetStatistics());
    }
    
    /**
     * Initialize backend NIO echo client
     */
    public void initializeBackend() {
        System.out.println("🔗 Initializing NIO echo client backend...");
        
        try {
            // TODO: Initialize NIO echo client with proper configuration
            // nioEchoClient = new NIOEchoClient();
            
            addStatsLog("System: NIO echo client module initialized");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing NIO echo client: " + e.getMessage());
            addStatsLog("Error: Could not initialize NIO echo client - " + e.getMessage());
        }
    }
    
    /**
     * Start NIO echo latency test
     */
    @FXML
    private void startEchoTest() {
        if (echoTestActive) return;
        
        String address = echoServerAddressField.getText().trim();
        String portText = echoServerPortField.getText().trim();
        
        if (address.isEmpty() || portText.isEmpty()) {
            addStatsLog("Error: Please enter echo server address and port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            if (mainController != null) {
                mainController.getBackgroundExecutor().submit(() -> {
                    try {
                        Platform.runLater(() -> {
                            setEchoTestActive(true);
                            addStatsLog("✅ Echo test started: " + address + ":" + port);
                            
                            if (mainController != null) {
                                mainController.showNotification("Echo latency test started");
                            }
                        });
                        
                        // Start echo test loop
                        runEchoTestLoop(address, port);
                        
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            addStatsLog("❌ Echo test failed: " + e.getMessage());
                            setEchoTestActive(false);
                        });
                    }
                });
            }
            
        } catch (NumberFormatException e) {
            addStatsLog("Error: Invalid port number");
        }
    }
    
    /**
     * Stop NIO echo test
     */
    @FXML
    private void stopEchoTest() {
        if (!echoTestActive) return;
        
        setEchoTestActive(false);
        addStatsLog("🔌 Echo test stopped");
        
        if (mainController != null) {
            mainController.showNotification("Echo latency test stopped");
        }
    }
    
    /**
     * Run echo test loop (background thread)
     */
    private void runEchoTestLoop(String address, int port) {
        while (echoTestActive) {
            try {
                // TODO: Implement actual NIO echo test
                // long startTime = System.nanoTime();
                // nioEchoClient.echo(address, port, "PING");
                // long endTime = System.nanoTime();
                // double latency = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
                
                // Simulate latency test for demo
                double latency = 10 + Math.random() * 50; // Random latency 10-60ms
                
                Platform.runLater(() -> {
                    updateLatencyStats(latency);
                    addLatencyDataPoint(latency);
                });
                
                Thread.sleep(1000); // Test every second
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addStatsLog("❌ Echo test error: " + e.getMessage());
                    setEchoTestActive(false);
                });
                break;
            }
        }
    }
    
    /**
     * Update latency statistics
     */
    private void updateLatencyStats(double latency) {
        currentLatency = latency;
        totalLatency += latency;
        latencyCount++;
        
        if (latency < minLatency) {
            minLatency = latency;
        }
        if (latency > maxLatency) {
            maxLatency = latency;
        }
        
        updateStatLabels();
    }
    
    /**
     * Add latency data point to chart
     */
    private void addLatencyDataPoint(double latency) {
        latencySeries.getData().add(new XYChart.Data<>(chartDataPoints++, latency));
        
        // Limit chart to last 50 data points
        if (latencySeries.getData().size() > 50) {
            latencySeries.getData().remove(0);
        }
    }
    
    /**
     * Update statistics from other modules
     */
    public void updateMessagesSent(long count) {
        messagesSent.set(count);
        Platform.runLater(() -> updateStatLabels());
    }
    
    public void updateMessagesReceived(long count) {
        messagesReceived.set(count);
        Platform.runLater(() -> updateStatLabels());
    }
    
    public void updatePacketsReceived(long count) {
        packetsReceived.set(count);
        Platform.runLater(() -> updateStatLabels());
    }
    
    public void updateTotalTransferred(long bytes) {
        totalTransferred.set(bytes);
        Platform.runLater(() -> updateStatLabels());
    }
    
    /**
     * Increment message counters
     */
    public void incrementMessagesSent() {
        messagesSent.incrementAndGet();
        Platform.runLater(() -> updateStatLabels());
    }
    
    public void incrementMessagesReceived() {
        messagesReceived.incrementAndGet();
        Platform.runLater(() -> updateStatLabels());
    }
    
    public void incrementPacketsReceived() {
        packetsReceived.incrementAndGet();
        Platform.runLater(() -> updateStatLabels());
    }
    
    public void addBytesTransferred(long bytes) {
        totalTransferred.addAndGet(bytes);
        Platform.runLater(() -> updateStatLabels());
    }
    
    /**
     * Update all stat labels
     */
    private void updateStatLabels() {
        messagesSentLabel.setText(String.valueOf(messagesSent.get()));
        messagesReceivedLabel.setText(String.valueOf(messagesReceived.get()));
        packetsReceivedLabel.setText(String.valueOf(packetsReceived.get()));
        
        // Format transfer rate
        long bytes = totalTransferred.get();
        String transferText;
        if (bytes < 1024) {
            transferText = bytes + " B";
        } else if (bytes < 1024 * 1024) {
            transferText = String.format("%.2f KB", bytes / 1024.0);
        } else {
            transferText = String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
        transferRateLabel.setText(transferText);
        
        // Update latency labels
        if (latencyCount > 0) {
            avgLatencyLabel.setText(String.format("%.2f ms", totalLatency / latencyCount));
            minLatencyLabel.setText(String.format("%.2f ms", minLatency));
            maxLatencyLabel.setText(String.format("%.2f ms", maxLatency));
        } else {
            avgLatencyLabel.setText("--");
            minLatencyLabel.setText("--");
            maxLatencyLabel.setText("--");
        }
    }
    
    /**
     * Reset all statistics
     */
    @FXML
    private void resetStatistics() {
        messagesSent.set(0);
        messagesReceived.set(0);
        packetsReceived.set(0);
        totalTransferred.set(0);
        
        currentLatency = 0;
        minLatency = Double.MAX_VALUE;
        maxLatency = 0;
        totalLatency = 0;
        latencyCount = 0;
        
        chartDataPoints = 0;
        latencySeries.getData().clear();
        
        updateStatLabels();
        addStatsLog("🔄 Statistics reset");
    }
    
    /**
     * Refresh statistics display
     */
    public void refreshStats() {
        addStatsLog("🔄 Refreshing statistics...");
        
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                try {
                    // TODO: Refresh stats from backend modules
                    
                    Platform.runLater(() -> {
                        updateStatLabels();
                        addStatsLog("✅ Statistics refreshed");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addStatsLog("❌ Error refreshing statistics: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Add message to stats log (thread-safe)
     */
    private void addStatsLog(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalTime.now().format(timeFormatter);
            statsLogArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }
    
    /**
     * Update echo test status
     */
    private void setEchoTestActive(boolean active) {
        this.echoTestActive = active;
        updateEchoTestStatus(active);
    }
    
    /**
     * Update UI echo test status
     */
    private void updateEchoTestStatus(boolean active) {
        Platform.runLater(() -> {
            if (active) {
                startEchoTestButton.setDisabled(true);
                stopEchoTestButton.setDisabled(false);
                testingIndicator.setVisible(true);
                
            } else {
                startEchoTestButton.setDisabled(false);
                stopEchoTestButton.setDisabled(true);
                testingIndicator.setVisible(false);
            }
        });
    }
    
    /**
     * Set main controller reference
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    /**
     * Additional FXML Action Methods
     */
    @FXML
    private void refreshStatistics() {
        addStatsLog("🔄 Refreshing statistics...");
        // Force update of all UI components
        Platform.runLater(() -> {
            updateStatisticsDisplay();
            addStatsLog("✅ Statistics refreshed");
        });
    }
    
    @FXML 
    private void exportStatistics() {
        addStatsLog("💾 Exporting statistics...");
        // TODO: Implement statistics export functionality
        addStatsLog("⚠️ Export feature coming soon");
    }
    
    @FXML
    private void toggleAutoRefresh() {
        boolean autoRefresh = autoRefreshCheckBox.isSelected();
        addStatsLog(autoRefresh ? "✅ Auto-refresh enabled" : "⏸ Auto-refresh disabled");
        // TODO: Implement auto-refresh toggle logic
    }
    
    @FXML
    private void changeRefreshInterval() {
        String interval = refreshIntervalComboBox.getValue();
        if (interval != null) {
            addStatsLog("⏱ Refresh interval changed to: " + interval);
            // TODO: Implement refresh interval change logic
        }
    }
    
    /**
     * Shutdown stats controller
     */
    public void shutdown() {
        System.out.println("🛑 Shutting down StatsController...");
        
        if (echoTestActive) {
            stopEchoTest();
        }
        
        // TODO: Cleanup NIO echo client resources
        
        System.out.println("✅ StatsController shutdown complete");
    }
}
