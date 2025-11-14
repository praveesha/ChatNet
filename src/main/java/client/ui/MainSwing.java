package client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TeamSync Swing Application (Alternative to JavaFX)
 * 
 * This is a Swing-based version that can run immediately without JavaFX dependencies.
 * Demonstrates the same functionality as the JavaFX version.
 */
public class MainSwing extends JFrame {
    
    private JTabbedPane mainTabPane;
    private ExecutorService backgroundExecutor;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Chat components
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField serverAddressField;
    private JTextField serverPortField;
    private JLabel connectionStatusLabel;
    private DefaultListModel<String> onlineUsersModel;
    private JList<String> onlineUsersList;
    
    // File transfer components  
    private DefaultListModel<String> serverFilesModel;
    private DefaultListModel<String> localFilesModel;
    private JList<String> serverFilesList;
    private JList<String> localFilesList;
    private JProgressBar transferProgressBar;
    private JLabel transferStatusLabel;
    private JTextArea transferLogArea;
    
    // Status components
    private JTable peersTable;
    private DefaultTableModel peersTableModel;
    private JLabel totalPeersLabel;
    private JLabel activePeersLabel;
    private JLabel lastHeartbeatLabel;
    private JTextArea discoveryLogArea;
    
    // Stats components
    private JLabel messagesSentLabel;
    private JLabel messagesReceivedLabel;
    private JLabel packetsReceivedLabel;
    private JLabel transferRateLabel;
    private JLabel avgLatencyLabel;
    private JTextArea statsLogArea;
    
    // Statistics counters
    private int messagesSent = 0;
    private int messagesReceived = 0;
    private int packetsReceived = 0;
    private long totalTransferred = 0;
    
    public MainSwing() {
        initializeApplication();
        createUI();
        setupEventHandlers();
        startBackgroundTasks();
    }
    
