package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
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
    private VBox barChartVBox;

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
        pieChartVBox.setVisible(false);
        barChartVBox.setVisible(false);

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
        // Show the BarChart VBox and hide the PieChart VBox
        pieChartVBox.setVisible(false); // Hide the PieChart VBox
        barChartVBox.setVisible(true);  // Show the BarChart VBox

        booksBarChart.getData().clear(); // Clear existing data

        // Create the series for the BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Category Reader Count");

        // Add data from the categoryBookCounts list to the series
        for (CategoryBookCount categoryBookCount : categoryBookCounts) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(
                    categoryBookCount.getCategoryName(),
                    categoryBookCount.getBorrowerCount()
            );

            // Attach a label to display the value on top of each bar
            data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // Create a label to show the value
                    Label label = new Label(data.getYValue().toString());
                    label.setStyle("-fx-font-size: 12; -fx-text-fill: black;");

                    // Position the label above the bar
                    StackPane stackPane = (StackPane) newValue;
                    stackPane.getChildren().add(label);
                    label.setTranslateY(-15); // Adjust position above the bar
                }
            });

            series.getData().add(data);
        }
        // Add the series to the BarChart
        booksBarChart.getData().add(series);
        // Dynamically adjust the width and height based on the content
        double chartWidth = categoryBookCounts.size() * 100; // Adjust width based on the number of categories
        booksBarChart.setPrefWidth(chartWidth);
        booksBarChart.setPrefHeight(400);
        // Ensure the x-axis labels are readable
        CategoryAxis categoryAxis = (CategoryAxis) booksBarChart.getXAxis();
        categoryAxis.setTickLabelRotation(45); // Rotate labels for readability
        // Force layout update to make sure everything is properly sized
        booksBarChart.layout();
    }

    private void showBooksPieChart() {
        // Show the PieChart VBox and hide the BarChart VBox
        pieChartVBox.setVisible(true);  // Show the PieChart VBox
        barChartVBox.setVisible(false); // Hide the BarChart VBox
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        // Populate PieChart data from the categoryTable items
        for (CategoryBookCount categoryBookCount : categoryTable.getItems()) {
            pieChartData.add(new PieChart.Data(categoryBookCount.getCategoryName(), categoryBookCount.getTotalBooks()));
        }
        // Set the PieChart data
        booksPieChart.setData(pieChartData);
    }

}
