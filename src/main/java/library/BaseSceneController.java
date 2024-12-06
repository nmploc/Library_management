package library;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

// Quản lý giao diện người dùng
public class BaseSceneController extends Controller
{
    @FXML
    private VBox navigationBar;

    @FXML
    private Label sceneTitle;

    @FXML
    private AnchorPane contentPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Sử dụng Singleton của DatabaseHelper để thiết lập kết nối
        DatabaseHelper.getInstance();  // Đảm bảo kết nối đã được thiết lập trong constructor của DatabaseHelper

        // Thiết lập sự kiện cho các Button trong navigationBar
        for (Node node : navigationBar.getChildren()) {
            if (node instanceof Button) {
                Button but = (Button) node;
                but.setOnMouseEntered(event -> but.setCursor(Cursor.HAND));
                but.setOnMouseExited(event -> but.setCursor(Cursor.DEFAULT));
            }
        }

        // Tải FXML vào contentPane
        loadFXMLtoAnchorPane("Dashboard", contentPane);
    }

    @FXML
    private void handleReportButtonAction() {
        loadFXMLtoAnchorPane("ReportScene", contentPane);
    }

    @FXML
    private void handleDashBoardButton() {
        loadFXMLtoAnchorPane("Dashboard", contentPane);
    }

    @FXML
    private void handleBooksButtonAction() {
        // Tải giao diện books.fxml và sử dụng BooksController
        loadFXMLtoAnchorPane("books", contentPane); // đảm bảo tên tệp là chính xác (không cần .fxml)
    }

    @FXML
    private void handleReadersButtonAction() {
        loadFXMLtoAnchorPane("Readers", contentPane);
    }

    @FXML
    private void handleBorrowingsButtonAction() {
        // Load the Borrowings FXML
        loadFXMLtoAnchorPane("Borrowings", contentPane);
        sceneTitle.setText("Borrowings");
    }


    @FXML
    private void UserProfile() {
        loadFXMLtoAnchorPane("ProfileScene", contentPane);
    }

    public void Logout(ActionEvent actionEvent) {
        loadNewScene("LoginScene", actionEvent);
    }
}