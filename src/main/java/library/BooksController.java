package library;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BooksController extends Controller {
    @FXML
    private TableView<Books> booksTable;

    @FXML
    private Button addBookButton, editBookButton, deleteBookButton, findBookButton;

    @FXML
    private TextField searchField;

    private ObservableList<Books> booksList;

    @FXML
    private AnchorPane contentPane;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        findBookButton.setOnAction(event -> handleFindBook());
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());

        booksTable.setFocusTraversable(true);  // Ensures the table can receive key events

        booksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openBookDetail(booksTable.getSelectionModel().getSelectedItem());
            }
        });

        booksTable.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                openBookDetail(booksTable.getSelectionModel().getSelectedItem());
            }
        });

    }

    @FXML
    private void openBookDetail(Books selectedBook) {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to view details.");
            return;
        }

        // Create a dialog for showing book details
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Book Details");
        dialog.setHeaderText("Details of the selected book");

        // Create a content layout with book details
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;"); // Add padding and center content

        vbox.getChildren().addAll(
                new Label("Title: " + selectedBook.getDocumentName()),
                new Label("Author: " + selectedBook.getAuthors()),
                new Label("Category: " + selectedBook.getCategory()),
                new Label("Quantity: " + selectedBook.getQuantity())
        );

        // If a cover image URL is available, display the image
        if (selectedBook.getCoverImageUrl() != null && !selectedBook.getCoverImageUrl().isEmpty()) {
            ImageView coverImageView = new ImageView(new Image(selectedBook.getCoverImageUrl()));
            coverImageView.setFitHeight(300); // Set preferred image size
            coverImageView.setFitWidth(200);
            coverImageView.setPreserveRatio(true);
            vbox.getChildren().add(coverImageView);
        }

        // Add the VBox to the dialog
        dialog.getDialogPane().setContent(vbox);

        // Set dialog size and allow resizing
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(500, 600); // Set initial size

        // Add a close button to the dialog
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Show the dialog
        dialog.showAndWait();
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

        // Search in the local database first
        ObservableList<Books> localSearchResults = searchBooksInDatabase(searchQuery);

        if (localSearchResults.isEmpty()) {
            // If no results are found locally, prompt for API search
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("No Results Found");
            alert.setHeaderText("No books found in the local database.");
            alert.setContentText("Would you like to search for books via the API?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Call the API to search for books
                ObservableList<Books> apiBooks = GoogleBooksService.searchBooks(searchQuery);
                if (apiBooks.isEmpty()) {
                    showAlert("No Results", "No books found via the API.");
                } else {
                    showBooksFromAPI(apiBooks);
                }
            }
        } else {
            // If local database has results, show them
            booksTable.setItems(localSearchResults);
        }
    }

    private ObservableList<Books> searchBooksInDatabase(String searchQuery) {
        ObservableList<Books> searchResults = FXCollections.observableArrayList();
        String query = "SELECT d.documentID, d.documentName, d.authors, c.categoryName, d.quantity " +
                "FROM documents d " +
                "LEFT JOIN categories c ON d.categoryID = c.categoryID " +
                "WHERE d.documentName LIKE ? OR d.authors LIKE ? OR c.categoryName LIKE ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Use wildcard search to find partial matches
            pstmt.setString(1, "%" + searchQuery + "%");
            pstmt.setString(2, "%" + searchQuery + "%");
            pstmt.setString(3, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Books book = new Books(
                        rs.getInt("documentID"),
                        rs.getString("documentName"),
                        rs.getString("authors"),
                        rs.getString("categoryName"),
                        rs.getInt("quantity")
                );
                searchResults.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return searchResults;
    }

    private void showBooksFromAPI(ObservableList<Books> apiBooksList) {
        TableView<Books> apiBooksTable = new TableView<>(apiBooksList);

        // Define table columns
        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        TableColumn<Books, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Books, String> coverColumn = new TableColumn<>("Cover");
        coverColumn.setCellValueFactory(new PropertyValueFactory<>("coverImageUrl"));
        coverColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(70);
            }

            @Override
            protected void updateItem(String coverImageUrl, boolean empty) {
                super.updateItem(coverImageUrl, empty);
                if (empty || coverImageUrl == null) {
                    setGraphic(null);
                } else {
                    // Prioritize high-resolution cover image
                    String highResCoverImageUrl = getTableView().getItems().get(getIndex()).getHighResCoverImageUrl();
                    String displayUrl = (highResCoverImageUrl != null) ? highResCoverImageUrl : coverImageUrl;
                    imageView.setImage(new Image(displayUrl, true));
                    setGraphic(imageView);
                }
            }
        });

        // Add columns to the table
        apiBooksTable.getColumns().addAll(coverColumn, titleColumn, authorColumn, categoryColumn);
        apiBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add event handlers for double-click and Enter key
        apiBooksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openBookDetail(apiBooksTable.getSelectionModel().getSelectedItem());
            }
        });

        apiBooksTable.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                openBookDetail(apiBooksTable.getSelectionModel().getSelectedItem());
            }
        });

        // Create dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Results from API");

        VBox vbox = new VBox(10);
        vbox.getChildren().add(apiBooksTable);

        // Add "Add to Database" button
        Button addButton = new Button("Add to Database");
        addButton.setDisable(true); // Disable initially
        vbox.getChildren().add(addButton);

        // Enable the button only when a book is selected
        apiBooksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            addButton.setDisable(newSelection == null);
        });

        // Handle "Add to Database" button action
        addButton.setOnAction(event -> {
            Books selectedBook = apiBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                addBookFromAPI(selectedBook);
            }
        });

        // Set dialog content and properties
        dialog.getDialogPane().setContent(vbox);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
    

    public void addBookFromAPI(Books book) {
        // Tạo một hộp thoại tùy chỉnh để yêu cầu người dùng nhập số lượng sách
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nhập số lượng");
        dialog.setHeaderText("Nhập số lượng sách bạn muốn thêm:");

        // Tạo một TextField để người dùng nhập số lượng
        TextField quantityField = new TextField();
        quantityField.setPromptText("Số lượng");

        // Tạo một layout để chứa TextField
        VBox vbox = new VBox(10);
        vbox.getChildren().add(quantityField);

        // Thiết lập nội dung cho hộp thoại
        dialog.getDialogPane().setContent(vbox);

        // Thêm nút "OK" và "Cancel" vào hộp thoại
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Chờ đợi phản hồi từ người dùng
        Optional<ButtonType> result = dialog.showAndWait();

        // Xử lý khi người dùng nhấn nút "OK"
        if (result.isPresent() && result.get() == okButton) {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());

                // Kiểm tra số lượng hợp lệ
                if (quantity <= 0) {
                    showAlert("Số lượng không hợp lệ", "Vui lòng nhập một số lượng hợp lệ.");
                    return;
                }

                // Tiến hành thêm sách với số lượng đã nhập
                try (Connection connection = DatabaseHelper.getConnection()) {
                    // Kiểm tra danh mục và thêm vào cơ sở dữ liệu như trước...
                    String categoryQuery = "SELECT categoryID FROM categories WHERE categoryName = ?";
                    PreparedStatement checkCategoryStmt = connection.prepareStatement(categoryQuery);
                    checkCategoryStmt.setString(1, book.getCategory());
                    ResultSet categoryResult = checkCategoryStmt.executeQuery();

                    int categoryID;
                    if (categoryResult.next()) {
                        categoryID = categoryResult.getInt("categoryID");
                    } else {
                        // Thêm danh mục mới nếu chưa tồn tại
                        String insertCategoryQuery = "INSERT INTO categories (categoryName) VALUES (?)";
                        try (PreparedStatement insertCategoryStmt = connection.prepareStatement(insertCategoryQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertCategoryStmt.setString(1, book.getCategory());
                            insertCategoryStmt.executeUpdate();

                            ResultSet generatedKeys = insertCategoryStmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                categoryID = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Không thể thêm danh mục mới.");
                            }
                        }
                    }

                    // Thêm sách vào bảng documents
                    String insertDocumentQuery = "INSERT INTO documents (documentName, categoryID, authors, quantity) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertDocumentStmt = connection.prepareStatement(insertDocumentQuery)) {
                        insertDocumentStmt.setString(1, book.getDocumentName());
                        insertDocumentStmt.setInt(2, categoryID);
                        insertDocumentStmt.setString(3, book.getAuthors());
                        insertDocumentStmt.setInt(4, quantity);

                        insertDocumentStmt.executeUpdate();
                        showAlert("Success", "Book added to the database successfully.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Lỗi cơ sở dữ liệu", "Có lỗi khi thêm sách vào cơ sở dữ liệu.");
                }

            } catch (NumberFormatException e) {
                showAlert("Lỗi nhập liệu", "Vui lòng nhập một số hợp lệ cho số lượng.");
            }
        }
    }


    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}