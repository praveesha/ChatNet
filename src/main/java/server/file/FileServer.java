package server.file;

import java.io.*;
import java.net.*;

public class FileServer {

    private static final int PORT = 12346; // file transfer port

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("File server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected for file transfer: " + clientSocket);

                // Handle each file client in its own thread
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            // First, read the command (UPLOAD or DOWNLOAD)
            System.out.println("Waiting for command from " + clientSocket);
            String command = dis.readUTF();
            System.out.println("Command received: " + command);

            if (command.equalsIgnoreCase("DOWNLOAD")) {
                // === FILE DOWNLOAD LOGIC ===
                String fileName = dis.readUTF();
                File file = new File("server_files/" + fileName);
                if (!file.exists()) {
                    dos.writeUTF("ERROR");
                    System.out.println("Download requested but file not found: " + fileName);
                    return;
                }

                dos.writeUTF("OK");
                dos.writeLong(file.length());

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, read);
                    }
                }
                System.out.println("Sent file: " + fileName);
                return;
            }

            // === FILE UPLOAD LOGIC ===
            if (command.equalsIgnoreCase("UPLOAD")) {
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();

                File file = new File("server_files/" + fileName);
                file.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    long remaining = fileSize;
                    while (remaining > 0 &&
                            (read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        fos.write(buffer, 0, read);
                        remaining -= read;
                    }
                }

                System.out.println("Received file: " + fileName + " (" + fileSize + " bytes)");
                dos.writeUTF("File uploaded successfully!");

                // Notify chat server about the new file
                notifyChatServer(fileName);
            } else {
                System.out.println("Unknown command: " + command);
            }

        } catch (IOException e) {
            System.err.println("handleClient error: " + e.getMessage());
            //e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private static void notifyChatServer(String fileName) {
        try (Socket chatSocket = new Socket("localhost", 12345);
             PrintWriter writer = new PrintWriter(chatSocket.getOutputStream(), true)) {
            writer.println("[SERVER_FILE] " + fileName);
            System.out.println("Notified chat server about uploaded file: " + fileName);
        } catch (IOException e) {
            System.err.println("Could not notify chat server: " + e.getMessage());
        }
    }
}
