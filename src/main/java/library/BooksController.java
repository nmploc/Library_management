package library;

import javafx.collections.FXCollections;
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

import java.sql.*;
import java.util.Optional;

public class BooksController {
    @FXML
    private TableView<Books> booksTable;

    @FXML
    private Button addBookButton, editBookButton, deleteBookButton;

    private ObservableList<Books> booksList;

    @FXML
    public void initialize() {
        loadBooksData();

        // Cấu hình các cột cho TableView
        TableColumn<Books, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("documentID"));

        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        TableColumn<Books, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Thêm các cột vào TableView
        booksTable.getColumns().addAll(idColumn, titleColumn, authorColumn, categoryColumn);

        // Thiết lập các sự kiện cho nút
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());
    }

    private void loadBooksData() {
        booksList = FXCollections.observableArrayList();
        String query = "SELECT d.documentID, d.documentName, d.authors, c.categoryName " +
                "FROM documents d LEFT JOIN categories c ON d.categoryID = c.categoryID";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

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

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Title:"), titleField, new Label("Author:"), authorField, new Label("Category:"), categoryField);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // Kiểm tra thông tin đầu vào
                if (titleField.getText().isEmpty() || authorField.getText().isEmpty() || categoryField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Input Error");
                    alert.setHeaderText("Missing Information");
                    alert.setContentText("Please fill in all fields.");
                    alert.showAndWait();
                    return null; // Ngăn không cho thêm
                }
                String title = titleField.getText();
                String authors = authorField.getText();
                String category = categoryField.getText();
                return new Books(0, title, authors, category);
            }
            return null;
        });

        Optional<Books> result = dialog.showAndWait();
        result.ifPresent(newBook -> {
            addBookToDatabase(newBook);
        });
    }

    private void addBookToDatabase(Books newBook) {
        String insertQuery = "INSERT INTO documents (documentName, authors, categoryID) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, newBook.getDocumentName());
            pstmt.setString(2, newBook.getAuthors());
            int categoryId = getCategoryIdByName(newBook.getCategory());

            if (categoryId == -1) {
                System.err.println("Category not found: " + newBook.getCategory());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Input Error");
                alert.setHeaderText("Invalid Category");
                alert.setContentText("The specified category does not exist.");
                alert.showAndWait();
                return;
            }

            pstmt.setInt(3, categoryId);
            pstmt.executeUpdate();
            loadBooksData(); // Tải lại dữ liệu sau khi thêm
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditBook() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Book Selected");
            alert.setContentText("Please select a book to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<Books> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");

        ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField(selectedBook.getDocumentName());
        TextField authorField = new TextField(selectedBook.getAuthors());
        TextField categoryField = new TextField(selectedBook.getCategory());

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Title:"), titleField, new Label("Author:"), authorField, new Label("Category:"), categoryField);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                // Kiểm tra thông tin đầu vào
                if (titleField.getText().isEmpty() || authorField.getText().isEmpty() || categoryField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Input Error");
                    alert.setHeaderText("Missing Information");
                    alert.setContentText("Please fill in all fields.");
                    alert.showAndWait();
                    return null; // Ngăn không cho cập nhật
                }
                return new Books(selectedBook.getDocumentID(), titleField.getText(), authorField.getText(), categoryField.getText());
            }
            return null;
        });

        Optional<Books> result = dialog.showAndWait();
        result.ifPresent(updatedBook -> {
            updateBookInDatabase(updatedBook);
        });
    }

    private void updateBookInDatabase(Books updatedBook) {
        String updateQuery = "UPDATE documents SET documentName = ?, authors = ?, categoryID = ? WHERE documentID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, updatedBook.getDocumentName());
            pstmt.setString(2, updatedBook.getAuthors());
            pstmt.setInt(3, getCategoryIdByName(updatedBook.getCategory()));
            pstmt.setInt(4, updatedBook.getDocumentID());
            pstmt.executeUpdate();
            loadBooksData(); // Tải lại dữ liệu sau khi cập nhật
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBook() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Book Selected");
            alert.setContentText("Please select a book to delete.");
            alert.showAndWait();
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
            loadBooksData(); // Tải lại dữ liệu sau khi xóa
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để lấy ID danh mục theo tên
    private int getCategoryIdByName(String categoryName) {
        String selectQuery = "SELECT categoryID FROM categories WHERE categoryName = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("categoryID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu không tìm thấy
    }
}