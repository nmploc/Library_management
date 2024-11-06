package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class BooksController {
    @FXML
    private TableView<Books> booksTable;

    @FXML
    private Button addBookButton, editBookButton, deleteBookButton;

    private ObservableList<Books> booksList;

    @FXML
    public void initialize() {
        // Initialize database connection and load data
        DatabaseHelper.connectToDatabase();
        loadBooksData();

        // Configure columns for the TableView (programmatically)
        TableColumn<Books, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("documentID"));

        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        TableColumn<Books, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Add columns to TableView
        booksTable.getColumns().addAll(idColumn, titleColumn, authorColumn, categoryColumn);

        // Set up button event handlers
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());
    }

    private void loadBooksData() {
        booksList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT d.documentID, d.documentName, d.authors, c.categoryName " +
                     "FROM documents d LEFT JOIN categories c ON d.categoryID = c.categoryID")) {

            while (rs.next()) {
                int id = rs.getInt("documentID");
                String name = rs.getString("documentName");
                String authors = rs.getString("authors");
                String category = rs.getString("categoryName");

                booksList.add(new Books(id, name, authors, category));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        booksTable.setItems(booksList);
    }

    @FXML
    private void handleAddBook() {
        // Add book implementation
    }

    @FXML
    private void handleEditBook() {
        // Edit book implementation
    }

    @FXML
    private void handleDeleteBook() {
        // Delete book implementation
    }
}
