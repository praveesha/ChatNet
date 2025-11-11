package client.nio;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadTestClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int NUM_CLIENTS = 100;
    private static final int MESSAGES_PER_CLIENT = 10;
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
        
        System.out.println("Starting load test with " + NUM_CLIENTS + " concurrent clients...");
        
        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientId = i;
            executor.submit(() -> runClient(clientId));
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
            System.out.println("Load test completed!");
        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
        }
    }
    
    private static void runClient(int clientId) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            for (int i = 0; i < MESSAGES_PER_CLIENT; i++) {
                String message = "Client-" + clientId + "-Message-" + i;
                out.println(message);
                String response = in.readLine();
                
                if (!message.equals(response)) {
                    System.err.println("Echo mismatch for client " + clientId);
                }
            }
            
            System.out.println("Client " + clientId + " completed");
            
        } catch (IOException e) {
            System.err.println("Client " + clientId + " error: " + e.getMessage());
        }
    }
}