package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
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
        // Create a new Stage (window) for the "Add Borrowing" form
        Stage addBorrowingWindow = new Stage();
        addBorrowingWindow.setTitle("Add Borrowing");

        // Create TextField for reader name
        TextField readerField = new TextField();
        readerField.setPromptText("Reader Name");

        // Create ComboBox for document selection
        ComboBox<String> documentComboBox = new ComboBox<>();
        documentComboBox.setPromptText("Select Document");

        // Create search field to search for documents by name, author, or category
        TextField searchBookField = new TextField();
        searchBookField.setPromptText("Search by Name, Author, or Category");

        searchBookField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateDocumentComboBox(newValue, documentComboBox);
        });

        // Create DatePickers for borrow date and due date
        DatePicker borrowDatePicker = new DatePicker();
        borrowDatePicker.setPromptText("Borrow Date");

        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Due Date");

        // Create "Submit" button
        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            // Handle form submission
            if (readerField.getText().isEmpty() || documentComboBox.getValue() == null ||
                    borrowDatePicker.getValue() == null || dueDatePicker.getValue() == null) {
                // Handle invalid input: show an error or simply return
                return;
            }

            String readerName = readerField.getText();
            String documentName = documentComboBox.getValue();
            String borrowDate = borrowDatePicker.getValue().toString(); // format is YYYY-MM-DD
            String dueDate = dueDatePicker.getValue().toString(); // format is YYYY-MM-DD

            // Create a new Borrowing object
            Borrowing newBorrowing = new Borrowing(0, readerName, documentName, borrowDate, dueDate, "borrowing");

            // Add borrowing to database
            addBorrowingToDatabase(newBorrowing);

            // Close the window after submission
            addBorrowingWindow.close();
        });

        // Create "Cancel" button to close the window
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> addBorrowingWindow.close());

        // Create an HBox for the buttons to appear in the same row
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(addButton, cancelButton);

        // Create a VBox layout to hold the form fields and buttons
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        vbox.getChildren().addAll(
                new Label("Reader Name:"), readerField,
                new Label("Search Document:"), searchBookField,
                new Label("Select Document:"), documentComboBox,
                new Label("Borrow Date:"), borrowDatePicker,
                new Label("Due Date:"), dueDatePicker,
                buttonBox // Add buttons below the fields
        );

        // Create a scene with the VBox and set it on the new window
        Scene scene = new Scene(vbox, 300, 350);
        addBorrowingWindow.setScene(scene);

        // Show the window
        addBorrowingWindow.show();
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
            insertStmt.setString(3, newBorrowing.getBorrowDate() + " 00:00:00"); // Format for DATETIME
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

    private void updateDocumentComboBox(String searchText, ComboBox<String> comboBox) {
        comboBox.getItems().clear();
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
                comboBox.getItems().add(rs.getString("documentName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditBorrowing() {
        Borrowing selectedBorrowing = borrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert("No Selection", "Please select a borrowing to edit.");
            return;
        }

        Dialog<Borrowing> dialog = new Dialog<>();
        dialog.setTitle("Edit Borrowing");

        ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        TextField readerField = new TextField(selectedBorrowing.getReaderName());
        TextField documentField = new TextField(selectedBorrowing.getDocumentName());
        DatePicker borrowDatePicker = new DatePicker();
        DatePicker dueDatePicker = new DatePicker();

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Reader Name:"), readerField, new Label("Document Name:"), documentField,
                new Label("Borrow Date:"), borrowDatePicker, new Label("Due Date:"), dueDatePicker);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                return new Borrowing(selectedBorrowing.getBorrowingID(), readerField.getText(),
                        documentField.getText(), borrowDatePicker.getValue().toString(),
                        dueDatePicker.getValue().toString(), selectedBorrowing.getBorrowingStatus());
            }
            return null;
        });

        Optional<Borrowing> result = dialog.showAndWait();
        result.ifPresent(this::updateBorrowingInDatabase);
    }

    private void updateBorrowingInDatabase(Borrowing updatedBorrowing) {
        String query = "UPDATE borrowings SET readerID = (SELECT readerID FROM readers WHERE readerName = ?), " +
                "documentID = (SELECT documentID FROM documents WHERE documentName = ?), " +
                "borrowDate = ?, dueDate = ?, borrowingStatus = ? WHERE borrowingID = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedBorrowing.getReaderName());
            pstmt.setString(2, updatedBorrowing.getDocumentName());
            pstmt.setString(3, updatedBorrowing.getBorrowDate());
            pstmt.setString(4, updatedBorrowing.getDueDate());
            pstmt.setString(5, updatedBorrowing.getBorrowingStatus());
            pstmt.setInt(6, updatedBorrowing.getBorrowingID());
            pstmt.executeUpdate();
            loadBorrowingsData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBorrowing() {
        Borrowing selectedBorrowing = borrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert("No Selection", "Please select a borrowing to delete.");
            return;
        }

        // SQL query to increase the quantity of the book in the documents table
        String updateQuery = "UPDATE documents SET quantity = quantity + 1 WHERE documentID = " +
                "(SELECT documentID FROM borrowings WHERE borrowingID = ?)";

        // SQL query to delete the borrowing from the borrowings table
        String deleteQuery = "DELETE FROM borrowings WHERE borrowingID = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {

            // Update the quantity in the documents table
            updateStmt.setInt(1, selectedBorrowing.getBorrowingID());
            updateStmt.executeUpdate();

            // Delete the borrowing record
            deleteStmt.setInt(1, selectedBorrowing.getBorrowingID());
            deleteStmt.executeUpdate();

            loadBorrowingsData(); // Reload the TableView
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to delete the borrowing.");
        }
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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
