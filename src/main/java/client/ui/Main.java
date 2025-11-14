package client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * TeamSync JavaFX Main Application
 * 
 * This is the main entry point for the JavaFX UI that integrates all backend modules:
 * - TCP Chat (ChatClient)
 * - TCP File Transfer (FileClient) 
 * - UDP Heartbeat/Peer Discovery
 * - NIO Echo Server
 * - Webbridge Module
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main UI FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/MainView.fxml"));
            if (loader.getLocation() == null) {
                // Try alternative path
                loader.setLocation(getClass().getResource("/src/main/resources/fxml/MainView.fxml"));
            }
            Parent root = loader.load();
            
            // Get the main controller to initialize backend connections
            MainController controller = loader.getController();
            controller.initializeBackendConnections();
            
            // Create scene with CSS styling
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            // Configure stage
            primaryStage.setTitle("TeamSync - Integrated Network Client");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Handle application shutdown
            primaryStage.setOnCloseRequest(e -> {
                controller.shutdown();
                System.exit(0);
            });
            
            primaryStage.show();
            
            System.out.println("🚀 TeamSync JavaFX Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error starting TeamSync application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method - entry point for the application
     */
    public static void main(String[] args) {
        System.out.println("🔧 Initializing TeamSync JavaFX Application...");
        launch(args);
    }
}
