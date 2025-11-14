package client.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Status Controller for TeamSync
 * 
 * Manages connection status and peer discovery interface
 * Integrates with UDP heartbeat/peer discovery backend
 */
public class StatusController implements Initializable {
    
    @FXML
    private TableView<PeerInfo> peersTableView;
    
    @FXML
    private TableColumn<PeerInfo, String> peerNameColumn;
    
    @FXML
    private TableColumn<PeerInfo, String> peerAddressColumn;
    
    @FXML
    private TableColumn<PeerInfo, String> peerStatusColumn;
    
    @FXML
    private TableColumn<PeerInfo, String> lastSeenColumn;
    
    @FXML
    private Label totalPeersLabel;
    
    @FXML
    private Label activePeersLabel;
    
    @FXML
    private Label lastHeartbeatLabel;
    
    @FXML
    private Button startDiscoveryButton;
    
    @FXML
    private Button stopDiscoveryButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private TextArea discoveryLogArea;
    
    @FXML
    private ProgressIndicator heartbeatIndicator;
    
    // Backend integration
    private MainController mainController;
    private boolean discoveryActive = false;
    
    // UI data
    private ObservableList<PeerInfo> peersData;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🌐 StatusController initializing...");
        
        // Initialize UI data
        peersData = FXCollections.observableArrayList();
        
        // Setup UI components
        setupUI();
        
        // Setup event handlers
        setupEventHandlers();
        
        System.out.println("✅ StatusController initialized");
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Setup peers table
        setupPeersTable();
        
        // Setup discovery log
        discoveryLogArea.setEditable(false);
        discoveryLogArea.setWrapText(true);
        discoveryLogArea.getStyleClass().add("discovery-log");
        
        // Setup labels
        totalPeersLabel.setText("0");
        activePeersLabel.setText("0");
        lastHeartbeatLabel.setText("Never");
        
        // Setup heartbeat indicator
        heartbeatIndicator.setVisible(false);
        
