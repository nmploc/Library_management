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
import java.util.Optional;
import java.util.ResourceBundle;

public class BooksController extends Controller {
    @FXML
    private TableView<Books> booksTable;

    @FXML
    private Button addBookButton, editBookButton, deleteBookButton, findBookButton, findByAPiButton;

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

        // Add ISBN column
        TableColumn<Books, String> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        // Add all columns to the table
        booksTable.getColumns().addAll(idColumn, titleColumn, authorColumn, categoryColumn, quantityColumn, isbnColumn);

        // Button actions
        findBookButton.setOnAction(event -> handleFindBook());
        findByAPiButton.setOnAction(event -> handleFindByApi());
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());

        booksTable.setFocusTraversable(true);  // Ensures the table can receive key events

        // Double-click event to open book detail
        booksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openBookDetail(booksTable.getSelectionModel().getSelectedItem());
            }
        });

        // Enter key event to open book detail
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

        // Fetch additional book details from the API using ISBN
        Books apiBookDetails = APIHelper.fetchBookDetailsByISBN(selectedBook.getIsbn());

        if (apiBookDetails == null) {
            showAlert("API Error", "Failed to fetch book details from the API.");
            return;
        }

        // Create a new Stage (window) for showing book details
        Stage detailWindow = new Stage();
        detailWindow.setTitle("Book Details");

        // Main container layout
        HBox hbox = new HBox(20); // Set spacing between sections
        hbox.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        // Left side: Book details and QR code
        VBox leftVBox = new VBox(10);
        leftVBox.setStyle("-fx-alignment: top-left;");

        // Consolidate book details into a single TextArea
        TextArea bookDetailsArea = new TextArea();
        bookDetailsArea.setText(String.format(
                "Title: %s\nAuthor: %s\nCategory: %s\nQuantity: %d\nISBN: %s",
                apiBookDetails.getDocumentName(),
                apiBookDetails.getAuthors(),
                apiBookDetails.getCategory(),
                selectedBook.getQuantity(),
                apiBookDetails.getIsbn()
        ));
        bookDetailsArea.setWrapText(true);
        bookDetailsArea.setEditable(false);
        bookDetailsArea.setPrefHeight(150);
        bookDetailsArea.setPrefWidth(250);
        bookDetailsArea.getStyleClass().add("content-area"); // Apply CSS styling
        leftVBox.getChildren().add(bookDetailsArea);

        // Add QR code section
        VBox qrVBox = new VBox(10);
        try {
            String tempQRCodePath = "temp_qr_code.png";
            QRCodeGenerator.generateQRCode(apiBookDetails, tempQRCodePath);

            ImageView qrCodeView = new ImageView(new Image("file:" + tempQRCodePath));
            qrCodeView.setFitHeight(160);
            qrCodeView.setFitWidth(160);
            qrCodeView.setPreserveRatio(true);

            qrVBox.getChildren().add(new Label("QR Code:"));
            qrVBox.getChildren().add(qrCodeView);
        } catch (Exception e) {
            qrVBox.getChildren().add(new Label("QR Code unavailable."));
            showAlert("QR Code Error", "Failed to generate QR code: " + e.getMessage());
        }
        leftVBox.getChildren().add(qrVBox);

        // Center: Description
        VBox descriptionVBox = new VBox(10);
        TextArea descriptionArea = new TextArea();
        descriptionArea.setText(apiBookDetails.getDescription() != null ? apiBookDetails.getDescription() : "No description available");
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(250);
        descriptionArea.setPrefWidth(200);
        descriptionArea.getStyleClass().add("content-area"); // Apply CSS styling
        descriptionVBox.getChildren().addAll(new Label("Description:"), descriptionArea);

        // Right side: Cover image with padding
        VBox rightVBox = new VBox(10);
        rightVBox.setStyle("-fx-padding: 28px; -fx-alignment: top-right;");

        if (apiBookDetails.getCoverImageUrl() != null && !apiBookDetails.getCoverImageUrl().isEmpty()) {
            try {
                ImageView coverImageView = new ImageView(new Image(apiBookDetails.getCoverImageUrl()));
                coverImageView.setFitHeight(300);
                coverImageView.setFitWidth(300);
                coverImageView.setPreserveRatio(true);
                rightVBox.getChildren().add(coverImageView);
            } catch (Exception e) {
                rightVBox.getChildren().add(new Label("Cover image unavailable."));
            }
        } else {
            rightVBox.getChildren().add(new Label("No cover image available."));
        }

        // Combine all sections
        hbox.getChildren().addAll(leftVBox, descriptionVBox, rightVBox);

        // Scene and window setup
        Scene detailScene = new Scene(hbox, 800, 400);
        detailScene.getStylesheets().add(getClass().getResource("/CSS/Books.css").toExternalForm()); // Add CSS stylesheet
        detailWindow.setScene(detailScene);
        detailWindow.show();
    }

    private void loadBooksData() {
        booksList = FXCollections.observableArrayList();
        String query = "SELECT d.documentID, d.documentName, d.authors, c.categoryName, d.quantity, d.isbn " +
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
                String isbn = rs.getString("isbn"); // Get the ISBN from the result set

                booksList.add(new Books(id, name, authors, category, quantity, isbn)); // Pass ISBN to the Books constructor
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
                try {
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity <= 0) {
                        showAlert("Input Error", "Quantity must be a positive number.");
                    } else {
                        String title = titleField.getText();
                        String authors = authorField.getText();
                        String category = categoryField.getText();

                        if (!DatabaseHelper.isCategoryExists(category)) {
                            showAddCategoryDialog(category, () -> {
                                // Add the new category to the database
                                DatabaseHelper.addCategoryToDatabase(category);
                                // Retry adding the book after adding the new category
                                handleAddBookRetry(title, authors, category, quantity, addBookWindow);
                            });
                        } else {
                            // Create a new book object
                            Books newBook = new Books(0, title, authors, category, quantity);
                            DatabaseHelper.addBookToDatabase(newBook);  // Add book to database
                            loadBooksData();

                            // Close the window after submission
                            addBookWindow.close();
                        }
                    }
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Quantity must be a number.");
                }
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

    private void showAddCategoryDialog(String category, Runnable onAddCategory) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Category Not Found");
        alert.setHeaderText(null);
        alert.setContentText("The category \"" + category + "\" does not exist. Would you like to add it?");

        ButtonType addNewCategoryButton = new ButtonType("Add New Category");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(addNewCategoryButton, cancelButton);

        alert.showAndWait().ifPresent(type -> {
            if (type == addNewCategoryButton) {
                onAddCategory.run();
            }
        });
    }

    private void handleAddBookRetry(String title, String authors, String category, int quantity, Stage addBookWindow) {
        // Create a new book object
        Books newBook = new Books(0, title, authors, category, quantity);
        DatabaseHelper.addBookToDatabase(newBook);  // Add book to database
        loadBooksData();

        // Close the window after submission
        addBookWindow.close();
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
                try {
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity <= 0) {
                        showAlert("Input Error", "Quantity must be a positive number.");
                    } else {
                        String title = titleField.getText();
                        String authors = authorField.getText();
                        String category = categoryField.getText();

                        if (!DatabaseHelper.isCategoryExists(category)) {
                            showAddCategoryDialog(category, () -> {
                                // Add the new category to the database
                                DatabaseHelper.addCategoryToDatabase(category);
                                // Retry updating the book after adding the new category
                                handleEditBookRetry(selectedBook, title, authors, category, quantity, editBookWindow);
                            });
                        } else {
                            // Create a new Books object with updated information
                            Books updatedBook = new Books(selectedBook.getDocumentID(), title, authors, category, quantity);
                            DatabaseHelper.updateBookInDatabase(updatedBook);  // Update book in the database
                            loadBooksData();

                            // Close the window after saving
                            editBookWindow.close();
                        }
                    }
                } catch (NumberFormatException e) {
                    showAlert("Input Error", "Quantity must be a number.");
                }
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

    private void handleEditBookRetry(Books selectedBook, String title, String authors, String category, int quantity, Stage editBookWindow) {
        // Create a new Books object with updated information
        Books updatedBook = new Books(selectedBook.getDocumentID(), title, authors, category, quantity);
        DatabaseHelper.updateBookInDatabase(updatedBook);  // Update book in the database
        loadBooksData();

        // Close the window after saving
        editBookWindow.close();
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
            DatabaseHelper.deleteBookFromDatabase(selectedBook.getDocumentID());  // Delete the book
            loadBooksData();
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

    @FXML
    private void handleFindBook() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Input Error", "Please enter a search term.");
            return;
        }

        // Search in the local database first
        ObservableList<Books> localSearchResults = DatabaseHelper.searchBooksInDatabase(searchQuery);

        if (localSearchResults.isEmpty()) {
            // If no results are found locally, prompt for API search
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("No Results Found");
            alert.setHeaderText("No books found in the local database.");
            alert.setContentText("Would you like to search for books via the API?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Call the API to search for books
                ObservableList<Books> apiBooks = APIHelper.searchBooks(searchQuery);
                if (apiBooks.isEmpty()) {
                    showAlert("No Results", "No books found via the API.");
                } else {
                    APIHelper.showBooksFromAPI(apiBooks);
                }
            }
        } else {
            // If local database has results, show them
            booksTable.setItems(localSearchResults);
        }
    }

    @FXML
    private void handleFindByApi() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Input Error", "Please enter a search term.");
            return;
        }

        // Call the API to search for books
        ObservableList<Books> apiBooks = APIHelper.searchBooks(searchQuery);
        if (apiBooks.isEmpty()) {
            showAlert("No Results", "No books found via the API.");
        } else {
            APIHelper.showBooksFromAPI(apiBooks);
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