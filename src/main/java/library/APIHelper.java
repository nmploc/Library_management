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

public class APIHelper {

    // Changed the thread pool size to 5
    private static final ExecutorService executor = Executors.newFixedThreadPool(5); // Create a thread pool with 5 threads

    // Method to fetch books using a single query (updated with URL encoding fix)
    public static ObservableList<Books> searchBooks(String query) {
        ObservableList<Books> apiBooksList = FXCollections.observableArrayList();
        try {
            // Encode the query
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery;

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

    // Method to parse the API response and create Books objects (unchanged)
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
                    if (volumeInfo.has("imageLinks")) {
                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        // Prefer high-quality images if available
                        coverImageUrl = imageLinks.optString("medium", null); // Medium-resolution
                        if (coverImageUrl == null) {
                            coverImageUrl = imageLinks.optString("thumbnail", null); // Default resolution
                        }
                    }

                    apiBooksList.add(new Books(0, title, authors, category, 0, coverImageUrl));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return apiBooksList;
    }

    public static void addBookFromAPI(Books book) {
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

    @FXML
    public static void openBookDetailAPI(Books selectedBook) {
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
                openBookDetailAPI(apiBooksTable.getSelectionModel().getSelectedItem());
            }
        });

        apiBooksTable.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                openBookDetailAPI(apiBooksTable.getSelectionModel().getSelectedItem());
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
                APIHelper.addBookFromAPI(selectedBook);
            }
        });

        // Set up the scene and show the new window
        Scene scene = new Scene(vbox, 800, 600);
        newWindow.setScene(scene);
        newWindow.show();
    }


    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
