package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Optional;
import java.util.concurrent.*;

public class APIHelper  {

    private static final ExecutorService executor = Executors.newFixedThreadPool(5); // Create a thread pool with 5 threads

    public static ObservableList<Books> searchBooks(String query) {
        ObservableList<Books> apiBooksList = FXCollections.observableArrayList();
        try {
            // Encode the query
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String apiKey = "AIzaSyBY-xK3OC_3yHdXZYjx32tVBsUjbBR0oU8";
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery + "&key=" + apiKey;

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                apiBooksList = parseAPIResponse(response.toString());
            } else {
                throw new RuntimeException("API Error: Response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return apiBooksList;
    }

    public static ObservableList<Books> parseAPIResponse(String jsonResponse) {
        ObservableList<Books> apiBooksList = FXCollections.observableArrayList();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.optJSONArray("items");

            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject volumeInfo = item.optJSONObject("volumeInfo");

                    String title = volumeInfo.optString("title", "Unknown");
                    String authors = volumeInfo.has("authors") ?
                            volumeInfo.getJSONArray("authors").join(", ").replace("\"", "") : "Unknown";
                    String category = volumeInfo.has("categories") ?
                            volumeInfo.getJSONArray("categories").join(", ").replace("\"", "") : "Unknown";

                    String coverImageUrl = null;
                    String highResCoverImageUrl = null;
                    if (volumeInfo.has("imageLinks")) {
                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        coverImageUrl = imageLinks.optString("medium", null); // Medium resolution
                        if (coverImageUrl == null) {
                            coverImageUrl = imageLinks.optString("thumbnail", null); // Default resolution
                        }
                        highResCoverImageUrl = imageLinks.optString("large", null); // High resolution
                    }

                    String isbn = "Unknown";
                    if (volumeInfo.has("industryIdentifiers")) {
                        JSONArray identifiers = volumeInfo.getJSONArray("industryIdentifiers");
                        for (int j = 0; j < identifiers.length(); j++) {
                            JSONObject identifier = identifiers.getJSONObject(j);
                            if (identifier.optString("type").equals("ISBN_13")) {
                                isbn = identifier.optString("identifier", "Unknown");
                                break;
                            }
                        }
                    }

                    String description = volumeInfo.optString("description", "No description available");

                    // Use the constructor with all fields if available, otherwise the constructor with fewer fields
                    if (highResCoverImageUrl != null) {
                        apiBooksList.add(new Books(0, title, authors, category, 0, coverImageUrl, highResCoverImageUrl, isbn, description));
                    } else {
                        apiBooksList.add(new Books(0, title, authors, category, 0, coverImageUrl, isbn, description));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return apiBooksList;
    }

    public static Books fetchBookDetailsByISBN(String isbn) {
        try {
            String encodedIsbn = URLEncoder.encode(isbn, StandardCharsets.UTF_8.toString());
            String apiKey = "AIzaSyBY-xK3OC_3yHdXZYjx32tVBsUjbBR0oU8";
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + encodedIsbn + "&key=" + apiKey;

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                return parseBookDetailsFromAPIResponse(response.toString());
            } else {
                throw new RuntimeException("API Error: Response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Books parseBookDetailsFromAPIResponse(String responseJson) {
        try {
            JSONObject jsonObject = new JSONObject(responseJson);
            JSONArray items = jsonObject.optJSONArray("items");

            if (items != null && items.length() > 0) {
                JSONObject item = items.getJSONObject(0);
                JSONObject volumeInfo = item.optJSONObject("volumeInfo");

                String title = volumeInfo.optString("title", "Unknown");
                String authors = volumeInfo.has("authors") ?
                        volumeInfo.getJSONArray("authors").join(", ").replace("\"", "") : "Unknown";
                String category = volumeInfo.has("categories") ?
                        volumeInfo.getJSONArray("categories").join(", ").replace("\"", "") : "Unknown";

                // Handle cover image
                String coverImageUrl = null;
                if (volumeInfo.has("imageLinks")) {
                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                    coverImageUrl = imageLinks.optString("medium", null); // Medium resolution
                    if (coverImageUrl == null) {
                        coverImageUrl = imageLinks.optString("thumbnail", null); // Use thumbnail if medium is missing
                    }
                }

                String isbn = "Unknown";
                if (volumeInfo.has("industryIdentifiers")) {
                    JSONArray identifiers = volumeInfo.getJSONArray("industryIdentifiers");
                    for (int j = 0; j < identifiers.length(); j++) {
                        JSONObject identifier = identifiers.getJSONObject(j);
                        if (identifier.optString("type").equals("ISBN_13")) {
                            isbn = identifier.optString("identifier", "Unknown");
                            break;
                        }
                    }
                }

                String description = volumeInfo.optString("description", "No description available");

                // Return a new Books object
                return new Books(0, title, authors, category, 0, coverImageUrl, isbn, description);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addBookFromAPI(Books book) {
        // Create a custom dialog to ask the user for the quantity of books
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Enter Quantity");
        dialog.setHeaderText("Please enter the quantity of books you want to add:");

        // Create a TextField for the user to enter the quantity
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        // Create a layout to hold the TextField
        VBox vbox = new VBox(10);
        vbox.getChildren().add(quantityField);

        // Set the content for the dialog
        dialog.getDialogPane().setContent(vbox);

        // Add "OK" and "Cancel" buttons to the dialog
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Wait for the user's response
        Optional<ButtonType> result = dialog.showAndWait();

        // Process when the user clicks "OK"
        if (result.isPresent() && result.get() == okButton) {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());

                // Validate the quantity
                if (quantity <= 0) {
                    showAlert("Input Error", "Please enter a valid number for the quantity.");
                    return;
                }

                // Proceed to add the book with the entered quantity
                try (Connection connection = DatabaseHelper.getInstance().getConnection()) {
                    // Check the category and add it to the database if necessary
                    String categoryQuery = "SELECT categoryID FROM categories WHERE categoryName = ?";
                    PreparedStatement checkCategoryStmt = connection.prepareStatement(categoryQuery);
                    checkCategoryStmt.setString(1, book.getCategory());
                    ResultSet categoryResult = checkCategoryStmt.executeQuery();

                    int categoryID;
                    if (categoryResult.next()) {
                        categoryID = categoryResult.getInt("categoryID");
                    } else {
                        // Add a new category if it doesn't exist
                        String insertCategoryQuery = "INSERT INTO categories (categoryName) VALUES (?)";
                        try (PreparedStatement insertCategoryStmt = connection.prepareStatement(insertCategoryQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertCategoryStmt.setString(1, book.getCategory());
                            insertCategoryStmt.executeUpdate();

                            ResultSet generatedKeys = insertCategoryStmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                categoryID = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Unable to add a new category.");
                            }
                        }
                    }

                    // Insert the book into the documents table
                    String insertDocumentQuery = "INSERT INTO documents (documentName, categoryID, authors, quantity, isbn, description) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertDocumentStmt = connection.prepareStatement(insertDocumentQuery)) {
                        insertDocumentStmt.setString(1, book.getDocumentName());
                        insertDocumentStmt.setInt(2, categoryID);
                        insertDocumentStmt.setString(3, book.getAuthors());
                        insertDocumentStmt.setInt(4, quantity);
                        insertDocumentStmt.setString(5, book.getIsbn()); // Add ISBN
                        insertDocumentStmt.setString(6, book.getDescription()); // Add description

                        insertDocumentStmt.executeUpdate();
                        showAlert("Success", "Book added to the database successfully.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "An error occurred while adding the book to the database.");
                }

            } catch (NumberFormatException e) {
                showAlert("Input Error", "Please enter a valid number for the quantity.");
            }
        }
    }

    @FXML
    public static void openBookDetailAPI(Books selectedBook) {
        StageController.openBookDetail(selectedBook);
    }

    public static void showBooksFromAPI(ObservableList<Books> apiBooksList) {
        // Create a new TableView
        TableView<Books> apiBooksTable = new TableView<>(apiBooksList);

        // Define table columns
        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        TableColumn<Books, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Books, String> isbnColumn = new TableColumn<>("ISBN");  // Column for ISBN
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));

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

        // Add columns to the table (no description column)
        apiBooksTable.getColumns().addAll(coverColumn, titleColumn, authorColumn, categoryColumn, isbnColumn);
        apiBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add event handlers for double-click and Enter key
        apiBooksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openBookDetailAPI(apiBooksTable.getSelectionModel().getSelectedItem());
            }
        });

        apiBooksTable.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                openBookDetailAPI(apiBooksTable.getSelectionModel().getSelectedItem());
            }
        });

        // Create a new Stage (window)
        Stage showFromAPI = new Stage();
        showFromAPI.setTitle("Results from API");
        Main.registerStage(showFromAPI);

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
                APIHelper.addBookFromAPI(selectedBook);
            }
        });

        // Set up the scene and show the new window
        Scene scene = new Scene(vbox, 800, 600);
        showFromAPI.setScene(scene);
        showFromAPI.show();
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
