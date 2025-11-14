package client.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO Client for echo testing and async communication
 */
public class NioClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9090;
    
    private SocketChannel socketChannel;
    private ExecutorService executor;
    private boolean connected = false;
    private String host;
    private int port;
    
    // Statistics
    private long messagesSent = 0;
    private long messagesReceived = 0;
    private long bytesSent = 0;
    private long bytesReceived = 0;
    private long startTime = 0;
    
    public NioClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public NioClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("NioClient-Thread");
            return t;
        });
    }
    
    /**
     * Connect to the NIO server
     */
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.connect(new InetSocketAddress(host, port));
                
                // Wait for connection to complete
                while (!socketChannel.finishConnect()) {
                    Thread.sleep(10);
                }
                
                connected = true;
                startTime = System.currentTimeMillis();
                System.out.println("🔗 NioClient connected to " + host + ":" + port);
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Failed to connect NioClient: " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * Send echo message and return response
     */
    public CompletableFuture<String> sendEcho(String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected || socketChannel == null) {
                throw new RuntimeException("Not connected to server");
            }
            
            try {
                // Send message
                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                writeBuffer.put(messageBytes);
                writeBuffer.flip();
                
                int bytesSentNow = socketChannel.write(writeBuffer);
                bytesSent += bytesSentNow;
                messagesSent++;
                
                System.out.println("📤 NIO Sent: " + message + " (" + bytesSentNow + " bytes)");
                
                // Read response
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                readBuffer.clear();
                
                // Non-blocking read with timeout
                long timeout = System.currentTimeMillis() + 5000; // 5 second timeout
                int bytesRead = 0;
                
                while (bytesRead <= 0 && System.currentTimeMillis() < timeout) {
                    bytesRead = socketChannel.read(readBuffer);
                    if (bytesRead <= 0) {
                        Thread.sleep(10); // Small delay for non-blocking
                    }
                }
                
                if (bytesRead > 0) {
                    readBuffer.flip();
                    byte[] responseBytes = new byte[readBuffer.remaining()];
                    readBuffer.get(responseBytes);
                    
                    String response = new String(responseBytes, StandardCharsets.UTF_8);
                    bytesReceived += bytesRead;
                    messagesReceived++;
                    
                    System.out.println("📥 NIO Received: " + response + " (" + bytesRead + " bytes)");
                    return response;
                } else {
                    throw new RuntimeException("Timeout waiting for response");
                }
                
            } catch (Exception e) {
                System.err.println("❌ NIO Echo failed: " + e.getMessage());
                throw new RuntimeException("Echo failed: " + e.getMessage());
            }
        }, executor);
    }
    
    /**
     * Calculate latency by sending echo and measuring round trip time
     */
    public CompletableFuture<Long> measureLatency(String testMessage) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.nanoTime();
            
            try {
                String response = sendEcho(testMessage).get();
                long endTime = System.nanoTime();
                long latencyNs = endTime - startTime;
                long latencyMs = latencyNs / 1_000_000;
                
                System.out.println("🔄 NIO Latency: " + latencyMs + "ms");
                return latencyMs;
                
            } catch (Exception e) {
                System.err.println("❌ Latency measurement failed: " + e.getMessage());
                return -1L;
            }
        }, executor);
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            connected = false;
            if (socketChannel != null && socketChannel.isConnected()) {
                socketChannel.close();
            }
            System.out.println("🔌 NioClient disconnected");
        } catch (IOException e) {
            System.err.println("❌ Error disconnecting NioClient: " + e.getMessage());
        }
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
    
    // Getters for statistics
    public boolean isConnected() { return connected; }
    public long getMessagesSent() { return messagesSent; }
    public long getMessagesReceived() { return messagesReceived; }
    public long getBytesSent() { return bytesSent; }
    public long getBytesReceived() { return bytesReceived; }
    public long getConnectionUptime() { 
        return connected ? System.currentTimeMillis() - startTime : 0; 
    }
    public String getServerInfo() { return host + ":" + port; }
}
