package client.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import client.nio.NioClient;
import client.udp.UdpReceiver;
import client.file_client.EnhancedFileClient;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main Controller for TeamSync JavaFX Application
 * 
 * Integrates all backend modules with JavaFX UI:
 * - Chat functionality (existing - do not modify)
 * - File transfer operations
 * - UDP peer discovery  
 * - NIO echo client
 * - Network statistics
 */
public class MainController implements Initializable {
    
    // ===== MAIN UI COMPONENTS =====
    @FXML private TabPane mainTabPane;
    @FXML private Tab chatTab;
    @FXML private Tab fileTab;
    @FXML private Tab statusTab;
    @FXML private Tab statsTab;
    
    // ===== FILE TAB COMPONENTS =====
    @FXML private TextField fileServerHost;
    @FXML private TextField fileServerPort;
    @FXML private Button connectFileButton;
    @FXML private Button disconnectFileButton;
    @FXML private Button uploadFileButton;
    @FXML private Button downloadFileButton;
    @FXML private Button refreshFilesButton;
    @FXML private ListView<String> serverFilesList;
    @FXML private ListView<String> localFilesList;
    @FXML private TextArea fileLogArea;
    @FXML private ProgressBar fileProgressBar;
    @FXML private Label fileStatusLabel;
    
    // ===== UDP TAB COMPONENTS =====
    @FXML private TextField udpListenPort;
    @FXML private Button startUdpButton;
    @FXML private Button stopUdpButton;
    @FXML private Button startDiscoveryButton;
    @FXML private Button stopDiscoveryButton;
    @FXML private ListView<String> peersListView;
    @FXML private TextArea udpLogArea;
    @FXML private Label udpStatusLabel;
    @FXML private Label peersCountLabel;
    
    // ===== NIO TAB COMPONENTS =====
    @FXML private TextField nioServerHost;
    @FXML private TextField nioServerPort;
    @FXML private Button connectNioButton;
    @FXML private Button disconnectNioButton;
    @FXML private TextField echoMessageField;
    @FXML private Button sendEchoButton;
    @FXML private Button measureLatencyButton;
    @FXML private TextArea nioLogArea;
    @FXML private Label nioStatusLabel;
    @FXML private Label latencyLabel;
    
    // ===== STATS TAB COMPONENTS =====
    @FXML private Label messageCountLabel;
    @FXML private Label connectionUptimeLabel;
    @FXML private Label bytesSentLabel;
    @FXML private Label bytesReceivedLabel;
    @FXML private Label fileTransfersLabel;
    @FXML private Label discoveredPeersLabel;
    @FXML private Label nioLatencyLabel;
    @FXML private Button refreshStatsButton;
    @FXML private Button resetStatsButton;
    @FXML private TextArea statsLogArea;
    
    // ===== BACKEND MODULES =====
    private NioClient nioClient;
    private UdpReceiver udpReceiver;
    private EnhancedFileClient fileClient;
    
    // ===== DATA COLLECTIONS =====
    private ObservableList<String> serverFiles = FXCollections.observableArrayList();
    private ObservableList<String> localFiles = FXCollections.observableArrayList();
    private ObservableList<String> discoveredPeers = FXCollections.observableArrayList();
    
    // ===== UTILITIES =====
    private ExecutorService backgroundExecutor;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // ===== STATISTICS =====
    private long applicationStartTime;
    private long totalMessagesSent = 0;
    private long totalBytesTransferred = 0;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔧 MainController initializing...");
        
        applicationStartTime = System.currentTimeMillis();
        
