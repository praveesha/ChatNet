package server.presence;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PresenceService - A singleton service that uses UDP multicast to discover
 * and track online users on the local network.
 * 
 * This service automatically:
 * - Discovers other users when they come online
 * - Sends periodic heartbeats to announce presence
 * - Removes users who haven't sent heartbeats (timeout)
 * - Handles graceful exits
 * 
 * Thread-safe for concurrent access from multiple modules.
 */
public class PresenceService {
    
    // Singleton instance with thread-safe lazy initialization
    private static volatile PresenceService instance;
    
    // Multicast networking
    private MulticastSocket socket;
    private InetAddress group;
    
    // Presence tracking: username -> last seen timestamp
    private final ConcurrentHashMap<String, Long> onlineUsers = new ConcurrentHashMap<>();
    
    // Current user's username
    private String myUsername;
    
    // Background threads
    private Thread listenerThread;
    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledExecutorService reaperExecutor;
    
    // Service state
    private volatile boolean running = false;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private PresenceService() {
        // Empty - initialization happens in start()
    }
    
    /**
     * Gets the singleton instance of PresenceService.
     * Thread-safe with double-checked locking.
     * 
     * @return the singleton instance
     */
    public static PresenceService getInstance() {
        if (instance == null) {
            synchronized (PresenceService.class) {
                if (instance == null) {
                    instance = new PresenceService();
                }
            }
        }
        return instance;
    }
    
