package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class BorrowingController extends Controller {  // Extend Controller

    @FXML
    private TableView<Borrowing> borrowingsTable;

    @FXML
    private Button addBorrowingButton, editBorrowingButton, deleteBorrowingButton, findBorrowingButton;

    @FXML
    private TextField searchField;

    private ObservableList<Borrowing> borrowingsList;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadBorrowingsData();

        // Configure columns for TableView
        TableColumn<Borrowing, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("borrowingID"));

        TableColumn<Borrowing, String> readerColumn = new TableColumn<>("Reader");
        readerColumn.setCellValueFactory(new PropertyValueFactory<>("readerName"));

        TableColumn<Borrowing, String> documentColumn = new TableColumn<>("Document");
        documentColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Borrowing, String> borrowDateColumn = new TableColumn<>("Borrow Date");
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));

        TableColumn<Borrowing, String> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Borrowing, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("borrowingStatus"));

        borrowingsTable.getColumns().addAll(idColumn, readerColumn, documentColumn, borrowDateColumn, dueDateColumn, statusColumn);

        findBorrowingButton.setOnAction(event -> handleFindBorrowing());
        addBorrowingButton.setOnAction(event -> handleAddBorrowing());
        editBorrowingButton.setOnAction(event -> handleEditBorrowing());
        deleteBorrowingButton.setOnAction(event -> handleDeleteBorrowing());
    }

    private void loadBorrowingsData() {
        borrowingsList = FXCollections.observableArrayList();
        String query = "SELECT b.borrowingID, r.readerName, d.documentName, b.borrowDate, b.dueDate, b.borrowingStatus " +
                "FROM borrowings b " +
                "JOIN readers r ON b.readerID = r.readerID " +
                "JOIN documents d ON b.documentID = d.documentID";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("borrowingID");
                String readerName = rs.getString("readerName");
                String documentName = rs.getString("documentName");
                String borrowDate = rs.getString("borrowDate");
                String dueDate = rs.getString("dueDate");
                String status = rs.getString("borrowingStatus");

                borrowingsList.add(new Borrowing(id, readerName, documentName, borrowDate, dueDate, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        borrowingsTable.setItems(borrowingsList);
    }

    @FXML
    private void handleAddBorrowing() {
        Stage addBorrowingWindow = new Stage();
        Main.registerStage(addBorrowingWindow);
        addBorrowingWindow.setTitle("Add Borrowing");

        // Reader Search Field
        TextField readerSearchField = new TextField();
        readerSearchField.setPromptText("Search Reader");
        readerSearchField.setPrefWidth(300);

        // Reader ListView
        ListView<String> readerListView = new ListView<>();
        readerListView.setPrefWidth(300);
        readerListView.setVisible(false); // Initially hidden
        readerListView.setMaxHeight(150);
        readerListView.setTranslateY(-40);

        // Add listener to search and update ListView
        readerSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateReaderListView(newValue, readerListView);
                readerListView.setVisible(true); // Show list when results exist
            } else {
                readerListView.setVisible(false); // Hide list when search field is empty
            }
        });

        // Handle selection in Reader ListView
        readerListView.setOnMouseClicked(event -> {
            String selectedReader = readerListView.getSelectionModel().getSelectedItem();
            if (selectedReader != null) {
                readerSearchField.setText(selectedReader); // Set selected reader in the search field
                readerListView.setVisible(false); // Hide list after selection
            }
        });

        // Document Search Field
        TextField documentSearchField = new TextField();
        documentSearchField.setPromptText("Search Document");
        documentSearchField.setPrefWidth(300);

        ListView<String> documentListView = new ListView<>();
        documentListView.setPrefWidth(300);
        documentListView.setVisible(false);
        documentListView.setMaxHeight(150);
        documentListView.setTranslateY(50);

        // Add listener to search and update Document ListView
        documentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateDocumentListView(newValue, documentListView);
                documentListView.setVisible(true);
            } else {
                documentListView.setVisible(false);
            }
        });

        // Handle selection in Document ListView
        documentListView.setOnMouseClicked(event -> {
            String selectedDocument = documentListView.getSelectionModel().getSelectedItem();
            if (selectedDocument != null) {
                documentSearchField.setText(selectedDocument);
                documentListView.setVisible(false);
            }
        });

        // Borrow Date
        DatePicker borrowDatePicker = new DatePicker();
        borrowDatePicker.setPromptText("Borrow Date");
        borrowDatePicker.setPrefWidth(300);

        // Due Date
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Due Date");
        dueDatePicker.setPrefWidth(300);

        // Buttons
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");

        addButton.setPrefWidth(100);
        cancelButton.setPrefWidth(100);

        addButton.setOnAction(event -> {
            String readerName = readerSearchField.getText();
            if (readerName.isEmpty() || !isReaderExists(readerName)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Reader Not Found");
                alert.setHeaderText(null);
                alert.setContentText("The reader name is not found in the database. Please check again.");
                alert.showAndWait();
                return;
            }

            if (readerSearchField.getText().isEmpty() || documentSearchField.getText().isEmpty() ||
                    borrowDatePicker.getValue() == null || dueDatePicker.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in all required fields!");
                alert.showAndWait();
                return;
            }

            if (borrowDatePicker.getValue().isAfter(dueDatePicker.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Date Error");
                alert.setHeaderText(null);
                alert.setContentText("Borrow date cannot be after the due date!");
                alert.showAndWait();
                return;
            }

            String documentName = documentSearchField.getText();
            String borrowDate = borrowDatePicker.getValue().toString();
            String dueDate = dueDatePicker.getValue().toString();

            Borrowing newBorrowing = new Borrowing(0, readerName, documentName, borrowDate, dueDate, "borrowing");

            addBorrowingToDatabase(newBorrowing);
            addBorrowingWindow.close();
        });

        cancelButton.setOnAction(event -> addBorrowingWindow.close());

        // Button Layout
        HBox buttonBox = new HBox(10, addButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // StackPane Layout
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        // VBox for the form
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Search Reader:"), readerSearchField,
                new Label("Search Document:"), documentSearchField,
                new Label("Borrow Date:"), borrowDatePicker,
                new Label("Due Date:"), dueDatePicker,
                buttonBox
        );

        stackPane.getChildren().addAll(vbox, readerListView, documentListView);

        // Scene
        Scene scene = new Scene(stackPane, 400, 400);
        addBorrowingWindow.setScene(scene);
        addBorrowingWindow.show();
    }

    private void updateReaderListView(String searchText, ListView<String> listView) {
        listView.getItems().clear(); // Clear previous items
        String query = "SELECT readerName FROM readers WHERE readerName LIKE ? OR fullName LIKE ?"; // Query for readerName and fullName

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchText + "%"); // Search by readerName
            pstmt.setString(2, "%" + searchText + "%"); // Search by fullName
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                listView.getItems().add(rs.getString("readerName")); // Add readerName to ListView
            }

            if (listView.getItems().isEmpty()) {
                listView.setVisible(false); // Hide ListView if no results
            } else {
                listView.setVisible(true); // Show ListView if results exist
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isReaderExists(String readerName) {
        String query = "SELECT COUNT(*) FROM readers WHERE readerName = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, readerName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Nếu có ít nhất 1 người mượn với tên này
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateDocumentListView(String searchText, ListView<String> listView) {
        listView.getItems().clear();
        String query = "SELECT d.documentName FROM documents d " +
                "LEFT JOIN categories c ON d.categoryID = c.categoryID " +
                "WHERE d.documentName LIKE ? OR d.authors LIKE ? OR c.categoryName LIKE ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, "%" + searchText + "%");
            pstmt.setString(3, "%" + searchText + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listView.getItems().add(rs.getString("documentName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addBorrowingToDatabase(Borrowing newBorrowing) {
        String checkQuantityQuery = "SELECT quantity FROM documents WHERE documentName = ?";
        String insertQuery = "INSERT INTO borrowings (readerID, documentID, borrowDate, dueDate, borrowingStatus) " +
                "VALUES ((SELECT readerID FROM readers WHERE readerName = ?), " +
                "(SELECT documentID FROM documents WHERE documentName = ?), ?, ?, ?)";
        String updateQuery = "UPDATE documents SET quantity = quantity - 1 WHERE documentID = " +
                "(SELECT documentID FROM documents WHERE documentName = ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuantityQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

            // Check the quantity of the document
            checkStmt.setString(1, newBorrowing.getDocumentName());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int quantity = rs.getInt("quantity");
                if (quantity == 0) {
                    showAlert("Out of Stock", "Cuốn sách đã được mượn hết");
                    return;
                }
            } else {
                showAlert("Error", "Document not found.");
                return;
            }

            // Set values for the insert statement
            insertStmt.setString(1, newBorrowing.getReaderName());
            insertStmt.setString(2, newBorrowing.getDocumentName());
            insertStmt.setString(3, newBorrowing.getBorrowDate());
            insertStmt.setString(4, newBorrowing.getDueDate());
            insertStmt.setString(5, newBorrowing.getBorrowingStatus());

            // Execute insert statement to add borrowing record
            insertStmt.executeUpdate();

            // Set value for the update statement (book name to reduce quantity)
            updateStmt.setString(1, newBorrowing.getDocumentName());

            // Execute update statement to reduce book quantity by 1
            updateStmt.executeUpdate();

            loadBorrowingsData(); // Reload the TableView
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to add borrowing to the database.");
        }
    }

    @FXML
    private void handleEditBorrowing() {
        Borrowing selectedBorrowing = borrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert("No Selection", "Please select a borrowing to edit.");
            return;
        }

        // Check if the borrowing status is already 'returned'
        if ("returned".equals(selectedBorrowing.getBorrowingStatus())) {
            showAlert("Edit Not Allowed", "This borrowing has already been returned and cannot be edited.");
            return;
        }

        // Create a new Stage for editing
        Stage editBorrowingWindow = new Stage();
        Main.registerStage(editBorrowingWindow);
        editBorrowingWindow.setTitle("Edit Borrowing");

        // Reader Name
        TextField readerField = new TextField(selectedBorrowing.getReaderName());
        readerField.setPrefWidth(300);

        // Document Name Search Field
        TextField documentSearchField = new TextField(selectedBorrowing.getDocumentName());
        documentSearchField.setPromptText("Search Document");
        documentSearchField.setPrefWidth(300);

        // Document List View
        ListView<String> documentListView = new ListView<>();
        documentListView.setPrefWidth(300);
        documentListView.setVisible(false); // Initially hidden
        documentListView.setMaxHeight(100);
        documentListView.setTranslateY(50); // Slightly shifted down

        // Add listener to update ListView dynamically
        documentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateDocumentListView(newValue, documentListView); // Dynamic search implementation
                documentListView.setVisible(true);
            } else {
                documentListView.setVisible(false);
            }
        });

        // Handle selection from ListView
        documentListView.setOnMouseClicked(event -> {
            String selectedDocument = documentListView.getSelectionModel().getSelectedItem();
            if (selectedDocument != null) {
                documentSearchField.setText(selectedDocument);
                documentListView.setVisible(false);
            }
        });

        // Borrow Date
        DatePicker borrowDatePicker = new DatePicker(LocalDate.parse(selectedBorrowing.getBorrowDate()));
        borrowDatePicker.setPrefWidth(300);

        // Due Date
        DatePicker dueDatePicker = new DatePicker(LocalDate.parse(selectedBorrowing.getDueDate()));
        dueDatePicker.setPrefWidth(300);

        // Borrowing Status Dropdown
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("borrowing", "returned", "late", "lost");
        statusComboBox.setValue(selectedBorrowing.getBorrowingStatus());
        statusComboBox.setPrefWidth(300);

        // Disable edit fields if status is 'returned'
        if ("returned".equals(selectedBorrowing.getBorrowingStatus())) {
            readerField.setDisable(true);
            documentSearchField.setDisable(true);
            borrowDatePicker.setDisable(true);
            dueDatePicker.setDisable(true);
            statusComboBox.setDisable(true);
        }

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setPrefWidth(100);
        cancelButton.setPrefWidth(100);

        // Save button action
        saveButton.setOnAction(event -> {
            if (readerField.getText().isEmpty() || documentSearchField.getText().isEmpty() ||
                    borrowDatePicker.getValue() == null || dueDatePicker.getValue() == null ||
                    statusComboBox.getValue() == null) {
                showAlert("Input Error", "Please fill in all required fields!");
                return;
            }

            if (borrowDatePicker.getValue().isAfter(dueDatePicker.getValue())) {
                showAlert("Date Error", "Borrow date cannot be after the due date!");
                return;
            }

            // Update Borrowing object
            Borrowing updatedBorrowing = new Borrowing(
                    selectedBorrowing.getBorrowingID(),
                    readerField.getText(),
                    documentSearchField.getText(),
                    borrowDatePicker.getValue().toString(),
                    dueDatePicker.getValue().toString(),
                    statusComboBox.getValue()
            );

            // Update database
            updateBorrowingInDatabase(updatedBorrowing);

            // Close the window
            editBorrowingWindow.close();
        });

        // Cancel button action
        cancelButton.setOnAction(event -> editBorrowingWindow.close());

        // Button layout
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Reader Name:"), readerField,
                new Label("Search Document:"), documentSearchField,
                new Label("Borrow Date:"), borrowDatePicker,
                new Label("Due Date:"), dueDatePicker,
                new Label("Borrowing Status:"), statusComboBox,
                buttonBox
        );

        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        stackPane.getChildren().addAll(vbox, documentListView); // Overlay ListView and VBox

        // Scene
        Scene scene = new Scene(stackPane, 400, 400); // Adjust height for the new field
        editBorrowingWindow.setScene(scene);
        editBorrowingWindow.show();
    }

    private void updateBorrowingInDatabase(Borrowing updatedBorrowing) {

        String updateBorrowingQuery = "UPDATE borrowings SET " +
                "readerID = (SELECT readerID FROM readers WHERE readerName = ?), " +
                "documentID = (SELECT documentID FROM documents WHERE documentName = ?), " +
                "borrowDate = ?, " +
                "dueDate = ?, " +
                "borrowingStatus = ? " +
                "WHERE borrowingID = ?";

        String updateQuantityQuery = "UPDATE documents SET quantity = quantity + 1 WHERE documentID = (SELECT documentID FROM documents WHERE documentName = ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateBorrowingQuery);
             PreparedStatement quantityPstmt = conn.prepareStatement(updateQuantityQuery)) {

            // Set parameters for the borrowing update query
            pstmt.setString(1, updatedBorrowing.getReaderName());
            pstmt.setString(2, updatedBorrowing.getDocumentName());
            pstmt.setString(3, updatedBorrowing.getBorrowDate());
            pstmt.setString(4, updatedBorrowing.getDueDate());
            pstmt.setString(5, updatedBorrowing.getBorrowingStatus());
            pstmt.setInt(6, updatedBorrowing.getBorrowingID());

            // Execute the update statement for the borrowing
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Borrowing updated successfully.");
            } else {
                System.out.println("No borrowing was updated.");
            }

            // If the status is 'returned', update the document quantity
            if ("returned".equals(updatedBorrowing.getBorrowingStatus())) {
                quantityPstmt.setString(1, updatedBorrowing.getDocumentName());
                int quantityRowsAffected = quantityPstmt.executeUpdate();
                if (quantityRowsAffected > 0) {
                    System.out.println("Document quantity updated successfully.");
                } else {
                    System.out.println("Failed to update document quantity.");
                }
            }

            // Refresh the table data
            loadBorrowingsData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update the borrowing.");
        }
    }

    @FXML
    private void handleDeleteBorrowing() {
        Borrowing selectedBorrowing = borrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert("No Selection", "Please select a borrowing to delete.");
            return;
        }

        // Create a new Stage for the confirmation dialog
        Stage confirmationWindow = new Stage();
        Main.registerStage(confirmationWindow);
        confirmationWindow.setTitle("Confirm Deletion");

        // Create the confirmation message
        Label confirmationLabel = new Label("Are you sure you want to delete this borrowing?");

        // Buttons for Yes and No
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setPrefWidth(100);
        noButton.setPrefWidth(100);

        // Yes button action - Perform the deletion
        yesButton.setOnAction(event -> {
            deleteBorrowingInDatabase(selectedBorrowing); // Call the method to handle deletion
            confirmationWindow.close();
        });

        // No button action - Close the confirmation window without doing anything
        noButton.setOnAction(event -> confirmationWindow.close());

        // Button Layout
        HBox buttonBox = new HBox(10, yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout for the confirmation window
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(confirmationLabel, buttonBox);
        vbox.setStyle("-fx-padding: 20px;");

        // Scene for the confirmation window
        Scene scene = new Scene(vbox, 300, 150);
        confirmationWindow.setScene(scene);
        confirmationWindow.show();
    }

    private void deleteBorrowingInDatabase(Borrowing selectedBorrowing) {
        // SQL query to delete the borrowing from the borrowings table
        String deleteQuery = "DELETE FROM borrowings WHERE borrowingID = ?";

        // SQL query to get the status of the borrowing
        String statusQuery = "SELECT borrowingStatus FROM borrowings WHERE borrowingID = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
             PreparedStatement statusStmt = conn.prepareStatement(statusQuery)) {

            // Get the borrowing status
            statusStmt.setInt(1, selectedBorrowing.getBorrowingID());
            ResultSet rs = statusStmt.executeQuery();

            if (rs.next()) {
                String borrowingStatus = rs.getString("borrowingStatus");

                // Check if the status is not 'returned'
                if (!"returned".equals(borrowingStatus)) {
                    showAlert("Cannot Delete", "The borrowing cannot be deleted because it has not been returned yet.");
                    return;  // Exit the method if the status is not 'returned'
                }

                // Delete the borrowing record
                deleteStmt.setInt(1, selectedBorrowing.getBorrowingID());
                int deletedRows = deleteStmt.executeUpdate();

                if (deletedRows > 0) {
                    showAlert("Success", "Borrowing deleted successfully.");
                    loadBorrowingsData(); // Reload the TableView
                } else {
                    showAlert("Error", "Failed to delete borrowing from the database.");
                }
            } else {
                showAlert("Error", "Borrowing not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to delete the borrowing.");
        }
    }

    @FXML
    private void handleFindBorrowing() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Borrowing> filteredList = FXCollections.observableArrayList();

        String query = "SELECT b.borrowingID, r.readerName, d.documentName, b.borrowDate, b.dueDate, b.borrowingStatus " +
                "FROM borrowings b " +
                "JOIN readers r ON b.readerID = r.readerID " +
                "JOIN documents d ON b.documentID = d.documentID " +
                "WHERE r.readerName LIKE ? OR d.documentName LIKE ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, "%" + searchText + "%");

            ResultSet rs = pstmt.executeQuery();
            filteredList.clear();
            while (rs.next()) {
                int id = rs.getInt("borrowingID");
                String readerName = rs.getString("readerName");
                String documentName = rs.getString("documentName");
                String borrowDate = rs.getString("borrowDate");
                String dueDate = rs.getString("dueDate");
                String status = rs.getString("borrowingStatus");

                filteredList.add(new Borrowing(id, readerName, documentName, borrowDate, dueDate, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        borrowingsTable.setItems(filteredList);
    }
}
