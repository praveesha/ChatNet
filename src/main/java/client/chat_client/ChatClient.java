package client.chat_client;

import java.io.*;
import java.net.*;

public class ChatClient {

    private static final String SERVER_IP = "localhost"; // or server IP
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("Connected to chat server");

            // Thread to read messages from server
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Main thread reads from user and sends to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = consoleReader.readLine()) != null) {
                writer.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
