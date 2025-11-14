package client.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * JavaFX Main Application without FXML
 */
public class MainNoFXML extends Application {
    
    private MainController controller;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Create controller
            controller = new MainController();
            
            // Create UI programmatically
            TabPane mainTabPane = new TabPane();
            mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            
            // Create tabs
            Tab chatTab = createChatTab();
            Tab fileTab = createFileTab();
            Tab udpTab = createUdpTab();  
            Tab nioTab = createNioTab();
            Tab statsTab = createStatsTab();
            
            mainTabPane.getTabs().addAll(chatTab, fileTab, udpTab, nioTab, statsTab);
            
            // Inject UI components into controller manually
            injectUIComponents(controller, mainTabPane, fileTab, udpTab, nioTab, statsTab);
            
            // Initialize controller
            controller.initialize(null, null);
            controller.initializeBackendConnections();
            
            // Create scene
            Scene scene = new Scene(mainTabPane, 1000, 700);
            
            // Configure stage
            primaryStage.setTitle("TeamSync - Integrated Network Client");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Handle application shutdown
            primaryStage.setOnCloseRequest(e -> {
                controller.shutdown();
                System.exit(0);
            });
            
            primaryStage.show();
            
            System.out.println("🚀 TeamSync JavaFX Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error starting TeamSync application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Tab createChatTab() {
        Tab tab = new Tab("💬 Chat");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().add(new Label("Chat functionality - existing implementation"));
        tab.setContent(content);
        return tab;
    }
    
