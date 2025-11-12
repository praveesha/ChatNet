package client.chat_client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

// Minimal JavaFX wrapper for your existing ChatClient
public class ChatClientFX extends Application {

    private TextArea chatArea;
    private TextField messageInput;
    private Button sendButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final String serverHost = "localhost";  // your server host
    private final int serverPort = 12345;           // replace with your ChatServer port

    @Override
    public void start(Stage primaryStage) {
        // ======== UI ========
        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageInput = new TextField();
        sendButton = new Button("Send");

        HBox inputArea = new HBox(10, messageInput, sendButton);
        inputArea.setPrefHeight(40);

        BorderPane root = new BorderPane();
        root.setCenter(chatArea);
        root.setBottom(inputArea);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ChatNet - Chat Client");
        primaryStage.show();

        // ======== Network ========
        new Thread(this::connectToServer).start();

        // ======== Button Action ========
        sendButton.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage()); // send on Enter key
    }

    // ======== Connect to server ========
    private void connectToServer() {
        try {
            socket = new Socket(serverHost, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Notify UI
            Platform.runLater(() -> chatArea.appendText("Connected to server!\n"));

            // Listen for incoming messages
            String line;
            while ((line = in.readLine()) != null) {
                String finalLine = line;
                Platform.runLater(() -> chatArea.appendText("Server: " + finalLine + "\n"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> chatArea.appendText("Failed to connect to server!\n"));
        }
    }

    // ======== Send message ========
    private void sendMessage() {
        String msg = messageInput.getText().trim();
        if (!msg.isEmpty() && out != null) {
            out.println(msg);
            chatArea.appendText("You: " + msg + "\n");
            messageInput.clear();
        }
    }

    @Override
    public void stop() throws Exception {
        // Close resources on exit
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}