    /**
     * Starts the presence service for the specified username.
     * This will:
     * - Join the multicast group
     * - Start listening for other users
     * - Send a DISCOVER message
     * - Begin sending periodic heartbeats
     * - Start the reaper thread to remove stale users
     * 
     * @param username the username to announce on the network
     * @throws IOException if network initialization fails
     */
    public synchronized void start(String username) throws IOException {
        if (running) {
            throw new IllegalStateException("PresenceService is already running");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        this.myUsername = username.trim();
        this.running = true;
        
        // Initialize multicast socket and join group
        socket = new MulticastSocket(NetworkConfig.MULTICAST_PORT);
        socket.setSoTimeout(1000); // 1 second timeout for receive operations
        group = InetAddress.getByName(NetworkConfig.MULTICAST_ADDRESS);
        socket.joinGroup(group);
        
        System.out.println("[PresenceService] Joined multicast group " + 
                          NetworkConfig.MULTICAST_ADDRESS + ":" + NetworkConfig.MULTICAST_PORT);
        
        // Start listener thread
        startListenerThread();
        
        // Send initial DISCOVER message
        sendMessage(NetworkConfig.MSG_DISCOVER, myUsername);
        
        // Start heartbeat sender
        startHeartbeatSender();
        
        // Start reaper thread
        startReaper();
        
        System.out.println("[PresenceService] Started for user: " + myUsername);
    }
    
    /**
     * Stops the presence service gracefully.
     * This will:
     * - Send an EXIT message
     * - Stop all background threads
     * - Leave the multicast group
     * - Clean up resources
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        
        System.out.println("[PresenceService] Stopping...");
        running = false;
        
        // Send EXIT message
        try {
            sendMessage(NetworkConfig.MSG_EXIT, myUsername);
        } catch (IOException e) {
            System.err.println("[PresenceService] Error sending EXIT message: " + e.getMessage());
        }
        
        // Stop executors
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
            try {
                if (!heartbeatExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    heartbeatExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatExecutor.shutdownNow();
            }
        }
        
        if (reaperExecutor != null) {
            reaperExecutor.shutdown();
            try {
                if (!reaperExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    reaperExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                reaperExecutor.shutdownNow();
            }
        }
        
        // Wait for listener thread to finish
        if (listenerThread != null) {
            try {
                listenerThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Leave multicast group and close socket
        try {
            if (socket != null && group != null) {
                socket.leaveGroup(group);
            }
        } catch (IOException e) {
            System.err.println("[PresenceService] Error leaving group: " + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        
        // Clear online users
        onlineUsers.clear();
        
        System.out.println("[PresenceService] Stopped");
    }
    
    /**
     * Gets the set of currently online usernames.
     * This is a snapshot of the current state and may change as users
     * come online or go offline.
     * 
     * @return a set of online usernames (excluding this user)
     */
    public Set<String> getOnlineUsers() {
        return onlineUsers.keySet();
    }
    
    /**
     * Starts the listener thread that receives multicast packets.
     */
    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            byte[] buffer = new byte[NetworkConfig.MAX_PACKET_SIZE];
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength()).trim();
                    handleMessage(message);
                    
                } catch (SocketTimeoutException e) {
                    // Normal - just continue loop
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[PresenceService] Listener error: " + e.getMessage());
                    }
                }
            }
        }, "PresenceListener");
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Starts the heartbeat sender that periodically announces this user's presence.
     */
    private void startHeartbeatSender() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeartbeatSender");
            t.setDaemon(true);
            return t;
        });
        
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                sendMessage(NetworkConfig.MSG_HEARTBEAT, myUsername);
            } catch (IOException e) {
                System.err.println("[PresenceService] Error sending heartbeat: " + e.getMessage());
            }
        }, NetworkConfig.HEARTBEAT_INTERVAL_MS, NetworkConfig.HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Starts the reaper thread that removes users who haven't sent heartbeats.
     */
    private void startReaper() {
        reaperExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PresenceReaper");
            t.setDaemon(true);
            return t;
        });
        
        reaperExecutor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            long timeoutThreshold = currentTime - NetworkConfig.USER_TIMEOUT_MS;
            
            onlineUsers.entrySet().removeIf(entry -> {
                if (entry.getValue() < timeoutThreshold) {
                    System.out.println("[PresenceService] User timed out: " + entry.getKey());
                    return true;
                }
                return false;
            });
        }, NetworkConfig.REAPER_INTERVAL_MS, NetworkConfig.REAPER_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Sends a message to the multicast group.
     * 
     * @param messageType the type of message (DISCOVER, HEARTBEAT, EXIT)
     * @param username the username to include in the message
     * @throws IOException if sending fails
     */
    private void sendMessage(String messageType, String username) throws IOException {
        String message = messageType + NetworkConfig.MSG_DELIMITER + username;
        byte[] data = message.getBytes();
        
        DatagramPacket packet = new DatagramPacket(
            data, 
            data.length, 
            group, 
            NetworkConfig.MULTICAST_PORT
        );
        
        socket.send(packet);
    }
    
    /**
     * Handles an incoming message from the multicast group.
     * 
     * @param message the received message
     */
    private void handleMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String[] parts = message.split("\\" + NetworkConfig.MSG_DELIMITER);
        if (parts.length != 2) {
            return; // Invalid message format
        }
        
        String messageType = parts[0];
        String username = parts[1];
        
        // Ignore messages from ourselves
        if (username.equals(myUsername)) {
            return;
        }
        
        switch (messageType) {
            case NetworkConfig.MSG_DISCOVER:
                handleDiscover(username);
                break;
                
            case NetworkConfig.MSG_HEARTBEAT:
                handleHeartbeat(username);
                break;
                
            case NetworkConfig.MSG_EXIT:
                handleExit(username);
                break;
                
            default:
                // Unknown message type - ignore
                break;
        }
    }
    
    /**
     * Handles a DISCOVER message from another user.
     * 
     * @param username the username that sent the discover message
     */
    private void handleDiscover(String username) {
        long currentTime = System.currentTimeMillis();
        onlineUsers.put(username, currentTime);
        System.out.println("[PresenceService] User discovered: " + username);
        
        // Send our own DISCOVER in response so the new user knows about us
        try {
            sendMessage(NetworkConfig.MSG_DISCOVER, myUsername);
        } catch (IOException e) {
            System.err.println("[PresenceService] Error responding to DISCOVER: " + e.getMessage());
        }
    }
    
    /**
     * Handles a HEARTBEAT message from another user.
     * 
     * @param username the username that sent the heartbeat
     */
    private void handleHeartbeat(String username) {
        long currentTime = System.currentTimeMillis();
        Long previousTime = onlineUsers.put(username, currentTime);
        
        // Only log if this is a new user (not just a regular heartbeat update)
        if (previousTime == null) {
            System.out.println("[PresenceService] User online: " + username);
        }
    }
    
    /**
     * Handles an EXIT message from another user.
     * 
     * @param username the username that is exiting
     */
    private void handleExit(String username) {
        onlineUsers.remove(username);
        System.out.println("[PresenceService] User exited: " + username);
    }
}
