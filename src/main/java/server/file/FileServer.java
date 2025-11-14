package server.file;

import java.io.*;
import java.net.*;

public class FileServer {

    private static final int PORT = 12346;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("File server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            String command = dis.readUTF();
            System.out.println("Command received = " + command);

            // -------------------------
            // FILE DOWNLOAD
            // -------------------------
            if (command.equalsIgnoreCase("DOWNLOAD")) {
                String fileName = dis.readUTF();
                File file = new File("server_files/" + fileName);

                if (!file.exists()) {
                    dos.writeUTF("ERROR");
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

            // -------------------------
            // FILE UPLOAD
            // -------------------------
            if (command.equalsIgnoreCase("UPLOAD")) {

                String username = dis.readUTF();   // 🔥 now we have username!
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

                System.out.println("Received file from " + username + ": " + fileName);
                dos.writeUTF("UPLOAD_OK");

                notifyChatServer(username, fileName);
            }

        } catch (IOException e) {
            System.err.println("FileServer error: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private static void notifyChatServer(String username, String fileName) {

        try (Socket chatSocket = new Socket("localhost", 12345);
             PrintWriter writer = new PrintWriter(chatSocket.getOutputStream(), true)) {

            writer.println("[FILE_NOTIFY] " + username + " " + fileName);

        } catch (IOException e) {
            System.err.println("Could not notify chat server: " + e.getMessage());
        }
    }
}
