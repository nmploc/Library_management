package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
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

    @FXML
    private VBox pieChartVBox;

    @FXML
    private PieChart booksPieChart;

    @FXML
    private Button btnViewDetailsBooks;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseHelper.connectToDatabase();

        // Initialize table columns
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        totalBooksColumn.setCellValueFactory(new PropertyValueFactory<>("totalBooks"));

        // Load total books and users
        loadTotalBooksAndUsers();

        // Load category book counts
        loadCategoryBookCounts();

        // Initially hide the pie chart and its title
        pieChartVBox.setVisible(false);

        // Add event handler for the "View Details" button
        btnViewDetailsBooks.setOnAction(event -> showBooksPieChart());
    }

    private void loadTotalBooksAndUsers() {
        try (Connection connection = DatabaseHelper.getConnection()) {
            String bookQuery = "SELECT COUNT(*) FROM documents";
            String userQuery = "SELECT COUNT(*) FROM readers";

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
                    labelTotalUsers.setText("Total readers: " + numOfUsers);
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

            ObservableList<CategoryBookCount> categoryBookCounts = FXCollections.observableArrayList();

            while (resultSet.next()) {
                String categoryName = resultSet.getString("categoryName");
                int totalBooks = resultSet.getInt("totalBooks");
                categoryBookCounts.add(new CategoryBookCount(categoryName, totalBooks));
            }

            categoryTable.setItems(categoryBookCounts);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showBooksPieChart() {
        // Make the pie chart and its title visible
        pieChartVBox.setVisible(true);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Add the data for the pie chart
        for (CategoryBookCount categoryBookCount : categoryTable.getItems()) {
            pieChartData.add(new PieChart.Data(categoryBookCount.getCategoryName(), categoryBookCount.getTotalBooks()));
        }

        // Set the data for the pie chart
        booksPieChart.setData(pieChartData);
    }
}
