package server.chat;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {

            String message;

            while ((message = reader.readLine()) != null) {

                if (message.startsWith("[FILE_NOTIFY]")) {
                    String[] parts = message.split(" ", 3);
                    String username = parts[1];
                    String fileName = parts[2];

                    ChatServer.broadcastFile(username, fileName);
                } else {
                    ChatServer.broadcast(message, this);
                }
            }

        } catch (IOException e) {

        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String msg) {
        writer.println(msg);
    }
}
