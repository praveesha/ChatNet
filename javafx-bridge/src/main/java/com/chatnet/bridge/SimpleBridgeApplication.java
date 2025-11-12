package com.chatnet.bridge;

/**
 * Simplified Bridge Application without JavaFX
 * Starts HTTP and WebSocket servers for web-based chat
 */
public class SimpleBridgeApplication {
    
    // Network configuration
    private static final int HTTP_PORT = 8081;
    private static final int WEBSOCKET_PORT = 8082;
    
    public static void main(String[] args) {
        System.out.println("Starting ChatNet Bridge Server...");
        
        // Create and start HTTP server
        HTTPServer httpServer = new HTTPServer(HTTP_PORT, SimpleBridgeApplication::getFileList);
        Thread httpThread = new Thread(httpServer);
        httpThread.start();
        
        // Create and start WebSocket server  
        SimpleWebSocketServer wsServer = new SimpleWebSocketServer(WEBSOCKET_PORT);
        Thread wsThread = new Thread(wsServer);
        wsThread.start();
        
        System.out.println("HTTP Server: http://localhost:" + HTTP_PORT);
        System.out.println("WebSocket Server: ws://localhost:" + WEBSOCKET_PORT + "/ws/chat");
        System.out.println("Press Ctrl+C to stop...");
        
        // Keep the application running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down servers...");
            httpServer.stop();
            wsServer.stop();
        }));
        
        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Application interrupted");
        }
    }
    
    private static String[] getFileList() {
        java.io.File folder = new java.io.File("filestorage");
        if (!folder.exists()) {
            folder.mkdirs();
            return new String[0];
        }
        return folder.list((dir, name) -> new java.io.File(dir, name).isFile());
    }
}
