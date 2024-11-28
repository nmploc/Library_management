package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
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

        // Create a new Stage (window) for showing book details
        Stage detailWindow = new Stage();
        detailWindow.setTitle("Book Details");

        // Create a VBox layout for the book details
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;"); // Add padding and center content

        // Add book details to the VBox
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

        // Generate QR code for the book details
        try {
            String tempQRCodePath = "temp_qr_code.png"; // Temporary path for the QR code image
            QRCodeGenerator.generateQRCode(selectedBook, tempQRCodePath); // Generate QR code using the class

            // Load the QR code image
            ImageView qrCodeView = new ImageView(new Image("file:" + tempQRCodePath));
            qrCodeView.setFitHeight(200); // Set QR code image size
            qrCodeView.setFitWidth(200);
            qrCodeView.setPreserveRatio(true);

            // Add the QR code image to the layout
            vbox.getChildren().add(new Label("QR Code:"));
            vbox.getChildren().add(qrCodeView);
        } catch (Exception e) {
            showAlert("QR Code Error", "Failed to generate QR code: " + e.getMessage());
        }

        // Create a scene with the VBox as the root node
        Scene detailScene = new Scene(vbox, 500, 700);
        detailWindow.setScene(detailScene);

        // Show the window
        detailWindow.show();
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
        // Create a new Stage (window) for the "Add Book" form
        Stage addBookWindow = new Stage();
        addBookWindow.setTitle("Add Book");

        // Create TextField fields for book details
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField authorField = new TextField();
        authorField.setPromptText("Author");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        // Create a "Submit" button
        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            // Handle the form submission
            if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                    categoryField.getText().isEmpty() || quantityField.getText().isEmpty()) {
                showAlert("Input Error", "Please fill in all fields.");
            } else {
                String title = titleField.getText();
                String authors = authorField.getText();
                String category = categoryField.getText();
                int quantity = Integer.parseInt(quantityField.getText());

                // Create a new book object
                Books newBook = new Books(0, title, authors, category, quantity);
                addBookToDatabase(newBook);  // Add book to database

                // Close the window after submission
                addBookWindow.close();
            }
        });

        // Create a "Cancel" button to close the window
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> addBookWindow.close());

        // Create an HBox for the buttons to appear in the same row
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(addButton, cancelButton);

        // Create a VBox layout to hold the form fields and buttons
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;"); // Padding and centering
        vbox.getChildren().addAll(
                titleField,
                authorField,
                categoryField,
                quantityField,
                buttonBox // Add the buttons in the same row
        );

        // Create a scene with the VBox and set it on the new window
        Scene scene = new Scene(vbox, 260, 320);
        addBookWindow.setScene(scene);

        // Show the window
        addBookWindow.show();
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

        // Create a new Stage (window) for editing the book
        Stage editBookWindow = new Stage();
        editBookWindow.setTitle("Edit Book");

        // Create TextFields pre-filled with the selected book's details
        TextField titleField = new TextField(selectedBook.getDocumentName());
        TextField authorField = new TextField(selectedBook.getAuthors());
        TextField categoryField = new TextField(selectedBook.getCategory());
        TextField quantityField = new TextField(String.valueOf(selectedBook.getQuantity()));

        // Create the "Save" button to apply changes
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            // Validate input fields
            if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                    categoryField.getText().isEmpty() || quantityField.getText().isEmpty()) {
                showAlert("Input Error", "Please fill in all fields.");
            } else {
                // Create a new Books object with updated information
                String title = titleField.getText();
                String authors = authorField.getText();
                String category = categoryField.getText();
                int quantity = Integer.parseInt(quantityField.getText());

                Books updatedBook = new Books(selectedBook.getDocumentID(), title, authors, category, quantity);
                updateBookInDatabase(updatedBook);  // Update book in the database

                // Close the window after saving
                editBookWindow.close();
            }
        });

        // Create a "Cancel" button to close the window without saving
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> editBookWindow.close());

        // Create a VBox layout to hold the form and buttons
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;"); // Padding and centering
        vbox.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("Category:"), categoryField,
                new Label("Quantity:"), quantityField,
                saveButton, cancelButton
        );

        // Create a scene with the VBox and set it on the new window
        Scene scene = new Scene(vbox, 400, 300);
        editBookWindow.setScene(scene);

        // Show the window
        editBookWindow.show();
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

        // Create a new Stage for the delete confirmation
        Stage deleteConfirmationWindow = new Stage();
        deleteConfirmationWindow.setTitle("Delete Confirmation");

        // Create a label with the book's name
        Label confirmationLabel = new Label("Are you sure you want to delete the following book?");
        Label bookNameLabel = new Label(selectedBook.getDocumentName());

        // Create "Delete" and "Cancel" buttons
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            deleteBookFromDatabase(selectedBook.getDocumentID());  // Delete the book
            deleteConfirmationWindow.close();  // Close the confirmation window
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> deleteConfirmationWindow.close());  // Close the window without deleting

        // Create a VBox layout to arrange the labels and buttons
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;"); // Padding and centering
        vbox.getChildren().addAll(confirmationLabel, bookNameLabel, deleteButton, cancelButton);

        // Create a Scene and set it on the window
        Scene scene = new Scene(vbox, 300, 200);
        deleteConfirmationWindow.setScene(scene);

        // Show the delete confirmation window
        deleteConfirmationWindow.show();
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
        // Create a new TableView
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

        // Create a new Stage (window)
        Stage newWindow = new Stage();
        newWindow.setTitle("Results from API");

        // Create a VBox layout to hold the TableView and Button
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

        // Set up the scene and show the new window
        Scene scene = new Scene(vbox, 800, 600);
        newWindow.setScene(scene);
        newWindow.show();
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