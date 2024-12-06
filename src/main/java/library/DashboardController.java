package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
    private VBox chartVBox;

    @FXML
    private PieChart booksPieChart;

    @FXML
    private BarChart  booksBarChart;

    @FXML
    private Button btnViewDetailsBooks;

    @FXML
    private Button btnViewDetailsUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseHelper.connectToDatabase();

        // Initialize table columns
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        totalBooksColumn.setCellValueFactory(new PropertyValueFactory<>("totalBooks"));

        // Load total books and users
        loadTotalBooksAndUsers();

        // Load category book counts
        loadCategoryData();

        // Initially hide the pie chart and its title
        chartVBox.setVisible(false);

        // Add event handler for the "View Details" button
        btnViewDetailsBooks.setOnAction(event -> showBooksPieChart());
        btnViewDetailsUser.setOnAction(event -> showBooksBarChart(categoryTable.getItems()));
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

    private void loadCategoryData() {
        // Updated query to handle NULL values using COALESCE
        String query = "SELECT c.categoryName, " +
                "COALESCE(COUNT(d.documentID), 0) AS totalBooks, " +
                "COALESCE(COUNT(DISTINCT b.readerID), 0) AS borrowerCount " +
                "FROM categories c " +
                "LEFT JOIN documents d ON c.categoryID = d.categoryID " +
                "LEFT JOIN borrowings b ON d.documentID = b.documentID " +
                "GROUP BY c.categoryName";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // Create a new observable list for category data
            ObservableList<CategoryBookCount> categoryData = FXCollections.observableArrayList();

            // Iterate through the result set and populate the observable list
            while (resultSet.next()) {
                String categoryName = resultSet.getString("categoryName");
                int totalBooks = resultSet.getInt("totalBooks");
                int borrowerCount = resultSet.getInt("borrowerCount");

                // Log data for debugging purposes
                System.out.println("Category: " + categoryName + ", Total Books: " + totalBooks + ", Borrower Count: " + borrowerCount);

                // Add data to the observable list
                categoryData.add(new CategoryBookCount(categoryName, totalBooks, borrowerCount));
            }

            // Set the category data to the table view
            categoryTable.setItems(categoryData);

            // Update the bar chart
            showBooksBarChart(categoryData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showBooksBarChart(ObservableList<CategoryBookCount> categoryBookCounts) {
        chartVBox.setVisible(true);
        booksPieChart.setVisible(false);
        booksBarChart.setVisible(true);

        booksBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Category Reader Count");

        for (CategoryBookCount categoryBookCount : categoryBookCounts) {
            series.getData().add(new XYChart.Data<>(categoryBookCount.getCategoryName(), categoryBookCount.getBorrowerCount()));
        }

        booksBarChart.getData().add(series);
    }

    private void showBooksPieChart() {
        chartVBox.setVisible(true);
        booksBarChart.setVisible(false);
        booksPieChart.setVisible(true);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (CategoryBookCount categoryBookCount : categoryTable.getItems()) {
            pieChartData.add(new PieChart.Data(categoryBookCount.getCategoryName(), categoryBookCount.getTotalBooks()));
        }

        booksPieChart.setData(pieChartData);
    }
}
