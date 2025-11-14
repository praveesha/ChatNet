package client.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import client.chat_client.ChatClient;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Chat Controller for TeamSync
 * 
 * Manages the chat interface and integrates with ChatClient backend
 */
public class ChatController implements Initializable {
    
    @FXML
    private TextArea chatLogArea;
    
    @FXML
    private TextField messageField;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button connectButton;
    
    @FXML
    private Button disconnectButton;
    
    @FXML
    private TextField serverAddressField;
    
    @FXML
    private TextField serverPortField;
    
    @FXML
    private Label connectionStatusLabel;
    
    @FXML
    private ListView<String> onlineUsersListView;
    
    // Backend integration
    private ChatClient chatClient;
    private MainController mainController;
    private boolean isConnected = false;
    
    // UI data
    private ObservableList<String> chatMessages;
    private ObservableList<String> onlineUsers;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🗣️ ChatController initializing...");
        
        // Initialize UI data
        chatMessages = FXCollections.observableArrayList();
        onlineUsers = FXCollections.observableArrayList();
        
        // Setup UI components
        setupUI();
        
        // Setup event handlers
        setupEventHandlers();
        
        System.out.println("✅ ChatController initialized");
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Configure chat log area
        chatLogArea.setEditable(false);
        chatLogArea.setWrapText(true);
        chatLogArea.getStyleClass().add("chat-log");
        
        // Configure message field
        messageField.setPromptText("Type your message here...");
        
        // Configure server fields with defaults
        serverAddressField.setText("localhost");
        serverPortField.setText("8080");
        
        // Setup online users list
        onlineUsersListView.setItems(onlineUsers);
        onlineUsersListView.setPlaceholder(new Label("No users online"));
        
        // Initial connection status
        updateConnectionStatus(false);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Send button action
        sendButton.setOnAction(e -> sendMessage());
        
        // Enter key in message field
        messageField.setOnAction(e -> sendMessage());
        
        // Connect button
        connectButton.setOnAction(e -> connectToServer());
        
        // Disconnect button  
        disconnectButton.setOnAction(e -> disconnectFromServer());
        
        // Auto-scroll chat log
        chatLogArea.textProperty().addListener((obs, oldText, newText) -> {
            chatLogArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    /**
     * Initialize backend ChatClient connection
     */
    public void initializeBackend() {
        System.out.println("🔗 Initializing ChatClient backend...");
        
        try {
            // TODO: Initialize ChatClient with proper configuration
            // chatClient = new ChatClient();
            
            addChatMessage("System", "Chat module initialized. Click Connect to join server.");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing ChatClient: " + e.getMessage());
            addChatMessage("System", "Error: Could not initialize chat module - " + e.getMessage());
        }
    }
    
    /**
     * Connect to chat server
     */
    @FXML
    private void connectToServer() {
        String address = serverAddressField.getText().trim();
        String portText = serverPortField.getText().trim();
        
        if (address.isEmpty() || portText.isEmpty()) {
            addChatMessage("System", "Error: Please enter server address and port");
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            // Disable connect button during connection attempt
            connectButton.setDisabled(true);
            connectButton.setText("Connecting...");
            
            // Connect in background thread
            if (mainController != null) {
                mainController.getBackgroundExecutor().submit(() -> {
                    try {
                        // TODO: Implement actual ChatClient connection
                        // chatClient.connect(address, port);
                        
                        Platform.runLater(() -> {
                            setConnected(true);
                            addChatMessage("System", "✅ Connected to server: " + address + ":" + port);
                            mainController.showNotification("Connected to chat server");
                        });
                        
                        // Start message listening thread
                        startMessageListener();
                        
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            addChatMessage("System", "❌ Connection failed: " + e.getMessage());
                            setConnected(false);
                        });
                    }
                });
            }
            
        } catch (NumberFormatException e) {
            addChatMessage("System", "Error: Invalid port number");
            connectButton.setDisabled(false);
            connectButton.setText("Connect");
        }
    }
    
    /**
     * Disconnect from chat server
     */
    @FXML  
    private void disconnectFromServer() {
        if (!isConnected) return;
        
        try {
            // TODO: Implement actual ChatClient disconnection
            // chatClient.disconnect();
            
            setConnected(false);
            addChatMessage("System", "🔌 Disconnected from server");
            onlineUsers.clear();
            
            if (mainController != null) {
                mainController.showNotification("Disconnected from chat server");
            }
            
        } catch (Exception e) {
            addChatMessage("System", "Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Send message to chat server
     */
    @FXML
    private void sendMessage() {
        if (!isConnected) {
            addChatMessage("System", "Error: Not connected to server");
            return;
        }
        
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;
        
        try {
            // TODO: Implement actual message sending
            // chatClient.sendMessage(message);
            
            // For now, just add to local chat log
            addChatMessage("You", message);
            messageField.clear();
            
        } catch (Exception e) {
            addChatMessage("System", "Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Add message to chat log (thread-safe)
     */
    public void addChatMessage(String sender, String message) {
        Platform.runLater(() -> {
            String timestamp = LocalTime.now().format(timeFormatter);
            String formattedMessage = String.format("[%s] %s: %s\n", timestamp, sender, message);
            chatLogArea.appendText(formattedMessage);
        });
    }
    
    /**
     * Update online users list (thread-safe)
     */
    public void updateOnlineUsers(java.util.List<String> users) {
        Platform.runLater(() -> {
            onlineUsers.setAll(users);
        });
    }
    
    /**
     * Start background message listener thread
     */
    private void startMessageListener() {
        if (mainController != null) {
            mainController.getBackgroundExecutor().submit(() -> {
                while (isConnected) {
                    try {
                        // TODO: Implement actual message listening
                        // String incomingMessage = chatClient.receiveMessage();
                        // if (incomingMessage != null) {
                        //     Platform.runLater(() -> addChatMessage("Server", incomingMessage));
                        // }
                        
                        Thread.sleep(100); // Prevent busy waiting
                        
                    } catch (Exception e) {
                        if (isConnected) {
                            Platform.runLater(() -> {
                                addChatMessage("System", "Connection error: " + e.getMessage());
                                setConnected(false);
                            });
                        }
                        break;
                    }
                }
            });
        }
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
                connectionStatusLabel.getStyleClass().removeAll("disconnected", "connecting");
                connectionStatusLabel.getStyleClass().add("connected");
                
                connectButton.setDisabled(true);
                connectButton.setText("Connect");
                disconnectButton.setDisabled(false);
                sendButton.setDisabled(false);
                messageField.setDisabled(false);
                
            } else {
                connectionStatusLabel.setText("🔴 Disconnected");
                connectionStatusLabel.getStyleClass().removeAll("connected", "connecting");
                connectionStatusLabel.getStyleClass().add("disconnected");
                
                connectButton.setDisabled(false);
                connectButton.setText("Connect");
                disconnectButton.setDisabled(true);
                sendButton.setDisabled(true);
                messageField.setDisabled(true);
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
     * Shutdown chat controller
     */
    /**
     * Cleanup and shutdown
     */
    public void shutdown() {
        System.out.println("🛑 Shutting down ChatController...");
        
        if (isConnected) {
            disconnectFromServer();
        }
        
        // TODO: Cleanup ChatClient resources
        // if (chatClient != null) {
        //     chatClient.cleanup();
        // }
        
        System.out.println("✅ ChatController shutdown complete");
    }
}
