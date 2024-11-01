package library;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static Parent root;
    private static Scene scene;
    private static Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        if (this.primaryStage == null) {
            this.primaryStage = primaryStage;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    // Tạo chuyển trang
    public void loadNewScene(String name, ActionEvent actionEvent) {
        String url = "/FXML/" + name + ".fxml";
        try {
            Object obj = null;
            try {
                obj = FXMLLoader.load(this.getClass().getResource(url)); //Load trang
            } catch (IOException e) {
                System.out.println("Get resource from " + url + " is null");
                e.printStackTrace();
            }

            root = (Parent) obj;
            scene = new Scene(root);
            primaryStage = loadCurrentStage(actionEvent);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("Can not load FXML from: " + url);
            e.printStackTrace();
        }
    }

    // Show cảnh báo (Ví dụ mật khẩu sai)
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Chuyển về trang trước
    public static Stage loadCurrentStage(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if (source instanceof Node) {
            return (Stage) ((Node) source).getScene().getWindow();
        } else if (source instanceof MenuItem) {
            return (Stage) ((MenuItem) source).getParentPopup().getOwnerWindow();
        }
        return null;
    }


    public void loadFXMLtoAnchorPane(String fxml, AnchorPane pane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/" + fxml + ".fxml"));
            Node newContent = loader.load();
            pane.getChildren().clear();
            pane.getChildren().add(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
