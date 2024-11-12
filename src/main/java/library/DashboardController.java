package library;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardController extends Controller {
    @FXML
    private VBox VBoxTotalBooks;

    @FXML
    private VBox VBoxTotalUsers;

    @FXML
    private Label labelTotalBooks;

    @FXML
    private Label labelTotalUsers;

    @FXML
    private TableView<CategoryBookCount> categoryTable;

    @FXML
    private TableColumn<CategoryBookCount, String> categoryColumn;

    @FXML
    private TableColumn<CategoryBookCount, Integer> totalBooksColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseHelper.connectToDatabase();

        // Initialize table columns
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        totalBooksColumn.setCellValueFactory(new PropertyValueFactory<>("totalBooks"));

        loadTotalBooksAndUsers();
        loadCategoryBookCounts();
    }

    private void loadTotalBooksAndUsers() {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String bookQuery = "SELECT COUNT(*) FROM documents";
            String userQuery = "SELECT COUNT(*) FROM users";

            // Query total books
            try (PreparedStatement bookStatement = connection.prepareStatement(bookQuery);
                 ResultSet bookResult = bookStatement.executeQuery()) {
                if (bookResult.next()) {
                    int numOfBooks = bookResult.getInt(1);
                    labelTotalBooks.setText("Total books: " + numOfBooks);
                }
            }

            // Query total users
            try (PreparedStatement userStatement = connection.prepareStatement(userQuery);
                 ResultSet userResult = userStatement.executeQuery()) {
                if (userResult.next()) {
                    int numOfUsers = userResult.getInt(1);
                    labelTotalUsers.setText("Total users: " + numOfUsers);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            labelTotalBooks.setText("Error fetching data");
            labelTotalUsers.setText("Error fetching data");
        }
    }

    private void loadCategoryBookCounts() {
        String categoryBookCountQuery = "SELECT c.categoryName, COUNT(d.documentID) AS totalBooks " +
                "FROM categories c LEFT JOIN documents d ON c.categoryID = d.categoryID " +
                "GROUP BY c.categoryName";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(categoryBookCountQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String categoryName = resultSet.getString("categoryName");
                int totalBooks = resultSet.getInt("totalBooks");
                categoryTable.getItems().add(new CategoryBookCount(categoryName, totalBooks));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
