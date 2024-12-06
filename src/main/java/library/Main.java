package library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    private static double savedWidth = 1200;
    private static double savedHeight = 665;
    private static final List<Stage> activeStages = new ArrayList<>(); // Track all active stages

    @Override
    public void start(Stage primaryStage) {
        DatabaseHelper.startXamppServices();
        DatabaseHelper.getInstance();
        Main.primaryStage = primaryStage;
        Main.primaryStage.setTitle("Library Management System");
        loadScene("/FXML/LoginScene.fxml");

        primaryStage.setOnCloseRequest(event -> {
            closeAllStages(); // Close all secondary stages
            DatabaseHelper.stopXamppServices();
            System.out.println("XAMPP services stopped.");
        });

        primaryStage.show();
    }

    public void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(this.getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);

            primaryStage.setTitle("Library Management System");
            primaryStage.setScene(scene);
            primaryStage.setWidth(savedWidth);
            primaryStage.setHeight(savedHeight);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a secondary stage to track active stages.
     */
    public static void registerStage(Stage stage) {
        activeStages.add(stage);
        stage.setOnCloseRequest(event -> activeStages.remove(stage)); // Remove stage when it is closed
    }

    /**
     * Closes all active stages except the primary stage.
     */
    private void closeAllStages() {
        for (Stage stage : new ArrayList<>(activeStages)) { // Copy list to avoid concurrent modification
            stage.close();
        }
        activeStages.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