    private void initializeApplication() {
        backgroundExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("TeamSync-Background-Thread");
            return t;
        });
        
        // Initialize data models
        onlineUsersModel = new DefaultListModel<>();
        serverFilesModel = new DefaultListModel<>();
        localFilesModel = new DefaultListModel<>();
        
        peersTableModel = new DefaultTableModel(new String[]{"Peer Name", "IP Address", "Status", "Last Seen"}, 0);
    }
    
    private void createUI() {
        setTitle("TeamSync - Integrated Network Client");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main tab pane with larger fonts
        mainTabPane = new JTabbedPane();
        mainTabPane.setFont(new Font("SansSerif", Font.BOLD, 15));
        mainTabPane.setTabPlacement(JTabbedPane.TOP);
        
        // Add tabs with proper sizing
        mainTabPane.addTab("💬 Chat", createChatTab());
        mainTabPane.addTab("📁 Files", createFileTab());
        mainTabPane.addTab("🌐 Status", createStatusTab());
        mainTabPane.addTab("📊 Stats", createStatsTab());
        
        add(mainTabPane, BorderLayout.CENTER);
        
        // Create status bar with better formatting
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(new EmptyBorder(8, 15, 8, 15));
        statusBar.setPreferredSize(new Dimension(0, 35));
        
        JLabel versionLabel = new JLabel("TeamSync v1.0 - Ready");
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JLabel timeLabel = new JLabel(LocalTime.now().format(timeFormatter));
        timeLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        statusBar.add(versionLabel, BorderLayout.WEST);
        statusBar.add(timeLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);
        
        // Configure window
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
    }
    
    private JPanel createChatTab() {
        JPanel chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Main chat area
        JPanel mainChatPanel = new JPanel(new BorderLayout(5, 5));
        
        // Chat messages
        JLabel chatLabel = new JLabel("Chat Messages:");
        chatLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chatArea.setBackground(new Color(248, 248, 248));
        chatArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(500, 400));
        mainChatPanel.add(chatLabel, BorderLayout.NORTH);
        mainChatPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        // Message input
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        messageField = new JTextField();
        messageField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        messageField.setPreferredSize(new Dimension(0, 30));
        sendButton = new JButton("Send");
        sendButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        sendButton.setPreferredSize(new Dimension(80, 30));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainChatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        chatPanel.add(mainChatPanel, BorderLayout.CENTER);
        
        // Side panel
        JPanel sidePanel = new JPanel(new BorderLayout(5, 10));
        sidePanel.setPreferredSize(new Dimension(200, 0));
        
        // Connection controls
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Configure server fields with larger sizes
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel serverLabel = new JLabel("Server:");
        serverLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        connectionPanel.add(serverLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        serverAddressField = new JTextField("localhost", 15);
        serverAddressField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        serverAddressField.setPreferredSize(new Dimension(120, 28));
        connectionPanel.add(serverAddressField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        connectionPanel.add(portLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        serverPortField = new JTextField("8080", 15);
        serverPortField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        serverPortField.setPreferredSize(new Dimension(120, 28));
        connectionPanel.add(serverPortField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        connectButton = new JButton("Connect");
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        connectButton.setPreferredSize(new Dimension(140, 32));
        connectionPanel.add(connectButton, gbc);
        
        gbc.gridy = 3;
        connectionStatusLabel = new JLabel("🔴 Disconnected");
        connectionStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        connectionStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        connectionPanel.add(connectionStatusLabel, gbc);
        
        sidePanel.add(connectionPanel, BorderLayout.NORTH);
        
        // Online users
        JPanel usersPanel = new JPanel(new BorderLayout(5, 8));
        usersPanel.setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        onlineUsersList = new JList<>(onlineUsersModel);
        onlineUsersList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        onlineUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineUsersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(3, 8, 3, 8));
                return this;
            }
        });
        
        JScrollPane usersScrollPane = new JScrollPane(onlineUsersList);
        usersScrollPane.setPreferredSize(new Dimension(200, 250));
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);
        sidePanel.add(usersPanel, BorderLayout.CENTER);
        
        chatPanel.add(sidePanel, BorderLayout.EAST);
        
        return chatPanel;
    }
    
    private JPanel createFileTab() {
        JPanel filePanel = new JPanel(new BorderLayout(10, 10));
        filePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.add(new JLabel("File Server:"));
        connectionPanel.add(new JTextField("localhost", 10));
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(new JTextField("8081", 5));
        connectionPanel.add(new JButton("Connect"));
        filePanel.add(connectionPanel, BorderLayout.NORTH);
        
        // Main file area
        JSplitPane fileSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Server files
        JPanel serverPanel = new JPanel(new BorderLayout(5, 5));
        serverPanel.add(new JLabel("Server Files:"), BorderLayout.NORTH);
        serverFilesList = new JList<>(serverFilesModel);
        serverPanel.add(new JScrollPane(serverFilesList), BorderLayout.CENTER);
        JPanel serverButtonPanel = new JPanel(new FlowLayout());
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.addActionListener(e -> refreshServerFiles());
        serverButtonPanel.add(refreshButton);
        
        JButton downloadButton = new JButton("⬇ Download");
        downloadButton.addActionListener(e -> downloadFile());
        serverButtonPanel.add(downloadButton);
        
        serverPanel.add(serverButtonPanel, BorderLayout.SOUTH);
        
        // Local files
        JPanel localPanel = new JPanel(new BorderLayout(5, 5));
        localPanel.add(new JLabel("Local Files:"), BorderLayout.NORTH);
        localFilesList = new JList<>(localFilesModel);
        localPanel.add(new JScrollPane(localFilesList), BorderLayout.CENTER);
        JPanel localButtonPanel = new JPanel(new FlowLayout());
        
        JButton uploadButton = new JButton("⬆ Upload");
        uploadButton.addActionListener(e -> uploadFile());
        localButtonPanel.add(uploadButton);
        
        localPanel.add(localButtonPanel, BorderLayout.SOUTH);
        
        fileSplitPane.setLeftComponent(serverPanel);
        fileSplitPane.setRightComponent(localPanel);
        fileSplitPane.setDividerLocation(400);
        
        filePanel.add(fileSplitPane, BorderLayout.CENTER);
        
        // Bottom progress panel
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        transferStatusLabel = new JLabel("Ready");
        transferProgressBar = new JProgressBar();
        transferLogArea = new JTextArea(4, 0);
        transferLogArea.setEditable(false);
        transferLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        
        progressPanel.add(transferStatusLabel, BorderLayout.NORTH);
        progressPanel.add(transferProgressBar, BorderLayout.CENTER);
        progressPanel.add(new JScrollPane(transferLogArea), BorderLayout.SOUTH);
        
        filePanel.add(progressPanel, BorderLayout.SOUTH);
        
        return filePanel;
    }
    
    private JPanel createStatusTab() {
        JPanel statusPanel = new JPanel(new BorderLayout(10, 10));
        statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Peer Discovery:"));
        controlPanel.add(new JButton("▶ Start Discovery"));
        controlPanel.add(new JButton("⏸ Stop Discovery"));
        controlPanel.add(new JButton("🔄 Refresh"));
        statusPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Main split pane
        JSplitPane statusSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Peers table
        peersTable = new JTable(peersTableModel);
        JScrollPane tableScrollPane = new JScrollPane(peersTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 300));
        
        // Statistics panel
        JPanel statsPanel = new JPanel(new BorderLayout(5, 10));
        
        JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.add(new JLabel("Total Peers:"));
        totalPeersLabel = new JLabel("0");
        summaryPanel.add(totalPeersLabel);
        summaryPanel.add(new JLabel("Active:"));
        activePeersLabel = new JLabel("0");
        summaryPanel.add(activePeersLabel);
        summaryPanel.add(new JLabel("Last Beat:"));
        lastHeartbeatLabel = new JLabel("Never");
        summaryPanel.add(lastHeartbeatLabel);
        
        discoveryLogArea = new JTextArea(10, 0);
        discoveryLogArea.setEditable(false);
        discoveryLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        
        statsPanel.add(summaryPanel, BorderLayout.NORTH);
        statsPanel.add(new JLabel("Discovery Log:"), BorderLayout.CENTER);
        statsPanel.add(new JScrollPane(discoveryLogArea), BorderLayout.SOUTH);
        
        statusSplitPane.setLeftComponent(tableScrollPane);
        statusSplitPane.setRightComponent(statsPanel);
        statusSplitPane.setDividerLocation(500);
        
        statusPanel.add(statusSplitPane, BorderLayout.CENTER);
        
        return statusPanel;
    }
    
    private JPanel createStatsTab() {
        JPanel statsPanel = new JPanel(new BorderLayout(10, 10));
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("NIO Echo Test:"));
        controlPanel.add(new JTextField("localhost", 8));
        controlPanel.add(new JLabel(":"));
        controlPanel.add(new JTextField("9090", 5));
        controlPanel.add(new JButton("▶ Start Test"));
        controlPanel.add(new JButton("⏸ Stop Test"));
        controlPanel.add(new JButton("🔄 Reset"));
        statsPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Statistics grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 4, 10, 10));
        statsGrid.setBorder(BorderFactory.createTitledBorder("Statistics"));
        
        statsGrid.add(new JLabel("Messages Sent:"));
        messagesSentLabel = new JLabel("0");
        statsGrid.add(messagesSentLabel);
        
        statsGrid.add(new JLabel("Messages Received:"));
        messagesReceivedLabel = new JLabel("0");
        statsGrid.add(messagesReceivedLabel);
        
        statsGrid.add(new JLabel("UDP Packets:"));
        packetsReceivedLabel = new JLabel("0");
        statsGrid.add(packetsReceivedLabel);
        
        statsGrid.add(new JLabel("Data Transferred:"));
        transferRateLabel = new JLabel("0 B");
        statsGrid.add(transferRateLabel);
        
        statsPanel.add(statsGrid, BorderLayout.CENTER);
        
        // Stats log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Statistics Log"));
        statsLogArea = new JTextArea(8, 0);
        statsLogArea.setEditable(false);
        statsLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        logPanel.add(new JScrollPane(statsLogArea), BorderLayout.CENTER);
        
        statsPanel.add(logPanel, BorderLayout.SOUTH);
        
        return statsPanel;
    }
    
    private void setupEventHandlers() {
        // Window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        // Chat handlers
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        connectButton.addActionListener(e -> toggleConnection());
        
        // Add some demo data
        addDemoData();
    }
    
    private void addDemoData() {
        // Add demo online users
        SwingUtilities.invokeLater(() -> {
            onlineUsersModel.addElement("User1");
            onlineUsersModel.addElement("User2");
            onlineUsersModel.addElement("User3");
            
            // Add demo server files
            serverFilesModel.addElement("document1.pdf");
            serverFilesModel.addElement("image.png");
            serverFilesModel.addElement("data.csv");
            
            // Add demo local files
            localFilesModel.addElement("local_file1.txt");
            localFilesModel.addElement("local_image.jpg");
            
            // Add demo peers
            peersTableModel.addRow(new Object[]{"Peer-1", "192.168.1.100", "Online", LocalTime.now().format(timeFormatter)});
            peersTableModel.addRow(new Object[]{"Peer-2", "192.168.1.101", "Online", LocalTime.now().format(timeFormatter)});
            
            updateCounters();
        });
    }
    
    private void startBackgroundTasks() {
        // Simulate background activity
        backgroundExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    SwingUtilities.invokeLater(() -> {
                        lastHeartbeatLabel.setText(LocalTime.now().format(timeFormatter));
                        addToDiscoveryLog("Heartbeat received from network peers");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            addToChatLog("You: " + message);
            messageField.setText("");
            
            messagesSent++;
            updateStatLabels();
            
            // Simulate response
            backgroundExecutor.submit(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> {
                        addToChatLog("Echo: " + message);
                        messagesReceived++;
                        updateStatLabels();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
    
    private void toggleConnection() {
        if (connectButton.getText().equals("Connect")) {
            connectButton.setText("Disconnect");
            connectionStatusLabel.setText("🟢 Connected");
            addToChatLog("System: Connected to server " + serverAddressField.getText() + ":" + serverPortField.getText());
        } else {
            connectButton.setText("Connect");
            connectionStatusLabel.setText("🔴 Disconnected");
            addToChatLog("System: Disconnected from server");
        }
    }
    
    private void addToChatLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(timeFormatter);
            chatArea.append("[" + timestamp + "] " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void addToDiscoveryLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(timeFormatter);
            discoveryLogArea.append("[" + timestamp + "] " + message + "\n");
            discoveryLogArea.setCaretPosition(discoveryLogArea.getDocument().getLength());
        });
    }
    
    private void updateCounters() {
        totalPeersLabel.setText(String.valueOf(peersTableModel.getRowCount()));
        activePeersLabel.setText(String.valueOf(peersTableModel.getRowCount()));
    }
    
    private void updateStatLabels() {
        SwingUtilities.invokeLater(() -> {
            messagesSentLabel.setText(String.valueOf(messagesSent));
            messagesReceivedLabel.setText(String.valueOf(messagesReceived));
            packetsReceivedLabel.setText(String.valueOf(packetsReceived));
            transferRateLabel.setText(formatBytes(totalTransferred));
        });
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        else return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    private void shutdown() {
        System.out.println("🛑 Shutting down TeamSync application...");
        
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
        }
        
        System.out.println("✅ TeamSync application shutdown complete");
        dispose();
        System.exit(0);
    }
    
    // File operations methods
    private void refreshServerFiles() {
        SwingUtilities.invokeLater(() -> {
            transferStatusLabel.setText("Refreshing server files...");
            serverFilesModel.clear();
            logTransferMessage("Refreshing server files list...");
            
            // Simulate file list (in real implementation, connect to server)
            String[] sampleFiles = {"document1.pdf", "image1.jpg", "data.csv", "presentation.pptx"};
            for (String file : sampleFiles) {
                serverFilesModel.addElement(file);
            }
            
            transferStatusLabel.setText("Server files refreshed");
            logTransferMessage("Found " + sampleFiles.length + " files on server");
        });
    }
    
    private void downloadFile() {
        String selectedFile = serverFilesList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file to download", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            transferStatusLabel.setText("Downloading " + selectedFile + "...");
            transferProgressBar.setValue(0);
            logTransferMessage("Starting download: " + selectedFile);
            
            // Simulate download progress
            new Thread(() -> {
                for (int i = 0; i <= 100; i += 10) {
                    try {
                        Thread.sleep(200);
                        final int progress = i;
                        SwingUtilities.invokeLater(() -> transferProgressBar.setValue(progress));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    transferStatusLabel.setText("Download completed");
                    logTransferMessage("Successfully downloaded: " + selectedFile);
                    localFilesModel.addElement(selectedFile);
                });
            }).start();
        });
    }
    
    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            
            SwingUtilities.invokeLater(() -> {
                transferStatusLabel.setText("Uploading " + selectedFile.getName() + "...");
                transferProgressBar.setValue(0);
                logTransferMessage("Starting upload: " + selectedFile.getName());
                
                // Simulate upload progress
                new Thread(() -> {
                    for (int i = 0; i <= 100; i += 10) {
                        try {
                            Thread.sleep(300);
                            final int progress = i;
                            SwingUtilities.invokeLater(() -> transferProgressBar.setValue(progress));
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    SwingUtilities.invokeLater(() -> {
                        transferStatusLabel.setText("Upload completed");
                        logTransferMessage("Successfully uploaded: " + selectedFile.getName());
                        serverFilesModel.addElement(selectedFile.getName());
                        localFilesModel.addElement(selectedFile.getName());
                    });
                }).start();
            });
        } else {
            logTransferMessage("Upload cancelled by user");
        }
    }
    
    private void logTransferMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(timeFormatter);
            transferLogArea.append("[" + timestamp + "] " + message + "\n");
            transferLogArea.setCaretPosition(transferLogArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        System.out.println("🔧 Initializing TeamSync Swing Application...");
        
        // Enable high DPI scaling
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("swing.plaf.metal.controlFont", "Dialog-14");
        System.setProperty("swing.plaf.metal.systemFont", "Dialog-14");
        
        // Set system look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default look and feel
        }
        
        // Fix font rendering on macOS
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.textantialiasing", "true");
        System.setProperty("apple.awt.rendering", "VALUE_RENDER_QUALITY");
        
        // Set global font defaults with larger sizes
        Font defaultFont = new Font("SansSerif", Font.PLAIN, 14);
        Font boldFont = new Font("SansSerif", Font.BOLD, 14);
        Font labelFont = new Font("SansSerif", Font.PLAIN, 13);
        
        UIManager.put("Label.font", labelFont);
        UIManager.put("Button.font", boldFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", new Font("Monospaced", Font.PLAIN, 13));
        UIManager.put("List.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TabbedPane.font", boldFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Menu.font", defaultFont);
        UIManager.put("MenuItem.font", defaultFont);
        
        SwingUtilities.invokeLater(() -> {
            new MainSwing().setVisible(true);
            System.out.println("🚀 TeamSync Swing Application started successfully!");
        });
    }
}
