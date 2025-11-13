package client.chat_client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ChatClientFX extends Application {
    private VBox chatBox;
    private ScrollPane scrollPane;
    private TextField messageInput;
    private Button sendButton;
    private Label typingLabel;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private boolean typing = false;
    private long lastTypedTime = 0;

    @Override
    public void start(Stage primaryStage) {
        // Username dialog
        TextInputDialog dialog = new TextInputDialog("User");
        dialog.setTitle("Chat Login");
        dialog.setHeaderText("Enter your username:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        username = result.orElse("Anonymous");

        // Header bar
        Label titleLabel = new Label("💬 " + username);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label statusLabel = new Label("🟢 Online");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #D0F0C0;");

        VBox header = new VBox(2, titleLabel, statusLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));

        HBox headerContainer = new HBox(header);
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        headerContainer.setStyle("-fx-background-color: linear-gradient(to right, #1976D2, #42A5F5);");

        // Chat area
        chatBox = new VBox(10);
        chatBox.setPadding(new Insets(15));
        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));

        scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("""
            -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
        """);

        // Typing label
        typingLabel = new Label();
        typingLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        typingLabel.setVisible(false);

        HBox typingContainer = new HBox(typingLabel);
        typingContainer.setAlignment(Pos.CENTER_LEFT);
        typingContainer.setPadding(new Insets(5, 10, 5, 15));

        // Message input bar
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(330);
        messageInput.setStyle("""
            -fx-background-color: #FAFAFA;
            -fx-border-color: #BDBDBD;
            -fx-border-radius: 20;
            -fx-background-radius: 20;
            -fx-padding: 8 15 8 15;
        """);

        // Detect typing
        messageInput.textProperty().addListener((obs, oldText, newText) -> {
            if (!typing) {
                typing = true;
                out.println(username + " is typing...");
            }
            lastTypedTime = System.currentTimeMillis();

            // Stop typing if idle for 1.5s
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    if (System.currentTimeMillis() - lastTypedTime >= 1500) {
                        typing = false;
                        out.println(username + " stopped typing");
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        });

        messageInput.setOnAction(e -> sendMessage());

        sendButton = new Button("Send");
        sendButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #2196F3, #64B5F6);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 20;
            -fx-padding: 8 15 8 15;
        """);
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(10, messageInput, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #ECEFF1; -fx-border-color: #CFD8DC;");

        // Root layout
        BorderPane root = new BorderPane();
        root.setTop(headerContainer);
        VBox centerLayout = new VBox(scrollPane, typingContainer);
        root.setCenter(centerLayout);
        root.setBottom(inputBox);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 470, 540);
        scene.getRoot().setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #E3F2FD, #FFF9C4);
        """);
        primaryStage.setTitle("Chat - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(username + " joined the chat");

            String message;
            while ((message = in.readLine()) != null) {
                String msg = message;
                Platform.runLater(() -> handleIncomingMessage(msg));
            }
        } catch (IOException e) {
            Platform.runLater(() -> showAlert("Connection Error", "Could not connect to server."));
        }
    }

    private void handleIncomingMessage(String message) {
        if (message.contains("joined the chat") || message.contains("left the chat")) {
            addSystemMessage(message);
            return;
        }

        if (message.endsWith("is typing...")) {
            if (!message.startsWith(username)) {
                typingLabel.setText("💭 " + message);
                typingLabel.setVisible(true);
            }
            return;
        }

        if (message.endsWith("stopped typing")) {
            typingLabel.setVisible(false);
            return;
        }

        if (message.startsWith(username + ":")) return;

        addMessageBubble(message, false);
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty() || out == null) return;

        String formatted = username + ": " + text;
        out.println(formatted);
        addMessageBubble(formatted, true);
        messageInput.clear();
    }

    private void addMessageBubble(String message, boolean isOwn) {
        String[] parts = message.split(":", 2);
        String sender = parts.length > 1 ? parts[0].trim() : "Server";
        String msgText = parts.length > 1 ? parts[1].trim() : message;

        Label bubble = new Label(msgText);
        bubble.setWrapText(true);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setMaxWidth(280);
        bubble.setStyle(
                "-fx-background-radius: 18;" +
                        (isOwn
                                ? "-fx-background-color: linear-gradient(to right, #42A5F5, #90CAF9); -fx-text-fill: white;"
                                : "-fx-background-color: #F8BBD0; -fx-text-fill: black;")
        );

        String time = LocalTime.now().format(timeFormatter);
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: gray;");

        Label nameLabel = new Label(sender);
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        VBox msgContainer = new VBox(3, nameLabel, bubble, timeLabel);
        msgContainer.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatBox.getChildren().add(msgContainer);
    }

    private void addSystemMessage(String message) {
        Label systemLabel = new Label(message);
        systemLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        systemLabel.setAlignment(Pos.CENTER);
        systemLabel.setMaxWidth(Double.MAX_VALUE);

        HBox container = new HBox(systemLabel);
        container.setAlignment(Pos.CENTER);
        chatBox.getChildren().add(container);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (socket != null && !socket.isClosed()) {
            out.println(username + " left the chat");
            socket.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
