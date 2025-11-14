package client.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import client.file_client.FileClient;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * File Controller for TeamSync
 * 
 * Manages file transfer interface and integrates with FileClient backend
 */
public class FileController implements Initializable {
    
    @FXML
    private ListView<String> serverFilesListView;
    
    @FXML
    private ListView<String> localFilesListView;
    
    @FXML
    private Button uploadButton;
    
    @FXML
    private Button downloadButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button connectButton;
    
    @FXML
    private TextField serverAddressField;
    
    @FXML
    private TextField serverPortField;
    
    @FXML
    private ProgressBar transferProgressBar;
    
    @FXML
    private Label transferStatusLabel;
    
    @FXML
    private Label connectionStatusLabel;
    
    @FXML
    private TextArea transferLogArea;
    
    // Backend integration
    private FileClient fileClient;
    private MainController mainController;
    private boolean isConnected = false;
    
    // UI data
    private ObservableList<String> serverFiles;
    private ObservableList<String> localFiles;
    private FileChooser fileChooser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("📁 FileController initializing...");
        
        // Initialize UI data
        serverFiles = FXCollections.observableArrayList();
        localFiles = FXCollections.observableArrayList();
        
        // Setup UI components
        setupUI();
        
        // Setup event handlers
        setupEventHandlers();
        
        System.out.println("✅ FileController initialized");
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Setup list views
        serverFilesListView.setItems(serverFiles);
        serverFilesListView.setPlaceholder(new Label("No server files available"));
        serverFilesListView.getStyleClass().add("file-list");
        
        localFilesListView.setItems(localFiles);
        localFilesListView.setPlaceholder(new Label("No local files selected"));
        localFilesListView.getStyleClass().add("file-list");
        
        // Configure server fields
        serverAddressField.setText("localhost");
        serverPortField.setText("8081");
        
        // Setup transfer components
        transferProgressBar.setProgress(0);
        transferProgressBar.setVisible(false);
        transferStatusLabel.setText("Ready");
        
        // Setup transfer log
        transferLogArea.setEditable(false);
        transferLogArea.setWrapText(true);
        transferLogArea.getStyleClass().add("transfer-log");
        
        // Initialize file chooser
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        
        // Initial connection status
        updateConnectionStatus(false);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Connect button
        connectButton.setOnAction(e -> connectToFileServer());
        
        // Upload button
        uploadButton.setOnAction(e -> uploadFile());
        
        // Download button
        downloadButton.setOnAction(e -> downloadFile());
        
        // Refresh button
        refreshButton.setOnAction(e -> refreshServerFiles());
        
