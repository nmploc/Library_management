package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class BooksController extends Controller {
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

        // Configure columns for the TableView
        TableColumn<Books, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("documentID"));

        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        booksTable.getColumns().addAll(idColumn, titleColumn, authorColumn);

        // Set up button event handlers
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());
    }

    private void loadBooksData() {
        booksList = FXCollections.observableArrayList();
        // Load data from the database
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM documents")) {

            while (rs.next()) {
                int id = rs.getInt("documentID");
                String name = rs.getString("documentName");
                String authors = rs.getString("authors");
                int categoryId = rs.getInt("categoryID");

                booksList.add(new Books(id, name, categoryId, authors));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        booksTable.setItems(booksList);
    }

    @FXML
    private void handleAddBook() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Book");
        dialog.setHeaderText("Enter the book details");

        // Get input for title
        dialog.setContentText("Enter Book Title:");
        Optional<String> title = dialog.showAndWait();

        // Get input for author
        dialog.setContentText("Enter Author Name:");
        Optional<String> author = dialog.showAndWait();

        if (title.isPresent() && author.isPresent()) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "INSERT INTO documents (documentName, authors) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, title.get());
                stmt.setString(2, author.get());
                stmt.executeUpdate();

                // Refresh data in TableView
                loadBooksData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditBook() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            System.out.println("No book selected for editing.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedBook.getDocumentName());
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Edit the book details");

        // Edit book title
        dialog.setContentText("Edit Book Title:");
        Optional<String> title = dialog.showAndWait();

        // Edit author name
        dialog.setContentText("Edit Author Name:");
        Optional<String> author = dialog.showAndWait();

        if (title.isPresent() && author.isPresent()) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "UPDATE documents SET documentName = ?, authors = ? WHERE documentID = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, title.get());
                stmt.setString(2, author.get());
                stmt.setInt(3, selectedBook.getDocumentID());
                stmt.executeUpdate();

                // Refresh data in TableView
                loadBooksData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteBook() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                String query = "DELETE FROM documents WHERE documentID = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedBook.getDocumentID());
                stmt.executeUpdate();

                // Remove from TableView and database
                booksList.remove(selectedBook);
                System.out.println("Deleted book: " + selectedBook.getDocumentName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
