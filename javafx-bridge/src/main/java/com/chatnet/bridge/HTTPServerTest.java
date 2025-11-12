package com.chatnet.bridge;

/**
 * Test runner for HTTP Server only
 * Useful for testing the web frontend without JavaFX dependencies
 */
public class HTTPServerTest {
    
    public static void main(String[] args) {
        System.out.println("🌐 Starting ChatNet HTTP Server Test...");
        System.out.println("This will start only the HTTP server for web frontend testing");
        System.out.println();
        
        // Create HTTP server with simple file list provider
        HTTPServer httpServer = new HTTPServer(8081, HTTPServerTest::getFileList);
        
        // Start server in a separate thread
        Thread serverThread = new Thread(httpServer);
        serverThread.start();
        
        System.out.println("✅ HTTP Server started successfully!");
        System.out.println();
        System.out.println("🔗 Access your web frontend at: http://localhost:8081");
        System.out.println("📁 File API available at: http://localhost:8081/api/files");
        System.out.println();
        System.out.println("Note: WebSocket functionality requires the full JavaFX application");
        System.out.println("Press Ctrl+C to stop the server");
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutting down HTTP server...");
            httpServer.stop();
            System.out.println("✅ Server stopped successfully!");
        }));
        
        // Keep the application running
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        }
    }
    
    /**
     * Simple file list provider for testing
     */
    private static String[] getFileList() {
        java.io.File folder = new java.io.File("filestorage");
        if (!folder.exists()) {
            folder.mkdirs();
            return new String[0];
        }
        
        String[] files = folder.list((dir, name) -> new java.io.File(dir, name).isFile());
        return files != null ? files : new String[0];
    }
}