        // Double-click to download
        serverFilesListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                downloadFile();
            }
        });
    }
    
    /**
     * Initialize backend FileClient connection
     */
    public void initializeBackend() {
        System.out.println("🔗 Initializing FileClient backend...");
        
        try {
            // TODO: Initialize FileClient with proper configuration
            // fileClient = new FileClient();
            
            addTransferLog("System: File transfer module initialized");
            loadLocalFiles();
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing FileClient: " + e.getMessage());
            addTransferLog("Error: Could not initialize file transfer module - " + e.getMessage());
        }
    }
    
    /**
     * Connect to file server
     */
    @FXML
    private void connectToFileServer() {
        String address = serverAddressField.getText().trim();
        String portText = serverPortField.getText().trim();
        
        if (address.isEmpty() || portText.isEmpty()) {
            addTransferLog("Error: Please enter server address and port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            connectButton.setDisabled(true);
            connectButton.setText("Connecting...");
            
            if (mainController != null) {
                mainController.getBackgroundExecutor().submit(() -> {
                    try {
                        // TODO: Implement actual FileClient connection
                        // fileClient.connect(address, port);
                        
                        Platform.runLater(() -> {
                            setConnected(true);
                            addTransferLog("✅ Connected to file server: " + address + ":" + port);
                            refreshServerFiles();
                            
                            if (mainController != null) {
                                mainController.showNotification("Connected to file server");
                            }
                        });
                        
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            addTransferLog("❌ Connection failed: " + e.getMessage());
                            setConnected(false);
                        });
                    }
                });
            }
            
        } catch (NumberFormatException e) {
            addTransferLog("Error: Invalid port number");
            connectButton.setDisabled(false);
            connectButton.setText("Connect");
        }
    }
    
    /**
     * Upload file to server
     */
    @FXML
    private void uploadFile() {
        if (!isConnected) {
            addTransferLog("Error: Not connected to server");
            return;
        }
        
        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            uploadFileToServer(selectedFile);
        }
    }
    
    /**
     * Download selected file from server
     */
    @FXML
    private void downloadFile() {
        if (!isConnected) {
            addTransferLog("Error: Not connected to server");
            return;
        }
        
        String selectedFile = serverFilesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            addTransferLog("Error: Please select a file to download");
            return;
        }
        
        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save File As");
        saveChooser.setInitialFileName(selectedFile);
        
        Stage stage = (Stage) downloadButton.getScene().getWindow();
        File saveFile = saveChooser.showSaveDialog(stage);
        
        if (saveFile != null) {
            downloadFileFromServer(selectedFile, saveFile);
        }
    }
    
    /**
     * Upload file to server (background operation)
     */
    private void uploadFileToServer(File file) {
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                try {
                    Platform.runLater(() -> {
                        transferProgressBar.setVisible(true);
                        transferProgressBar.setProgress(0);
                        transferStatusLabel.setText("Uploading: " + file.getName());
                        uploadButton.setDisabled(true);
                    });
                    
                    // TODO: Implement actual file upload with progress callback
                    // fileClient.uploadFile(file, progress -> {
                    //     Platform.runLater(() -> transferProgressBar.setProgress(progress));
                    // });
                    
                    // Simulate upload progress
                    for (int i = 0; i <= 100; i += 10) {
                        Thread.sleep(100);
                        final int progress = i;
                        Platform.runLater(() -> transferProgressBar.setProgress(progress / 100.0));
                    }
                    
                    Platform.runLater(() -> {
                        transferProgressBar.setVisible(false);
                        transferStatusLabel.setText("Upload completed");
                        uploadButton.setDisabled(false);
                        addTransferLog("✅ Upload completed: " + file.getName());
                        refreshServerFiles();
                        loadLocalFiles();
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        transferProgressBar.setVisible(false);
                        transferStatusLabel.setText("Upload failed");
                        uploadButton.setDisabled(false);
                        addTransferLog("❌ Upload failed: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Download file from server (background operation)
     */
    private void downloadFileFromServer(String filename, File saveFile) {
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                try {
                    Platform.runLater(() -> {
                        transferProgressBar.setVisible(true);
                        transferProgressBar.setProgress(0);
                        transferStatusLabel.setText("Downloading: " + filename);
                        downloadButton.setDisabled(true);
                    });
                    
                    // TODO: Implement actual file download with progress callback
                    // fileClient.downloadFile(filename, saveFile, progress -> {
                    //     Platform.runLater(() -> transferProgressBar.setProgress(progress));
                    // });
                    
                    // Simulate download progress
                    for (int i = 0; i <= 100; i += 10) {
                        Thread.sleep(100);
                        final int progress = i;
                        Platform.runLater(() -> transferProgressBar.setProgress(progress / 100.0));
                    }
                    
                    Platform.runLater(() -> {
                        transferProgressBar.setVisible(false);
                        transferStatusLabel.setText("Download completed");
                        downloadButton.setDisabled(false);
                        addTransferLog("✅ Download completed: " + filename);
                        loadLocalFiles();
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        transferProgressBar.setVisible(false);
                        transferStatusLabel.setText("Download failed");
                        downloadButton.setDisabled(false);
                        addTransferLog("❌ Download failed: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Refresh server files list
     */
    @FXML
    private void refreshServerFiles() {
        if (!isConnected) {
            addTransferLog("Error: Not connected to server");
            return;
        }
        
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                try {
                    // TODO: Implement actual server file listing
                    // List<String> files = fileClient.getServerFiles();
                    
                    // Sample server files for demo
                    java.util.List<String> files = java.util.Arrays.asList(
                        "document1.pdf",
                        "image.png", 
                        "data.csv",
                        "presentation.pptx"
                    );
                    
                    Platform.runLater(() -> {
                        serverFiles.setAll(files);
                        addTransferLog("📄 Server files refreshed (" + files.size() + " files)");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addTransferLog("❌ Error refreshing server files: " + e.getMessage());
                    });
                }
            });
        }
    }
    
    /**
     * Load local files list
     */
    private void loadLocalFiles() {
        // TODO: Load actual local files from a designated directory
        java.util.List<String> files = java.util.Arrays.asList(
            "local_file1.txt",
            "local_image.jpg",
            "local_data.xml"
        );
        
        localFiles.setAll(files);
    }
    
    /**
     * Add message to transfer log (thread-safe)
     */
    private void addTransferLog(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            transferLogArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }
    
    /**
     * Update connection status
     */
    private void setConnected(boolean connected) {
        this.isConnected = connected;
        updateConnectionStatus(connected);
    }
    
    /**
     * Update UI connection status
     */
    private void updateConnectionStatus(boolean connected) {
        Platform.runLater(() -> {
            if (connected) {
                connectionStatusLabel.setText("🟢 Connected");
                connectionStatusLabel.getStyleClass().removeAll("disconnected");
                connectionStatusLabel.getStyleClass().add("connected");
                
                connectButton.setText("Disconnect");
                connectButton.setOnAction(e -> disconnectFromServer());
                
                uploadButton.setDisabled(false);
                downloadButton.setDisabled(false);
                refreshButton.setDisabled(false);
                
            } else {
                connectionStatusLabel.setText("🔴 Disconnected");
                connectionStatusLabel.getStyleClass().removeAll("connected");
                connectionStatusLabel.getStyleClass().add("disconnected");
                
                connectButton.setText("Connect");
                connectButton.setOnAction(e -> connectToFileServer());
                connectButton.setDisabled(false);
                
                uploadButton.setDisabled(true);
                downloadButton.setDisabled(true);
                refreshButton.setDisabled(true);
            }
        });
    }
    
    /**
     * Disconnect from file server
     */
    private void disconnectFromServer() {
        try {
            // TODO: Implement actual FileClient disconnection
            // fileClient.disconnect();
            
            setConnected(false);
            serverFiles.clear();
            addTransferLog("🔌 Disconnected from file server");
            
            if (mainController != null) {
                mainController.showNotification("Disconnected from file server");
            }
            
        } catch (Exception e) {
            addTransferLog("Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Set main controller reference
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    /**
     * Shutdown file controller
     */
    public void shutdown() {
        System.out.println("🛑 Shutting down FileController...");
        
        if (isConnected) {
            disconnectFromServer();
        }
        
        // TODO: Cleanup FileClient resources
        // if (fileClient != null) {
        //     fileClient.cleanup();
        // }
        
        System.out.println("✅ FileController shutdown complete");
    }
}