        // Initialize background thread pool
        backgroundExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("TeamSync-Background");
            return t;
        });
        
        // Initialize backend modules
        initializeBackendModules();
        
        // Setup UI components
        setupUIComponents();
        
        // Set up tab selection listeners for auto-refresh
        setupTabListeners();
        
        System.out.println("✅ MainController initialized successfully!");
    }
    
    /**
     * Initialize all backend modules
     */
    private void initializeBackendModules() {
        System.out.println("🔧 Initializing backend modules...");
        
        // Initialize NIO Client
        nioClient = new NioClient();
        
        // Initialize UDP Receiver with callbacks
        udpReceiver = new UdpReceiver();
        udpReceiver.setMessageCallback(this::onUdpMessage);
        udpReceiver.setPeersCallback(this::onPeersUpdated);
        
        // Initialize Enhanced File Client with callbacks
        fileClient = new EnhancedFileClient();
        fileClient.setStatusCallback(this::onFileStatus);
        fileClient.setProgressCallback(this::onFileProgress);
    }
    
    /**
     * Setup UI components and bind data
     */
    private void setupUIComponents() {
        // Bind data to list views
        if (serverFilesList != null) serverFilesList.setItems(serverFiles);
        if (localFilesList != null) localFilesList.setItems(localFiles);
        if (peersListView != null) peersListView.setItems(discoveredPeers);
        
        // Set default values
        if (fileServerHost != null) fileServerHost.setText("localhost");
        if (fileServerPort != null) fileServerPort.setText("12346");
        if (udpListenPort != null) udpListenPort.setText("8888");
        if (nioServerHost != null) nioServerHost.setText("localhost");
        if (nioServerPort != null) nioServerPort.setText("9090");
        if (echoMessageField != null) echoMessageField.setText("Hello NIO Server!");
        
        // Initialize logs
        logToFile("File transfer system initialized");
        logToUdp("UDP system initialized");
        logToNio("NIO system initialized");
        logToStats("Statistics system initialized");
        
        refreshLocalFiles();
        updateAllStatistics();
    }
    
    /**
     * Setup tab selection listeners
     */
    private void setupTabListeners() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    System.out.println("📋 Switched to tab: " + newTab.getText());
                    
                    // Auto-refresh data when switching tabs
                    Platform.runLater(() -> {
                        if (newTab == statsTab) {
                            updateAllStatistics();
                        }
                    });
                }
            });
        }
    }
    
    // ===== FILE TAB HANDLERS =====
    
    @FXML
    public void connectToFileServer() {
        System.out.println("🔗 File: Connect button clicked");
        logToFile("Connecting to file server...");
        
        String host = fileServerHost.getText().trim();
        String portText = fileServerPort.getText().trim();
        
        if (host.isEmpty() || portText.isEmpty()) {
            updateFileStatus("Error: Please enter server host and port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            fileClient = new EnhancedFileClient(host, port);
            fileClient.setStatusCallback(this::onFileStatus);
            fileClient.setProgressCallback(this::onFileProgress);
            
            fileClient.connect().thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        updateFileStatus("✅ Connected to " + host + ":" + port);
                        logToFile("Connected to file server: " + host + ":" + port);
                        refreshServerFiles();
                    } else {
                        updateFileStatus("❌ Failed to connect to file server");
                    }
                });
            });
            
        } catch (NumberFormatException e) {
            updateFileStatus("Error: Invalid port number");
        }
    }
    
    @FXML
    public void disconnectFromFileServer() {
        System.out.println("🔌 File: Disconnect button clicked");
        fileClient.disconnect();
        updateFileStatus("Disconnected from file server");
        logToFile("Disconnected from file server");
    }
    
    @FXML
    private void uploadFile() {
        System.out.println("📤 File: Upload button clicked");
        
        Stage stage = (Stage) uploadFileButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to upload");
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            logToFile("Starting upload: " + selectedFile.getName());
            
            fileClient.upload(selectedFile.getAbsolutePath()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        totalMessagesSent++;
                        totalBytesTransferred += selectedFile.length();
                        logToFile("✅ Upload completed: " + selectedFile.getName());
                        refreshServerFiles();
                    } else {
                        logToFile("❌ Upload failed: " + selectedFile.getName());
                    }
                    updateAllStatistics();
                });
            });
        }
    }
    
    @FXML
    private void downloadFile() {
        System.out.println("📥 File: Download button clicked");
        
        String selectedFile = serverFilesList.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            updateFileStatus("Error: Please select a file to download");
            return;
        }
        
        Stage stage = (Stage) downloadFileButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save downloaded file");
        fileChooser.setInitialFileName(selectedFile);
        File saveFile = fileChooser.showSaveDialog(stage);
        
        if (saveFile != null) {
            logToFile("Starting download: " + selectedFile);
            
            fileClient.download(selectedFile, saveFile.getParent()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        totalMessagesSent++;
                        totalBytesTransferred += saveFile.length();
                        logToFile("✅ Download completed: " + selectedFile);
                        refreshLocalFiles();
                    } else {
                        logToFile("❌ Download failed: " + selectedFile);
                    }
                    updateAllStatistics();
                });
            });
        }
    }
    
    @FXML
    private void refreshServerFiles() {
        System.out.println("🔄 File: Refresh server files clicked");
        
        fileClient.listFiles().thenAccept(files -> {
            Platform.runLater(() -> {
                serverFiles.clear();
                serverFiles.addAll(files);
                logToFile("Server files refreshed: " + files.size() + " files");
            });
        });
    }
    
    private void refreshLocalFiles() {
        // Simulate local files (in a real app, you'd scan a directory)
        localFiles.clear();
        localFiles.addAll(List.of(
            "document.txt", 
            "image.png", 
            "data.csv",
            "presentation.pdf"
        ));
    }
    
    // ===== UDP TAB HANDLERS =====
    
    @FXML
    private void startUdpListening() {
        System.out.println("🔊 UDP: Start listening clicked");
        
        String portText = udpListenPort.getText().trim();
        if (portText.isEmpty()) {
            updateUdpStatus("Error: Please enter UDP port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            udpReceiver = new UdpReceiver(port);
            udpReceiver.setMessageCallback(this::onUdpMessage);
            udpReceiver.setPeersCallback(this::onPeersUpdated);
            
            udpReceiver.startListening().thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        updateUdpStatus("✅ UDP listening on port " + port);
                        logToUdp("Started UDP listening on port " + port);
                    } else {
                        updateUdpStatus("❌ Failed to start UDP listener");
                    }
                });
            });
            
        } catch (NumberFormatException e) {
            updateUdpStatus("Error: Invalid port number");
        }
    }
    
    @FXML
    private void stopUdpListening() {
        System.out.println("🔇 UDP: Stop listening clicked");
        udpReceiver.stopListening();
        updateUdpStatus("UDP listening stopped");
        logToUdp("Stopped UDP listening");
    }
    
    @FXML
    private void startPeerDiscovery() {
        System.out.println("🔍 UDP: Start discovery clicked");
        
        udpReceiver.startPeerDiscovery().thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    updateUdpStatus("✅ Peer discovery active");
                    logToUdp("Started peer discovery");
                } else {
                    updateUdpStatus("❌ Failed to start peer discovery");
                }
            });
        });
    }
    
    @FXML
    private void stopPeerDiscovery() {
        System.out.println("🛑 UDP: Stop discovery clicked");
        udpReceiver.stopPeerDiscovery();
        discoveredPeers.clear();
        updateUdpStatus("Peer discovery stopped");
        updatePeersCount(0);
        logToUdp("Stopped peer discovery");
    }
    
    // ===== NIO TAB HANDLERS =====
    
    @FXML
    private void connectToNioServer() {
        System.out.println("🔗 NIO: Connect button clicked");
        
        String host = nioServerHost.getText().trim();
        String portText = nioServerPort.getText().trim();
        
        if (host.isEmpty() || portText.isEmpty()) {
            updateNioStatus("Error: Please enter server host and port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            nioClient = new NioClient(host, port);
            
            nioClient.connect().thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        updateNioStatus("✅ Connected to " + host + ":" + port);
                        logToNio("Connected to NIO server: " + host + ":" + port);
                    } else {
                        updateNioStatus("❌ Failed to connect to NIO server");
                    }
                });
            });
            
        } catch (NumberFormatException e) {
            updateNioStatus("Error: Invalid port number");
        }
    }
    
    @FXML
    private void disconnectFromNioServer() {
        System.out.println("🔌 NIO: Disconnect button clicked");
        nioClient.disconnect();
        updateNioStatus("Disconnected from NIO server");
        logToNio("Disconnected from NIO server");
    }
    
    @FXML
    private void sendEchoMessage() {
        System.out.println("📤 NIO: Send echo clicked");
        
        String message = echoMessageField.getText().trim();
        if (message.isEmpty()) {
            updateNioStatus("Error: Please enter a message");
            return;
        }
        
        logToNio("Sending echo: " + message);
        
        nioClient.sendEcho(message).thenAccept(response -> {
            Platform.runLater(() -> {
                totalMessagesSent++;
                totalBytesTransferred += message.length();
                logToNio("Echo response: " + response);
                updateAllStatistics();
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                logToNio("❌ Echo failed: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    @FXML
    private void measureLatency() {
        System.out.println("⏱ NIO: Measure latency clicked");
        
        String testMessage = "PING_" + System.currentTimeMillis();
        logToNio("Measuring latency with: " + testMessage);
        
        nioClient.measureLatency(testMessage).thenAccept(latency -> {
            Platform.runLater(() -> {
                if (latency >= 0) {
                    updateLatency(latency + " ms");
                    logToNio("✅ Latency: " + latency + " ms");
                } else {
                    updateLatency("Failed");
                    logToNio("❌ Latency measurement failed");
                }
            });
        });
    }
    
    // ===== STATS TAB HANDLERS =====
    
    @FXML
    private void refreshStatistics() {
        System.out.println("🔄 Stats: Refresh clicked");
        updateAllStatistics();
        logToStats("Statistics refreshed");
    }
    
    @FXML
    private void resetStatistics() {
        System.out.println("🗑 Stats: Reset clicked");
        totalMessagesSent = 0;
        totalBytesTransferred = 0;
        applicationStartTime = System.currentTimeMillis();
        
        updateAllStatistics();
        logToStats("Statistics reset");
    }
    
    // ===== CALLBACK HANDLERS =====
    
    private void onUdpMessage(String message) {
        Platform.runLater(() -> logToUdp("📦 " + message));
    }
    
    private void onPeersUpdated(List<UdpReceiver.PeerInfo> peers) {
        Platform.runLater(() -> {
            discoveredPeers.clear();
            peers.forEach(peer -> discoveredPeers.add(peer.toString()));
            updatePeersCount(peers.size());
            logToUdp("🔍 Peers updated: " + peers.size() + " discovered");
        });
    }
    
    private void onFileStatus(String status) {
        Platform.runLater(() -> updateFileStatus(status));
    }
    
    private void onFileProgress(String progress) {
        Platform.runLater(() -> {
            if (fileProgressBar != null) {
                // Extract percentage if available
                try {
                    if (progress.contains("%")) {
                        String percentStr = progress.substring(progress.indexOf("g... ") + 5, progress.indexOf("%"));
                        double percent = Double.parseDouble(percentStr) / 100.0;
                        fileProgressBar.setProgress(percent);
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        });
    }
    
    // ===== UI UPDATE METHODS =====
    
    private void updateFileStatus(String status) {
        if (fileStatusLabel != null) fileStatusLabel.setText(status);
    }
    
    private void updateUdpStatus(String status) {
        if (udpStatusLabel != null) udpStatusLabel.setText(status);
    }
    
    private void updatePeersCount(int count) {
        if (peersCountLabel != null) peersCountLabel.setText("Peers: " + count);
    }
    
    private void updateNioStatus(String status) {
        if (nioStatusLabel != null) nioStatusLabel.setText(status);
    }
    
    private void updateLatency(String latency) {
        if (latencyLabel != null) latencyLabel.setText("Latency: " + latency);
    }
    
    private void updateAllStatistics() {
        long uptime = System.currentTimeMillis() - applicationStartTime;
        String uptimeStr = formatUptime(uptime);
        
        if (messageCountLabel != null) messageCountLabel.setText("Messages: " + totalMessagesSent);
        if (connectionUptimeLabel != null) connectionUptimeLabel.setText("Uptime: " + uptimeStr);
        if (bytesSentLabel != null) bytesSentLabel.setText("Bytes Sent: " + formatBytes(nioClient.getBytesSent()));
        if (bytesReceivedLabel != null) bytesReceivedLabel.setText("Bytes Received: " + formatBytes(nioClient.getBytesReceived()));
        if (fileTransfersLabel != null) fileTransfersLabel.setText("File Transfers: " + (fileClient.getFilesUploaded() + fileClient.getFilesDownloaded()));
        if (discoveredPeersLabel != null) discoveredPeersLabel.setText("Discovered Peers: " + udpReceiver.getPeerCount());
        if (nioLatencyLabel != null) nioLatencyLabel.setText("NIO Messages: " + (nioClient.getMessagesSent() + nioClient.getMessagesReceived()));
    }
    
    // ===== LOGGING METHODS =====
    
    private void logToFile(String message) {
        if (fileLogArea != null) {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            Platform.runLater(() -> fileLogArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }
    
    private void logToUdp(String message) {
        if (udpLogArea != null) {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            Platform.runLater(() -> udpLogArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }
    
    private void logToNio(String message) {
        if (nioLogArea != null) {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            Platform.runLater(() -> nioLogArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }
    
    private void logToStats(String message) {
        if (statsLogArea != null) {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            Platform.runLater(() -> statsLogArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }
    
    // ===== UTILITY METHODS =====
    
    private String formatUptime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
    
    // ===== LIFECYCLE METHODS =====
    
    /**
     * Initialize all backend connections (called from Main.java)
     */
    public void initializeBackendConnections() {
        System.out.println("🔗 Backend connections already initialized");
        // Backend modules are initialized in initialize() method
    }
    
    /**
     * Shutdown all backend connections and cleanup resources
     */
    public void shutdown() {
        System.out.println("🛑 Shutting down TeamSync application...");
        
        // Shutdown backend modules
        if (nioClient != null) {
            nioClient.shutdown();
        }
        if (udpReceiver != null) {
            udpReceiver.shutdown();
        }
        if (fileClient != null) {
            fileClient.shutdown();
        }
        
        // Shutdown background executor
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
        }
        
        System.out.println("✅ TeamSync application shutdown complete");
    }
    
    /**
     * Get the background executor for other components
     */
    public ExecutorService getBackgroundExecutor() {
        return backgroundExecutor;
    }
}
