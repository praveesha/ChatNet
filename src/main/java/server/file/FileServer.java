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

                // Handle file transfer in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            // Step 1: Receive file name
            String fileName = dis.readUTF();

            // Step 2: Receive file size
            long fileSize = dis.readLong();

            // Step 3: Save file to server folder
            File file = new File("server_files/" + fileName);
            file.getParentFile().mkdirs(); // create folder if not exists

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                long remaining = fileSize;

                while (remaining > 0 && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
            }

            System.out.println("Received file: " + fileName + " (" + fileSize + " bytes)");

            dos.writeUTF("File uploaded successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
