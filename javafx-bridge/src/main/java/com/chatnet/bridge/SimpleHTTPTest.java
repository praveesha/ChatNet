package com.chatnet.bridge;

import java.io.*;
import java.net.*;

public class SimpleHTTPTest {
    public static void main(String[] args) {
        try {
            System.out.println("🧪 Testing Simple HTTP Server...");
            
            ServerSocket serverSocket = new ServerSocket(8081);
            System.out.println("✅ Server started on port 8081");
            System.out.println("🌐 Open http://localhost:8081 in your browser");
            System.out.println("Press Ctrl+C to stop");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("📥 New connection from: " + clientSocket.getInetAddress());
                
                // Handle request in a new thread
                new Thread(() -> {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                        
                        String requestLine = in.readLine();
                        System.out.println("📨 Request: " + requestLine);
                        
                        // Skip headers
                        String line;
                        while ((line = in.readLine()) != null && !line.isEmpty()) {
                            System.out.println("📋 Header: " + line);
                        }
                        
                        // Send response
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: text/html");
                        out.println("Access-Control-Allow-Origin: *");
                        out.println();
                        out.println("<!DOCTYPE html>");
                        out.println("<html><head><title>Test Server</title></head>");
                        out.println("<body>");
                        out.println("<h1>🎉 Server is Working!</h1>");
                        out.println("<p>Your HTTP server is responding correctly.</p>");
                        out.println("<p>Time: " + new java.util.Date() + "</p>");
                        out.println("</body></html>");
                        out.flush();
                        
                        clientSocket.close();
                        System.out.println("✅ Request handled successfully");
                        
                    } catch (IOException e) {
                        System.err.println("❌ Error handling request: " + e.getMessage());
                    }
                }).start();
            }
            
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