        // Initial discovery status
        updateDiscoveryStatus(false);
    }
    
    /**
     * Setup peers table view
     */
    private void setupPeersTable() {
        // Setup columns
        peerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        peerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        peerStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        lastSeenColumn.setCellValueFactory(new PropertyValueFactory<>("lastSeen"));
        
        // Set data
        peersTableView.setItems(peersData);
        peersTableView.setPlaceholder(new Label("No peers discovered"));
        
        // Custom cell factories for styling
        peerStatusColumn.setCellFactory(column -> new TableCell<PeerInfo, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    getStyleClass().removeAll("status-online", "status-offline", "status-timeout");
                } else {
                    setText(status);
                    getStyleClass().removeAll("status-online", "status-offline", "status-timeout");
                    
                    switch (status.toLowerCase()) {
                        case "online":
                            getStyleClass().add("status-online");
                            break;
                        case "offline":
                            getStyleClass().add("status-offline");
                            break;
                        case "timeout":
                            getStyleClass().add("status-timeout");
                            break;
                    }
                }
            }
        });
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Start discovery button
        startDiscoveryButton.setOnAction(e -> startPeerDiscovery());
        
        // Stop discovery button
        stopDiscoveryButton.setOnAction(e -> stopPeerDiscovery());
        
        // Refresh button
        refreshButton.setOnAction(e -> refreshStatus());
        
        // Double-click on peer to view details
        peersTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                PeerInfo selectedPeer = peersTableView.getSelectionModel().getSelectedItem();
                if (selectedPeer != null) {
                    showPeerDetails(selectedPeer);
                }
            }
        });
    }
    
    /**
     * Initialize backend UDP discovery
     */
    public void initializeBackend() {
        System.out.println("🔗 Initializing UDP peer discovery backend...");
        
        try {
            // TODO: Initialize UDP discovery with proper configuration
            // udpDiscovery = new UDPDiscovery();
            
            addDiscoveryLog("System: UDP peer discovery module initialized");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing UDP discovery: " + e.getMessage());
            addDiscoveryLog("Error: Could not initialize UDP discovery - " + e.getMessage());
        }
    }
    
    /**
     * Start peer discovery
     */
    @FXML
    private void startPeerDiscovery() {
        if (discoveryActive) return;
        
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                try {
                    // TODO: Start actual UDP peer discovery
                    // udpDiscovery.startDiscovery();
                    
                    Platform.runLater(() -> {
                        setDiscoveryActive(true);
                        addDiscoveryLog("✅ Peer discovery started");
                        
                        if (mainController != null) {
                            mainController.showNotification("Peer discovery started");
                        }
                    });
                    
                    // Start discovery listener thread
                    startDiscoveryListener();
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addDiscoveryLog("❌ Error starting discovery: " + e.getMessage());
                        setDiscoveryActive(false);
                    });
                }
            });
        }
    }
    
    /**
     * Stop peer discovery
     */
    @FXML
    private void stopPeerDiscovery() {
        if (!discoveryActive) return;
        
        try {
            // TODO: Stop actual UDP peer discovery
            // udpDiscovery.stopDiscovery();
            
            setDiscoveryActive(false);
            addDiscoveryLog("🔌 Peer discovery stopped");
            
            if (mainController != null) {
                mainController.showNotification("Peer discovery stopped");
            }
            
        } catch (Exception e) {
            addDiscoveryLog("Error stopping discovery: " + e.getMessage());
        }
    }
    
    /**
     * Start background discovery listener thread
     */
    private void startDiscoveryListener() {
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                while (discoveryActive) {
                    try {
                        // TODO: Listen for actual UDP peer discoveries
                        // PeerInfo discoveredPeer = udpDiscovery.listenForPeers();
                        // if (discoveredPeer != null) {
                        //     Platform.runLater(() -> addOrUpdatePeer(discoveredPeer));
                        // }
                        
                        // Simulate peer discovery for demo
                        Thread.sleep(5000);
                        if (discoveryActive) {
                            Platform.runLater(() -> {
                                updateLastHeartbeat();
                                simulatePeerDiscovery();
                            });
                        }
                        
                    } catch (Exception e) {
                        if (discoveryActive) {
                            Platform.runLater(() -> {
                                addDiscoveryLog("Discovery error: " + e.getMessage());
                            });
                        }
                        break;
                    }
                }
            });
        }
    }
    
    /**
     * Simulate peer discovery for demo purposes
     */
    private void simulatePeerDiscovery() {
        if (peersData.size() < 3) {
            PeerInfo newPeer = new PeerInfo(
                "Peer-" + (peersData.size() + 1),
                "192.168.1." + (100 + peersData.size()),
                "Online",
                LocalDateTime.now().format(timeFormatter)
            );
            addOrUpdatePeer(newPeer);
        }
    }
    
    /**
     * Add or update peer in the list
     */
    public void addOrUpdatePeer(PeerInfo peerInfo) {
        Platform.runLater(() -> {
            // Check if peer already exists
            boolean found = false;
            for (int i = 0; i < peersData.size(); i++) {
                if (peersData.get(i).getName().equals(peerInfo.getName())) {
                    peersData.set(i, peerInfo);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                peersData.add(peerInfo);
                addDiscoveryLog("🔍 New peer discovered: " + peerInfo.getName() + " (" + peerInfo.getAddress() + ")");
            }
            
            updatePeerCounters();
        });
    }
    
    /**
     * Remove peer from the list
     */
    public void removePeer(String peerName) {
        Platform.runLater(() -> {
            peersData.removeIf(peer -> peer.getName().equals(peerName));
            addDiscoveryLog("❌ Peer lost: " + peerName);
            updatePeerCounters();
        });
    }
    
    /**
     * Update peer counters
     */
    private void updatePeerCounters() {
        int total = peersData.size();
        long active = peersData.stream().filter(peer -> "Online".equals(peer.getStatus())).count();
        
        totalPeersLabel.setText(String.valueOf(total));
        activePeersLabel.setText(String.valueOf(active));
    }
    
    /**
     * Update last heartbeat time
     */
    private void updateLastHeartbeat() {
        lastHeartbeatLabel.setText(LocalDateTime.now().format(timeFormatter));
        
        // Flash heartbeat indicator
        heartbeatIndicator.setVisible(true);
        Platform.runLater(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            heartbeatIndicator.setVisible(false);
        });
    }
    
    /**
     * Refresh status display
     */
    @FXML
    public void refreshStatus() {
        addDiscoveryLog("🔄 Refreshing peer status...");
        
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                try {
                    // TODO: Refresh actual peer status from backend
                    
                    Platform.runLater(() -> {
                        updatePeerCounters();
                        addDiscoveryLog("✅ Status refreshed");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addDiscoveryLog("❌ Error refreshing status: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Show peer details dialog
     */
    private void showPeerDetails(PeerInfo peer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Peer Details");
        alert.setHeaderText("Details for " + peer.getName());
        
        String details = String.format(
            "Name: %s\nAddress: %s\nStatus: %s\nLast Seen: %s",
            peer.getName(), peer.getAddress(), peer.getStatus(), peer.getLastSeen()
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    /**
     * Add message to discovery log (thread-safe)
     */
    private void addDiscoveryLog(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            discoveryLogArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }
    
    /**
     * Update discovery status
     */
    private void setDiscoveryActive(boolean active) {
        this.discoveryActive = active;
        updateDiscoveryStatus(active);
    }
    
    /**
     * Update UI discovery status
     */
    private void updateDiscoveryStatus(boolean active) {
        Platform.runLater(() -> {
            if (active) {
                startDiscoveryButton.setDisabled(true);
                stopDiscoveryButton.setDisabled(false);
                heartbeatIndicator.setVisible(true);
                
            } else {
                startDiscoveryButton.setDisabled(false);
                stopDiscoveryButton.setDisabled(true);
                heartbeatIndicator.setVisible(false);
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
     * Shutdown status controller
     */
    public void shutdown() {
        System.out.println("🛑 Shutting down StatusController...");
        
        if (discoveryActive) {
            stopPeerDiscovery();
        }
        
        // TODO: Cleanup UDP discovery resources
        
        System.out.println("✅ StatusController shutdown complete");
    }
    
    /**
     * Peer information data class
     */
    public static class PeerInfo {
        private String name;
        private String address;
        private String status;
        private String lastSeen;
        
        public PeerInfo(String name, String address, String status, String lastSeen) {
            this.name = name;
            this.address = address;
            this.status = status;
            this.lastSeen = lastSeen;
        }
        
        // Getters
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getStatus() { return status; }
        public String getLastSeen() { return lastSeen; }
        
        // Setters
        public void setName(String name) { this.name = name; }
        public void setAddress(String address) { this.address = address; }
        public void setStatus(String status) { this.status = status; }
        public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }
    }
}
