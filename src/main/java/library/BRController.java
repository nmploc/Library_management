package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class BRController {

    @FXML
    private TableView<BorrowReturn> borrowReturnTable;

    @FXML
    private Button addBorrowReturnButton, editBorrowReturnButton, deleteBorrowReturnButton;

    @FXML
    private TextField searchField;

    private ObservableList<BorrowReturn> borrowReturnList;

    @FXML
    public void initialize() {
        loadBorrowReturnData();

        // Configure columns for TableView
        TableColumn<BorrowReturn, LocalDate> borrowDateColumn = new TableColumn<>("Borrow Date");
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));

        TableColumn<BorrowReturn, Integer> bookIdColumn = new TableColumn<>("Book ID");
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<BorrowReturn, Integer> readerIdColumn = new TableColumn<>("Reader ID");
        readerIdColumn.setCellValueFactory(new PropertyValueFactory<>("readerId"));

        borrowReturnTable.getColumns().addAll(borrowDateColumn, bookIdColumn, readerIdColumn);

        addBorrowReturnButton.setOnAction(event -> handleAddBorrowReturn());
        editBorrowReturnButton.setOnAction(event -> handleEditBorrowReturn());
        deleteBorrowReturnButton.setOnAction(event -> handleDeleteBorrowReturn());
    }

    private void loadBorrowReturnData() {
        borrowReturnList = FXCollections.observableArrayList();
        String query = "SELECT borrowDate, bookId, readerId FROM borrow_return";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                LocalDate borrowDate = rs.getDate("borrowDate").toLocalDate();
                int bookId = rs.getInt("bookId");
                int readerId = rs.getInt("readerId");

                borrowReturnList.add(new BorrowReturn(borrowDate, bookId, readerId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        borrowReturnTable.setItems(borrowReturnList);
    }

    @FXML
    private void handleAddBorrowReturn() {
        Dialog<BorrowReturn> dialog = new Dialog<>();
        dialog.setTitle("Add Borrow Return");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        DatePicker borrowDateField = new DatePicker();
        borrowDateField.setPromptText("Borrow Date");

        TextField bookIdField = new TextField();
        bookIdField.setPromptText("Book ID");

        TextField readerIdField = new TextField();
        readerIdField.setPromptText("Reader ID");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Borrow Date:"), borrowDateField,
                new Label("Book ID:"), bookIdField,
                new Label("Reader ID:"), readerIdField
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (borrowDateField.getValue() == null || bookIdField.getText().isEmpty() || readerIdField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return null;
                }
                LocalDate borrowDate = borrowDateField.getValue();
                int bookId = Integer.parseInt(bookIdField.getText());
                int readerId = Integer.parseInt(readerIdField.getText());

                return new BorrowReturn(borrowDate, bookId, readerId);
            }
            return null;
        });

        Optional<BorrowReturn> result = dialog.showAndWait();
        result.ifPresent(this::addBorrowReturnToDatabase);
    }

    private void addBorrowReturnToDatabase(BorrowReturn newBorrowReturn) {
        String insertQuery = "INSERT INTO borrow_return (borrowDate, bookId, readerId) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setDate(1, Date.valueOf(newBorrowReturn.getBorrowDate()));
            pstmt.setInt(2, newBorrowReturn.getBookId());
            pstmt.setInt(3, newBorrowReturn.getReaderId());
            pstmt.executeUpdate();
            loadBorrowReturnData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditBorrowReturn() {
        BorrowReturn selectedBorrowReturn = borrowReturnTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowReturn == null) {
            showAlert("No Selection", "Please select a borrow return record to edit.");
            return;
        }

        Dialog<BorrowReturn> dialog = new Dialog<>();
        dialog.setTitle("Edit Borrow Return");

        ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        DatePicker borrowDateField = new DatePicker(selectedBorrowReturn.getBorrowDate());
        TextField bookIdField = new TextField(String.valueOf(selectedBorrowReturn.getBookId()));
        TextField readerIdField = new TextField(String.valueOf(selectedBorrowReturn.getReaderId()));

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Borrow Date:"), borrowDateField, new Label("Book ID:"), bookIdField, new Label("Reader ID:"), readerIdField);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                if (borrowDateField.getValue() == null || bookIdField.getText().isEmpty() || readerIdField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return null;
                }
                LocalDate borrowDate = borrowDateField.getValue();
                int bookId = Integer.parseInt(bookIdField.getText());
                int readerId = Integer.parseInt(readerIdField.getText());

                return new BorrowReturn(borrowDate, bookId, readerId);
            }
            return null;
        });

        Optional<BorrowReturn> result = dialog.showAndWait();
        result.ifPresent(this::updateBorrowReturnInDatabase);
    }

    private void updateBorrowReturnInDatabase(BorrowReturn updatedBorrowReturn) {
        String updateQuery = "UPDATE borrow_return SET borrowDate = ?, bookId = ?, readerId = ? WHERE borrowDate = ? AND bookId = ? AND readerId = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setDate(1, Date.valueOf(updatedBorrowReturn.getBorrowDate()));
            pstmt.setInt(2, updatedBorrowReturn.getBookId());
            pstmt.setInt(3, updatedBorrowReturn.getReaderId());
            pstmt.setDate(4, Date.valueOf(updatedBorrowReturn.getBorrowDate()));
            pstmt.setInt(5, updatedBorrowReturn.getBookId());
            pstmt.setInt(6, updatedBorrowReturn.getReaderId());
            pstmt.executeUpdate();
            loadBorrowReturnData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBorrowReturn() {
        BorrowReturn selectedBorrowReturn = borrowReturnTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowReturn == null) {
            showAlert("No Selection", "Please select a borrow return record to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this borrow return record?");
        alert.setContentText("Borrow Date: " + selectedBorrowReturn.getBorrowDate() + "\nBook ID: " + selectedBorrowReturn.getBookId() + "\nReader ID: " + selectedBorrowReturn.getReaderId());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteBorrowReturnFromDatabase(selectedBorrowReturn);
        }
    }

    private void deleteBorrowReturnFromDatabase(BorrowReturn borrowReturn) {
        String deleteQuery = "DELETE FROM borrow_return WHERE borrowDate = ? AND bookId = ? AND readerId = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setDate(1, Date.valueOf(borrowReturn.getBorrowDate()));
            pstmt.setInt(2, borrowReturn.getBookId());
            pstmt.setInt(3, borrowReturn.getReaderId());
            pstmt.executeUpdate();
            loadBorrowReturnData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleFindBorrowReturn() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Input Error", "Please enter a search term.");
            return;
        }

        FilteredList<BorrowReturn> filteredList = new FilteredList<>(borrowReturnList, borrowReturn ->
                String.valueOf(borrowReturn.getBookId()).contains(searchQuery) ||
                        String.valueOf(borrowReturn.getReaderId()).contains(searchQuery)
        );

        borrowReturnTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showAlert("No Results", "No borrow return records found matching the search term.");
        }
    }
}
