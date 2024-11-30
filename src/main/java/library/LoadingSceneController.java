package library;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoadingSceneController extends Controller {

    @FXML
    private Label loadingLabel;

    @FXML
    private AnchorPane loadingPane;  // Assuming you have an AnchorPane to control visibility

    @FXML
    private ProgressIndicator progressIndicator;  // Circular progress indicator

    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadingLabel.setText("Loading, please wait...");
        progressIndicator.setVisible(true); // Show the progress indicator by default
    }

    public void showLoadingMessage(String message) {
        loadingLabel.setText(message);
        show();  // Show the loading screen when a message is set
    }

    public void show() {
        loadingPane.setVisible(true);  // Show the loading pane
        progressIndicator.setVisible(true); // Ensure the circular loader is visible
    }

    public void hide() {
        loadingPane.setVisible(false);  // Hide the loading pane
        progressIndicator.setVisible(false); // Hide the circular loader
    }
}
