package client.file_client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12346;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter path of file to send: ");
        String filePath = scanner.nextLine();

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File does not exist!");
            return;
        }

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             FileInputStream fis = new FileInputStream(file)) {

            // Step 1: Send file name
            dos.writeUTF(file.getName());

            // Step 2: Send file size
            dos.writeLong(file.length());

            // Step 3: Send file content
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }

            dos.flush();

            // Step 4: Read server response
            String response = dis.readUTF();
            System.out.println("Server: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
