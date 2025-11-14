package client.file_client;

import java.io.*;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced FileClient for programmatic file transfer operations
 */
public class EnhancedFileClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12346;
    
    private String serverHost;
    private int serverPort;
    private ExecutorService executor;
    private boolean connected = false;
    
    // Statistics
    private long filesUploaded = 0;
    private long filesDownloaded = 0;
    private long bytesUploaded = 0;
    private long bytesDownloaded = 0;
    private long connectionStartTime = 0;
    
    // Callbacks
    private Consumer<String> progressCallback;
    private Consumer<String> statusCallback;
    
    public EnhancedFileClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public EnhancedFileClient(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("FileClient-Thread");
            return t;
        });
    }
    
    /**
     * Set progress callback for UI updates
     */
    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }
    
    /**
     * Set status callback for UI updates
     */
    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }
    
    /**
     * Test connection to file server
     */
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Test connection
                try (Socket testSocket = new Socket(serverHost, serverPort)) {
                    connected = true;
                    connectionStartTime = System.currentTimeMillis();
                    updateStatus("Connected to file server: " + serverHost + ":" + serverPort);
                    return true;
                }
            } catch (IOException e) {
                connected = false;
                updateStatus("Failed to connect to file server: " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * Upload a file to the server
     */
    public CompletableFuture<Boolean> upload(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File(filePath);
            
            if (!file.exists()) {
                updateStatus("❌ File does not exist: " + filePath);
                return false;
            }
            
            updateStatus("📤 Uploading: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
            updateProgress("Starting upload...");
            
            try (Socket socket = new Socket(serverHost, serverPort);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream());
                 FileInputStream fis = new FileInputStream(file)) {
                
                // Send upload command
                dos.writeUTF("UPLOAD");
                
                // Send file name
                dos.writeUTF(file.getName());
                
                // Send file size
                long fileSize = file.length();
                dos.writeLong(fileSize);
                
                // Send file content with progress
                byte[] buffer = new byte[4096];
                long totalSent = 0;
                int read;
                
                while ((read = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, read);
                    totalSent += read;
                    
                    int progress = (int) ((totalSent * 100) / fileSize);
                    updateProgress("Uploading... " + progress + "% (" + 
                                 formatFileSize(totalSent) + "/" + formatFileSize(fileSize) + ")");
                    
                    // Small delay to prevent overwhelming the UI
                    if (totalSent % 8192 == 0) {
                        Thread.sleep(1);
                    }
                }
                
                dos.flush();
                
                // Read server response
                String response = dis.readUTF();
                
                if (response.startsWith("SUCCESS")) {
                    filesUploaded++;
                    bytesUploaded += fileSize;
                    updateStatus("✅ Upload completed: " + file.getName());
                    updateProgress("Upload completed successfully!");
                    return true;
                } else {
                    updateStatus("❌ Upload failed: " + response);
                    return false;
                }
                
            } catch (Exception e) {
                updateStatus("❌ Upload error: " + e.getMessage());
                updateProgress("Upload failed!");
                return false;
            }
        }, executor);
    }
    
    /**
     * Download a file from the server
     */
    public CompletableFuture<Boolean> download(String fileName, String saveDirectory) {
        return CompletableFuture.supplyAsync(() -> {
            updateStatus("📥 Requesting download: " + fileName);
            updateProgress("Starting download...");
            
            try (Socket socket = new Socket(serverHost, serverPort);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                
                // Send download command
                dos.writeUTF("DOWNLOAD");
                dos.writeUTF(fileName);
                dos.flush();
                
                // Read response
                String response = dis.readUTF();
                
                if (!response.equals("FILE_FOUND")) {
                    updateStatus("❌ File not found on server: " + fileName);
                    return false;
                }
                
                // Read file size
                long fileSize = dis.readLong();
                updateStatus("📥 Downloading: " + fileName + " (" + formatFileSize(fileSize) + ")");
                
                // Create save file
                File saveDir = new File(saveDirectory);
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }
                
                File saveFile = new File(saveDir, fileName);
                
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    byte[] buffer = new byte[4096];
                    long totalReceived = 0;
                    int read;
                    
                    while (totalReceived < fileSize) {
                        int toRead = (int) Math.min(buffer.length, fileSize - totalReceived);
                        read = dis.read(buffer, 0, toRead);
                        
                        if (read == -1) break;
                        
                        fos.write(buffer, 0, read);
                        totalReceived += read;
                        
                        int progress = (int) ((totalReceived * 100) / fileSize);
                        updateProgress("Downloading... " + progress + "% (" + 
                                     formatFileSize(totalReceived) + "/" + formatFileSize(fileSize) + ")");
                        
                        // Small delay to prevent overwhelming the UI
                        if (totalReceived % 8192 == 0) {
                            Thread.sleep(1);
                        }
                    }
                    
                    if (totalReceived == fileSize) {
                        filesDownloaded++;
                        bytesDownloaded += fileSize;
                        updateStatus("✅ Download completed: " + fileName);
                        updateProgress("Download completed successfully!");
                        return true;
                    } else {
                        updateStatus("❌ Download incomplete: " + fileName);
                        return false;
                    }
                }
                
            } catch (Exception e) {
                updateStatus("❌ Download error: " + e.getMessage());
                updateProgress("Download failed!");
                return false;
            }
        }, executor);
    }
    
    /**
     * List files on the server
     */
    public CompletableFuture<List<String>> listFiles() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> files = new ArrayList<>();
            
            try (Socket socket = new Socket(serverHost, serverPort);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                
                // Send list command
                dos.writeUTF("LIST");
                dos.flush();
                
                // Read file count
                int fileCount = dis.readInt();
                
                // Read file names
                for (int i = 0; i < fileCount; i++) {
                    String fileName = dis.readUTF();
                    files.add(fileName);
                }
                
                updateStatus("📋 Listed " + fileCount + " files from server");
                
            } catch (Exception e) {
                updateStatus("❌ Failed to list files: " + e.getMessage());
            }
            
            return files;
        }, executor);
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        connected = false;
        updateStatus("🔌 Disconnected from file server");
    }
    
    /**
     * Shutdown client and cleanup resources
     */
    public void shutdown() {
        disconnect();
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    // Helper methods
    private void updateStatus(String status) {
        System.out.println(status);
        if (statusCallback != null) {
            statusCallback.accept(status);
        }
    }
    
    private void updateProgress(String progress) {
        if (progressCallback != null) {
            progressCallback.accept(progress);
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
    
    // Getters for statistics
    public boolean isConnected() { return connected; }
    public long getFilesUploaded() { return filesUploaded; }
    public long getFilesDownloaded() { return filesDownloaded; }
    public long getBytesUploaded() { return bytesUploaded; }
    public long getBytesDownloaded() { return bytesDownloaded; }
    public long getConnectionUptime() { 
        return connected ? System.currentTimeMillis() - connectionStartTime : 0; 
    }
    public String getServerInfo() { return serverHost + ":" + serverPort; }
}
