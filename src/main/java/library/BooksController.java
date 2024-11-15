package library;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

import java.sql.*;
import java.util.Optional;

public class BooksController  {
    @FXML
    private TableView<Books> booksTable;

    @FXML
    private Button addBookButton, editBookButton, deleteBookButton, findBookButton;

    @FXML
    private TextField searchField;

    private ObservableList<Books> booksList;

    @FXML
    public void initialize() {
        loadBooksData();

        // Configure columns for TableView
        TableColumn<Books, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("documentID"));

        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        TableColumn<Books, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Books, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        booksTable.getColumns().addAll(idColumn, titleColumn, authorColumn, categoryColumn, quantityColumn);

        /*findBookButton.setGraphic(createImageView("/resources/image/FindBook_icon.png"));
        addBookButton.setGraphic(createImageView("/resources/image/AddBook_icon.png"));
        editBookButton.setGraphic(createImageView("/resources/image/EditBook_icon.png"));
        deleteBookButton.setGraphic(createImageView("/resources/image/DeleteBook_icon.png")); */

        findBookButton.setOnAction(event -> handleFindBook());
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());
    }

    private void loadBooksData() {
        booksList = FXCollections.observableArrayList();
        String query = "SELECT d.documentID, d.documentName, d.authors, c.categoryName, d.quantity " +
                "FROM documents d LEFT JOIN categories c ON d.categoryID = c.categoryID";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("documentID");
                String name = rs.getString("documentName");
                String authors = rs.getString("authors");
                String category = rs.getString("categoryName");
                int quantity = rs.getInt("quantity");

                booksList.add(new Books(id, name, authors, category, quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        booksTable.setItems(booksList);
    }

    @FXML
    private void handleAddBook() {
        Dialog<Books> dialog = new Dialog<>();
        dialog.setTitle("Add Book");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField authorField = new TextField();
        authorField.setPromptText("Author");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("Category:"), categoryField,
                new Label("Quantity:"), quantityField
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                        categoryField.getText().isEmpty() || quantityField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return null;
                }
                String title = titleField.getText();
                String authors = authorField.getText();
                String category = categoryField.getText();
                int quantity = Integer.parseInt(quantityField.getText());

                return new Books(0, title, authors, category, quantity);
            }
            return null;
        });

        Optional<Books> result = dialog.showAndWait();
        result.ifPresent(this::addBookToDatabase);
    }

    private void addBookToDatabase(Books newBook) {
        String insertQuery = "INSERT INTO documents (documentName, authors, categoryID, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, newBook.getDocumentName());
            pstmt.setString(2, newBook.getAuthors());
            pstmt.setInt(3, getCategoryIdByName(newBook.getCategory()));
            pstmt.setInt(4, newBook.getQuantity());
            pstmt.executeUpdate();
            loadBooksData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditBook() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to edit.");
            return;
        }

        Dialog<Books> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");

        ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField(selectedBook.getDocumentName());
        TextField authorField = new TextField(selectedBook.getAuthors());
        TextField categoryField = new TextField(selectedBook.getCategory());
        TextField quantityField = new TextField(String.valueOf(selectedBook.getQuantity()));

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Title:"), titleField, new Label("Author:"), authorField,
                new Label("Category:"), categoryField, new Label("Quantity:"), quantityField);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                        categoryField.getText().isEmpty() || quantityField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return null;
                }
                return new Books(selectedBook.getDocumentID(), titleField.getText(), authorField.getText(),
                        categoryField.getText(), Integer.parseInt(quantityField.getText()));
            }
            return null;
        });

        Optional<Books> result = dialog.showAndWait();
        result.ifPresent(this::updateBookInDatabase);
    }

    private void updateBookInDatabase(Books updatedBook) {
        String updateQuery = "UPDATE documents SET documentName = ?, authors = ?, categoryID = ?, quantity = ? WHERE documentID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, updatedBook.getDocumentName());
            pstmt.setString(2, updatedBook.getAuthors());
            pstmt.setInt(3, getCategoryIdByName(updatedBook.getCategory()));
            pstmt.setInt(4, updatedBook.getQuantity());
            pstmt.setInt(5, updatedBook.getDocumentID());
            pstmt.executeUpdate();
            loadBooksData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBook() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this book?");
        alert.setContentText(selectedBook.getDocumentName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteBookFromDatabase(selectedBook.getDocumentID());
        }
    }

    private void deleteBookFromDatabase(int documentID) {
        String deleteQuery = "DELETE FROM documents WHERE documentID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setInt(1, documentID);
            pstmt.executeUpdate();
            loadBooksData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCategoryIdByName(String categoryName) {
        String query = "SELECT categoryID FROM categories WHERE categoryName = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("categoryID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if category is not found
    }

    @FXML
    private void handleFindBook() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Input Error", "Please enter a search term.");
            return;
        }

        FilteredList<Books> filteredList = new FilteredList<>(booksList, book ->
                book.getDocumentName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        book.getAuthors().toLowerCase().contains(searchQuery.toLowerCase())
        );

        booksTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showAlert("No Results", "No books found matching the search term.");
        }
    }

    /*private ImageView createImageView(String imagePath) {
        Image image = new Image(getClass().getResourceAsStream(imagePath)); // Path relative to the resource folder
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20); // Adjust size
        imageView.setFitWidth(20);  // Adjust size
        return imageView;
    }*/

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