    private Tab createFileTab() {
        Tab tab = new Tab("📁 Files");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // Connection Panel
        VBox connectionPanel = new VBox(10);
        connectionPanel.getChildren().add(new Label("File Server Connection:"));
        
        HBox connectionRow = new HBox(10);
        connectionRow.setAlignment(Pos.CENTER_LEFT);
        TextField fileServerHost = new TextField("localhost");
        fileServerHost.setPrefWidth(120);
        TextField fileServerPort = new TextField("12346");
        fileServerPort.setPrefWidth(80);
        Button connectButton = new Button("Connect");
        Button disconnectButton = new Button("Disconnect");
        
        connectionRow.getChildren().addAll(
            new Label("Host:"), fileServerHost,
            new Label("Port:"), fileServerPort,
            connectButton, disconnectButton
        );
        
        Label fileStatusLabel = new Label("Not connected");
        ProgressBar fileProgressBar = new ProgressBar(0);
        fileProgressBar.setPrefWidth(300);
        
        connectionPanel.getChildren().addAll(connectionRow, fileStatusLabel, fileProgressBar);
        
        // File Operations
        HBox fileOpsPanel = new HBox(15);
        
        // Server Files
        VBox serverPanel = new VBox(10);
        serverPanel.getChildren().add(new Label("Server Files:"));
        ListView<String> serverFilesList = new ListView<>();
        serverFilesList.setPrefHeight(200);
        HBox serverButtons = new HBox(10);
        Button refreshButton = new Button("🔄 Refresh");
        Button downloadButton = new Button("📥 Download");
        serverButtons.getChildren().addAll(refreshButton, downloadButton);
        serverPanel.getChildren().addAll(serverFilesList, serverButtons);
        
        // Local Files  
        VBox localPanel = new VBox(10);
        localPanel.getChildren().add(new Label("Local Files:"));
        ListView<String> localFilesList = new ListView<>();
        localFilesList.setPrefHeight(200);
        Button uploadButton = new Button("📤 Upload File");
        localPanel.getChildren().addAll(localFilesList, uploadButton);
        
        fileOpsPanel.getChildren().addAll(serverPanel, localPanel);
        HBox.setHgrow(serverPanel, Priority.ALWAYS);
        HBox.setHgrow(localPanel, Priority.ALWAYS);
        
        // Log Area
        VBox logPanel = new VBox(10);
        logPanel.getChildren().add(new Label("Transfer Log:"));
        TextArea fileLogArea = new TextArea();
        fileLogArea.setPrefHeight(100);
        fileLogArea.setEditable(false);
        logPanel.getChildren().add(fileLogArea);
        
        content.getChildren().addAll(
            connectionPanel, 
            new Separator(),
            fileOpsPanel,
            logPanel
        );
        
        // Store references for controller injection
        tab.setUserData(new FileTabComponents(
            fileServerHost, fileServerPort, connectButton, disconnectButton,
            uploadButton, downloadButton, refreshButton, serverFilesList, 
            localFilesList, fileLogArea, fileProgressBar, fileStatusLabel
        ));
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createUdpTab() {
        Tab tab = new Tab("🌐 UDP");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // UDP Configuration
        VBox udpConfig = new VBox(10);
        udpConfig.getChildren().add(new Label("UDP Configuration:"));
        
        HBox udpRow = new HBox(10);
        udpRow.setAlignment(Pos.CENTER_LEFT);
        TextField udpListenPort = new TextField("8888");
        udpListenPort.setPrefWidth(80);
        Button startUdpButton = new Button("🔊 Start Listening");
        Button stopUdpButton = new Button("🔇 Stop Listening");
        
        udpRow.getChildren().addAll(
            new Label("Listen Port:"), udpListenPort,
            startUdpButton, stopUdpButton
        );
        
        Label udpStatusLabel = new Label("Not listening");
        udpConfig.getChildren().addAll(udpRow, udpStatusLabel);
        
        // Peer Discovery
        VBox discoveryPanel = new VBox(10);
        discoveryPanel.getChildren().add(new Label("Peer Discovery:"));
        
        HBox discoveryRow = new HBox(10);
        discoveryRow.setAlignment(Pos.CENTER_LEFT);
        Button startDiscoveryButton = new Button("🔍 Start Discovery");
        Button stopDiscoveryButton = new Button("🛑 Stop Discovery");
        Label peersCountLabel = new Label("Peers: 0");
        
        discoveryRow.getChildren().addAll(startDiscoveryButton, stopDiscoveryButton, peersCountLabel);
        
        HBox peersPanel = new HBox(15);
        
        // Peers List
        VBox peersListPanel = new VBox(10);
        peersListPanel.getChildren().add(new Label("Discovered Peers:"));
        ListView<String> peersListView = new ListView<>();
        peersListView.setPrefHeight(150);
        peersListPanel.getChildren().add(peersListView);
        
        // UDP Log
        VBox logPanel = new VBox(10);
        logPanel.getChildren().add(new Label("UDP Log:"));
        TextArea udpLogArea = new TextArea();
        udpLogArea.setPrefHeight(150);
        udpLogArea.setEditable(false);
        logPanel.getChildren().add(udpLogArea);
        
        peersPanel.getChildren().addAll(peersListPanel, logPanel);
        HBox.setHgrow(peersListPanel, Priority.ALWAYS);
        HBox.setHgrow(logPanel, Priority.ALWAYS);
        
        discoveryPanel.getChildren().addAll(discoveryRow, peersPanel);
        
        content.getChildren().addAll(
            udpConfig,
            new Separator(),
            discoveryPanel
        );
        
        // Store references for controller injection
        tab.setUserData(new UdpTabComponents(
            udpListenPort, startUdpButton, stopUdpButton, startDiscoveryButton,
            stopDiscoveryButton, peersListView, udpLogArea, udpStatusLabel, peersCountLabel
        ));
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createNioTab() {
        Tab tab = new Tab("⚡ NIO");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // NIO Connection
        VBox connectionPanel = new VBox(10);
        connectionPanel.getChildren().add(new Label("NIO Server Connection:"));
        
        HBox connectionRow = new HBox(10);
        connectionRow.setAlignment(Pos.CENTER_LEFT);
        TextField nioServerHost = new TextField("localhost");
        nioServerHost.setPrefWidth(120);
        TextField nioServerPort = new TextField("9090");
        nioServerPort.setPrefWidth(80);
        Button connectButton = new Button("Connect");
        Button disconnectButton = new Button("Disconnect");
        
        connectionRow.getChildren().addAll(
            new Label("Host:"), nioServerHost,
            new Label("Port:"), nioServerPort,
            connectButton, disconnectButton
        );
        
        Label nioStatusLabel = new Label("Not connected");
        connectionPanel.getChildren().addAll(connectionRow, nioStatusLabel);
        
        // Echo Testing
        VBox echoPanel = new VBox(10);
        echoPanel.getChildren().add(new Label("Echo Testing:"));
        
        HBox echoRow = new HBox(10);
        echoRow.setAlignment(Pos.CENTER_LEFT);
        TextField echoMessageField = new TextField("Hello NIO Server!");
        Button sendEchoButton = new Button("📤 Send Echo");
        Button measureLatencyButton = new Button("⏱ Measure Latency");
        
        echoRow.getChildren().addAll(
            new Label("Message:"), echoMessageField,
            sendEchoButton, measureLatencyButton
        );
        HBox.setHgrow(echoMessageField, Priority.ALWAYS);
        
        Label latencyLabel = new Label("Latency: --");
        echoPanel.getChildren().addAll(echoRow, latencyLabel);
        
        // NIO Log
        VBox logPanel = new VBox(10);
        logPanel.getChildren().add(new Label("NIO Log:"));
        TextArea nioLogArea = new TextArea();
        nioLogArea.setPrefHeight(200);
        nioLogArea.setEditable(false);
        logPanel.getChildren().add(nioLogArea);
        
        content.getChildren().addAll(
            connectionPanel,
            new Separator(),
            echoPanel,
            logPanel
        );
        
        // Store references for controller injection
        tab.setUserData(new NioTabComponents(
            nioServerHost, nioServerPort, connectButton, disconnectButton,
            echoMessageField, sendEchoButton, measureLatencyButton, 
            nioLogArea, nioStatusLabel, latencyLabel
        ));
        
        tab.setContent(content);
        return tab;
    }
    
    private Tab createStatsTab() {
        Tab tab = new Tab("📊 Stats");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // Controls
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        Button refreshStatsButton = new Button("🔄 Refresh");
        Button resetStatsButton = new Button("🗑 Reset");
        controls.getChildren().addAll(refreshStatsButton, resetStatsButton);
        
        // Statistics Display
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        
        Label messageCountLabel = new Label("Messages: 0");
        Label connectionUptimeLabel = new Label("Uptime: 0:00");
        Label bytesSentLabel = new Label("Bytes Sent: 0");
        Label bytesReceivedLabel = new Label("Bytes Received: 0");
        Label fileTransfersLabel = new Label("File Transfers: 0");
        Label discoveredPeersLabel = new Label("Discovered Peers: 0");
        Label nioLatencyLabel = new Label("NIO Messages: 0");
        
        statsGrid.add(messageCountLabel, 0, 0);
        statsGrid.add(connectionUptimeLabel, 1, 0);
        statsGrid.add(bytesSentLabel, 2, 0);
        statsGrid.add(bytesReceivedLabel, 0, 1);
        statsGrid.add(fileTransfersLabel, 1, 1);
        statsGrid.add(discoveredPeersLabel, 2, 1);
        statsGrid.add(nioLatencyLabel, 0, 2);
        
        // Statistics Log
        VBox logPanel = new VBox(10);
        logPanel.getChildren().add(new Label("Statistics Log:"));
        TextArea statsLogArea = new TextArea();
        statsLogArea.setPrefHeight(200);
        statsLogArea.setEditable(false);
        logPanel.getChildren().add(statsLogArea);
        
        content.getChildren().addAll(
            controls,
            new Separator(),
            statsGrid,
            logPanel
        );
        
        // Store references for controller injection
        tab.setUserData(new StatsTabComponents(
            refreshStatsButton, resetStatsButton, messageCountLabel,
            connectionUptimeLabel, bytesSentLabel, bytesReceivedLabel,
            fileTransfersLabel, discoveredPeersLabel, nioLatencyLabel, statsLogArea
        ));
        
        tab.setContent(content);
        return tab;
    }
    
    private void injectUIComponents(MainController controller, TabPane mainTabPane, 
                                  Tab fileTab, Tab udpTab, Tab nioTab, Tab statsTab) {
        // Use reflection to inject components
        try {
            java.lang.reflect.Field field;
            
            // Main components
            field = MainController.class.getDeclaredField("mainTabPane");
            field.setAccessible(true);
            field.set(controller, mainTabPane);
            
            field = MainController.class.getDeclaredField("fileTab");
            field.setAccessible(true);
            field.set(controller, fileTab);
            
            field = MainController.class.getDeclaredField("statsTab");
            field.setAccessible(true);
            field.set(controller, statsTab);
            
            // File tab components
            FileTabComponents fileComponents = (FileTabComponents) fileTab.getUserData();
            injectFileComponents(controller, fileComponents);
            
            // UDP tab components  
            UdpTabComponents udpComponents = (UdpTabComponents) udpTab.getUserData();
            injectUdpComponents(controller, udpComponents);
            
            // NIO tab components
            NioTabComponents nioComponents = (NioTabComponents) nioTab.getUserData();
            injectNioComponents(controller, nioComponents);
            
            // Stats tab components
            StatsTabComponents statsComponents = (StatsTabComponents) statsTab.getUserData();
            injectStatsComponents(controller, statsComponents);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to inject UI components: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void injectFileComponents(MainController controller, FileTabComponents components) throws Exception {
        injectField(controller, "fileServerHost", components.fileServerHost);
        injectField(controller, "fileServerPort", components.fileServerPort);
        injectField(controller, "connectFileButton", components.connectButton);
        injectField(controller, "disconnectFileButton", components.disconnectButton);
        injectField(controller, "uploadFileButton", components.uploadButton);
        injectField(controller, "downloadFileButton", components.downloadButton);
        injectField(controller, "refreshFilesButton", components.refreshButton);
        injectField(controller, "serverFilesList", components.serverFilesList);
        injectField(controller, "localFilesList", components.localFilesList);
        injectField(controller, "fileLogArea", components.fileLogArea);
        injectField(controller, "fileProgressBar", components.fileProgressBar);
        injectField(controller, "fileStatusLabel", components.fileStatusLabel);
        
        // Set up event handlers
        components.connectButton.setOnAction(e -> controller.connectToFileServer());
        components.disconnectButton.setOnAction(e -> controller.disconnectFromFileServer());
        components.uploadButton.setOnAction(e -> controller.uploadFile());
        components.downloadButton.setOnAction(e -> controller.downloadFile());
        components.refreshButton.setOnAction(e -> controller.refreshServerFiles());
    }
    
    private void injectUdpComponents(MainController controller, UdpTabComponents components) throws Exception {
        injectField(controller, "udpListenPort", components.udpListenPort);
        injectField(controller, "startUdpButton", components.startUdpButton);
        injectField(controller, "stopUdpButton", components.stopUdpButton);
        injectField(controller, "startDiscoveryButton", components.startDiscoveryButton);
        injectField(controller, "stopDiscoveryButton", components.stopDiscoveryButton);
        injectField(controller, "peersListView", components.peersListView);
        injectField(controller, "udpLogArea", components.udpLogArea);
        injectField(controller, "udpStatusLabel", components.udpStatusLabel);
        injectField(controller, "peersCountLabel", components.peersCountLabel);
        
        // Set up event handlers  
        components.startUdpButton.setOnAction(e -> controller.startUdpListening());
        components.stopUdpButton.setOnAction(e -> controller.stopUdpListening());
        components.startDiscoveryButton.setOnAction(e -> controller.startPeerDiscovery());
        components.stopDiscoveryButton.setOnAction(e -> controller.stopPeerDiscovery());
    }
    
    private void injectNioComponents(MainController controller, NioTabComponents components) throws Exception {
        injectField(controller, "nioServerHost", components.nioServerHost);
        injectField(controller, "nioServerPort", components.nioServerPort);
        injectField(controller, "connectNioButton", components.connectButton);
        injectField(controller, "disconnectNioButton", components.disconnectButton);
        injectField(controller, "echoMessageField", components.echoMessageField);
        injectField(controller, "sendEchoButton", components.sendEchoButton);
        injectField(controller, "measureLatencyButton", components.measureLatencyButton);
        injectField(controller, "nioLogArea", components.nioLogArea);
        injectField(controller, "nioStatusLabel", components.nioStatusLabel);
        injectField(controller, "latencyLabel", components.latencyLabel);
        
        // Set up event handlers
        components.connectButton.setOnAction(e -> controller.connectToNioServer());
        components.disconnectButton.setOnAction(e -> controller.disconnectFromNioServer());
        components.sendEchoButton.setOnAction(e -> controller.sendEchoMessage());
        components.measureLatencyButton.setOnAction(e -> controller.measureLatency());
    }
    
    private void injectStatsComponents(MainController controller, StatsTabComponents components) throws Exception {
        injectField(controller, "refreshStatsButton", components.refreshStatsButton);
        injectField(controller, "resetStatsButton", components.resetStatsButton);
        injectField(controller, "messageCountLabel", components.messageCountLabel);
        injectField(controller, "connectionUptimeLabel", components.connectionUptimeLabel);
        injectField(controller, "bytesSentLabel", components.bytesSentLabel);
        injectField(controller, "bytesReceivedLabel", components.bytesReceivedLabel);
        injectField(controller, "fileTransfersLabel", components.fileTransfersLabel);
        injectField(controller, "discoveredPeersLabel", components.discoveredPeersLabel);
        injectField(controller, "nioLatencyLabel", components.nioLatencyLabel);
        injectField(controller, "statsLogArea", components.statsLogArea);
        
        // Set up event handlers
        components.refreshStatsButton.setOnAction(e -> controller.refreshStatistics());
        components.resetStatsButton.setOnAction(e -> controller.resetStatistics());
    }
    
    private void injectField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    // Helper classes to store component references
    private static class FileTabComponents {
        final TextField fileServerHost, fileServerPort;
        final Button connectButton, disconnectButton, uploadButton, downloadButton, refreshButton;
        final ListView<String> serverFilesList, localFilesList;
        final TextArea fileLogArea;
        final ProgressBar fileProgressBar;
        final Label fileStatusLabel;
        
        FileTabComponents(TextField fileServerHost, TextField fileServerPort,
                         Button connectButton, Button disconnectButton, Button uploadButton,
                         Button downloadButton, Button refreshButton, ListView<String> serverFilesList,
                         ListView<String> localFilesList, TextArea fileLogArea,
                         ProgressBar fileProgressBar, Label fileStatusLabel) {
            this.fileServerHost = fileServerHost;
            this.fileServerPort = fileServerPort;
            this.connectButton = connectButton;
            this.disconnectButton = disconnectButton;
            this.uploadButton = uploadButton;
            this.downloadButton = downloadButton;
            this.refreshButton = refreshButton;
            this.serverFilesList = serverFilesList;
            this.localFilesList = localFilesList;
            this.fileLogArea = fileLogArea;
            this.fileProgressBar = fileProgressBar;
            this.fileStatusLabel = fileStatusLabel;
        }
    }
    
    private static class UdpTabComponents {
        final TextField udpListenPort;
        final Button startUdpButton, stopUdpButton, startDiscoveryButton, stopDiscoveryButton;
        final ListView<String> peersListView;
        final TextArea udpLogArea;
        final Label udpStatusLabel, peersCountLabel;
        
        UdpTabComponents(TextField udpListenPort, Button startUdpButton, Button stopUdpButton,
                        Button startDiscoveryButton, Button stopDiscoveryButton,
                        ListView<String> peersListView, TextArea udpLogArea,
                        Label udpStatusLabel, Label peersCountLabel) {
            this.udpListenPort = udpListenPort;
            this.startUdpButton = startUdpButton;
            this.stopUdpButton = stopUdpButton;
            this.startDiscoveryButton = startDiscoveryButton;
            this.stopDiscoveryButton = stopDiscoveryButton;
            this.peersListView = peersListView;
            this.udpLogArea = udpLogArea;
            this.udpStatusLabel = udpStatusLabel;
            this.peersCountLabel = peersCountLabel;
        }
    }
    
    private static class NioTabComponents {
        final TextField nioServerHost, nioServerPort, echoMessageField;
        final Button connectButton, disconnectButton, sendEchoButton, measureLatencyButton;
        final TextArea nioLogArea;
        final Label nioStatusLabel, latencyLabel;
        
        NioTabComponents(TextField nioServerHost, TextField nioServerPort, Button connectButton,
                        Button disconnectButton, TextField echoMessageField, Button sendEchoButton,
                        Button measureLatencyButton, TextArea nioLogArea, Label nioStatusLabel,
                        Label latencyLabel) {
            this.nioServerHost = nioServerHost;
            this.nioServerPort = nioServerPort;
            this.connectButton = connectButton;
            this.disconnectButton = disconnectButton;
            this.echoMessageField = echoMessageField;
            this.sendEchoButton = sendEchoButton;
            this.measureLatencyButton = measureLatencyButton;
            this.nioLogArea = nioLogArea;
            this.nioStatusLabel = nioStatusLabel;
            this.latencyLabel = latencyLabel;
        }
    }
    
    private static class StatsTabComponents {
        final Button refreshStatsButton, resetStatsButton;
        final Label messageCountLabel, connectionUptimeLabel, bytesSentLabel,
                   bytesReceivedLabel, fileTransfersLabel, discoveredPeersLabel, nioLatencyLabel;
        final TextArea statsLogArea;
        
        StatsTabComponents(Button refreshStatsButton, Button resetStatsButton,
                          Label messageCountLabel, Label connectionUptimeLabel,
                          Label bytesSentLabel, Label bytesReceivedLabel,
                          Label fileTransfersLabel, Label discoveredPeersLabel,
                          Label nioLatencyLabel, TextArea statsLogArea) {
            this.refreshStatsButton = refreshStatsButton;
            this.resetStatsButton = resetStatsButton;
            this.messageCountLabel = messageCountLabel;
            this.connectionUptimeLabel = connectionUptimeLabel;
            this.bytesSentLabel = bytesSentLabel;
            this.bytesReceivedLabel = bytesReceivedLabel;
            this.fileTransfersLabel = fileTransfersLabel;
            this.discoveredPeersLabel = discoveredPeersLabel;
            this.nioLatencyLabel = nioLatencyLabel;
            this.statsLogArea = statsLogArea;
        }
    }
    
    /**
     * Main method - entry point for the application
     */
    public static void main(String[] args) {
        System.out.println("🔧 Initializing TeamSync JavaFX Application (No FXML)...");
        launch(args);
    }
}
