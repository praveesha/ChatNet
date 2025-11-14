package server.chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server running...");

            while (true) {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler c : clientHandlers) {
            if (c != sender) {
                c.sendMessage(msg);
            }
        }
    }

    public static void broadcastFile(String username, String fileName) {
        String formatted = username + ": [FILE] " + fileName;

        for (ClientHandler c : clientHandlers) {
            c.sendMessage(formatted);
        }
    }
}
