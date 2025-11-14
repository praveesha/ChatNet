package client.udp;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * UDP Receiver for peer discovery and network monitoring
 */
public class UdpReceiver {
    private static final int DEFAULT_PORT = 8888;
    private static final int DISCOVERY_PORT = 8889;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    
    private DatagramSocket socket;
    private DatagramSocket discoverySocket;
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final AtomicBoolean discovering = new AtomicBoolean(false);
    private ExecutorService executor;
    private int port;
    
    // Discovered peers
    private final Map<String, PeerInfo> discoveredPeers = new ConcurrentHashMap<>();
    
    // Statistics
    private long packetsReceived = 0;
    private long bytesReceived = 0;
    private long discoveryStartTime = 0;
    
    // Callbacks for UI updates
    private Consumer<String> messageCallback;
    private Consumer<List<PeerInfo>> peersCallback;
    
    public static class PeerInfo {
        public final String address;
        public final String name;
        public final long lastSeen;
        public final String status;
        
        public PeerInfo(String address, String name, long lastSeen, String status) {
            this.address = address;
            this.name = name;
            this.lastSeen = lastSeen;
            this.status = status;
        }
        
        @Override
        public String toString() {
            return name + " (" + address + ") - " + status;
        }
    }
    
    public UdpReceiver() {
        this(DEFAULT_PORT);
    }
    
    public UdpReceiver(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("UdpReceiver-Thread");
            return t;
        });
    }
    
    /**
     * Set callback for receiving messages
     */
    public void setMessageCallback(Consumer<String> callback) {
        this.messageCallback = callback;
    }
    
    /**
     * Set callback for peer updates
     */
    public void setPeersCallback(Consumer<List<PeerInfo>> callback) {
        this.peersCallback = callback;
    }
    
    /**
     * Start listening for UDP packets
     */
    public CompletableFuture<Boolean> startListening() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (listening.get()) {
                    return true; // Already listening
                }
                
                socket = new DatagramSocket(port);
                listening.set(true);
                discoveryStartTime = System.currentTimeMillis();
                
                System.out.println("🔊 UDP Receiver started on port " + port);
                
                // Start listening thread
                executor.submit(this::listenForPackets);
                
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Failed to start UDP Receiver: " + e.getMessage());
                listening.set(false);
                return false;
            }
        }, executor);
    }
    
    /**
     * Start peer discovery
     */
    public CompletableFuture<Boolean> startPeerDiscovery() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (discovering.get()) {
                    return true; // Already discovering
                }
                
                discoverySocket = new DatagramSocket();
                discoverySocket.setBroadcast(true);
                discovering.set(true);
                
                System.out.println("🔍 Peer discovery started");
                
                // Start discovery threads
                executor.submit(this::broadcastDiscovery);
                executor.submit(this::listenForDiscovery);
                
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Failed to start peer discovery: " + e.getMessage());
                discovering.set(false);
                return false;
            }
        }, executor);
    }
    
    /**
     * Listen for UDP packets
     */
    private void listenForPackets() {
        byte[] buffer = new byte[1024];
        
        while (listening.get() && socket != null && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                String senderAddress = packet.getAddress().getHostAddress();
                
                packetsReceived++;
                bytesReceived += packet.getLength();
                
                System.out.println("📦 UDP Received from " + senderAddress + ": " + message);
                
                if (messageCallback != null) {
                    messageCallback.accept("From " + senderAddress + ": " + message);
                }
                
            } catch (IOException e) {
                if (listening.get()) {
                    System.err.println("❌ Error receiving UDP packet: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Broadcast discovery messages
     */
    private void broadcastDiscovery() {
        while (discovering.get() && discoverySocket != null) {
            try {
                String discoveryMessage = "DISCOVER:TeamSync:localhost";
                byte[] data = discoveryMessage.getBytes();
                
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, 
                    InetAddress.getByName(BROADCAST_ADDRESS), 
                    DISCOVERY_PORT
                );
                
                discoverySocket.send(packet);
                System.out.println("📡 Broadcast discovery message");
                
                Thread.sleep(5000); // Broadcast every 5 seconds
                
            } catch (Exception e) {
                if (discovering.get()) {
                    System.err.println("❌ Error broadcasting discovery: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Listen for discovery responses
     */
    private void listenForDiscovery() {
        try (DatagramSocket listener = new DatagramSocket(DISCOVERY_PORT)) {
            byte[] buffer = new byte[1024];
            
            while (discovering.get()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    listener.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength());
                    String senderAddress = packet.getAddress().getHostAddress();
                    
                    if (message.startsWith("DISCOVER:")) {
                        String[] parts = message.split(":");
                        if (parts.length >= 3) {
                            String peerName = parts[1];
                            String peerAddress = senderAddress;
                            
                            PeerInfo peer = new PeerInfo(
                                peerAddress, 
                                peerName, 
                                System.currentTimeMillis(),
                                "Active"
                            );
                            
                            discoveredPeers.put(peerAddress, peer);
                            
                            System.out.println("🔍 Discovered peer: " + peer);
                            
                            if (peersCallback != null) {
                                peersCallback.accept(new ArrayList<>(discoveredPeers.values()));
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    if (discovering.get()) {
                        System.err.println("❌ Error in discovery listener: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start discovery listener: " + e.getMessage());
        }
    }
    
    /**
     * Send a UDP message to specific address
     */
    public CompletableFuture<Boolean> sendMessage(String address, int targetPort, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, 
                    InetAddress.getByName(address), 
                    targetPort
                );
                
                if (socket != null) {
                    socket.send(packet);
                    System.out.println("📤 UDP Sent to " + address + ":" + targetPort + " - " + message);
                    return true;
                }
                
                return false;
                
            } catch (Exception e) {
                System.err.println("❌ Failed to send UDP message: " + e.getMessage());
                return false;
            }
        }, executor);
    }
    
    /**
     * Stop listening
     */
    public void stopListening() {
        listening.set(false);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("🔇 UDP Receiver stopped");
    }
    
    /**
     * Stop peer discovery
     */
    public void stopPeerDiscovery() {
        discovering.set(false);
        if (discoverySocket != null && !discoverySocket.isClosed()) {
            discoverySocket.close();
        }
        discoveredPeers.clear();
        System.out.println("🛑 Peer discovery stopped");
    }
    
    /**
     * Shutdown receiver and cleanup resources
     */
    public void shutdown() {
        stopListening();
        stopPeerDiscovery();
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    // Getters
    public boolean isListening() { return listening.get(); }
    public boolean isDiscovering() { return discovering.get(); }
    public long getPacketsReceived() { return packetsReceived; }
    public long getBytesReceived() { return bytesReceived; }
    public int getPeerCount() { return discoveredPeers.size(); }
    public List<PeerInfo> getDiscoveredPeers() { return new ArrayList<>(discoveredPeers.values()); }
    public long getDiscoveryUptime() { 
        return discovering.get() ? System.currentTimeMillis() - discoveryStartTime : 0; 
    }
}
