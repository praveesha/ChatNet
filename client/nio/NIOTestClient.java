package client.nio;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class NIOTestClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connected to NIO Echo Server. Type messages (or 'quit' to exit):");
            
            String userInput;
            while (!(userInput = scanner.nextLine()).equals("quit")) {
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Echo: " + response);
            }
            
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}