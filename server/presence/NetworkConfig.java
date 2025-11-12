package server.presence;

/**
 * Shared network configuration for the PresenceService.
 * All team members MUST use these exact same values to ensure 
 * all app instances can discover each other on the same multicast group.
 */
public class NetworkConfig {
    
    // Multicast group address (Class D IP address)
    public static final String MULTICAST_ADDRESS = "230.0.0.0";
    
    // Multicast port
    public static final int MULTICAST_PORT = 4446;
    
    // How often to send heartbeat packets (milliseconds)
    public static final int HEARTBEAT_INTERVAL_MS = 5000; // 5 seconds
    
    // How long before a user is considered offline (milliseconds)
    public static final int USER_TIMEOUT_MS = 15000; // 15 seconds
    
    // How often to check for and remove timed-out users (milliseconds)
    public static final int REAPER_INTERVAL_MS = 10000; // 10 seconds
    
    // Maximum size for UDP packets
    public static final int MAX_PACKET_SIZE = 1024;
    
    // Protocol message types
    public static final String MSG_DISCOVER = "DISCOVER";
    public static final String MSG_HEARTBEAT = "HEARTBEAT";
    public static final String MSG_EXIT = "EXIT";
    public static final String MSG_DELIMITER = "|";
}
