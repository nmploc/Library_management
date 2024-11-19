package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
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
        Dialog<Borrowing> dialog = new Dialog<>();
        dialog.setTitle("Add Borrowing");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField readerField = new TextField();
        readerField.setPromptText("Reader Name");

        TextField documentField = new TextField();
        documentField.setPromptText("Document Name");

        DatePicker borrowDatePicker = new DatePicker();
        DatePicker dueDatePicker = new DatePicker();

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Reader Name:"), readerField,
                new Label("Document Name:"), documentField,
                new Label("Borrow Date:"), borrowDatePicker,
                new Label("Due Date:"), dueDatePicker
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Borrowing(0, readerField.getText(), documentField.getText(),
                        borrowDatePicker.getValue().toString(), dueDatePicker.getValue().toString(), "Active");
            }
            return null;
        });

        Optional<Borrowing> result = dialog.showAndWait();
        result.ifPresent(this::addBorrowingToDatabase);
    }

    private void addBorrowingToDatabase(Borrowing newBorrowing) {
        String query = "INSERT INTO borrowings (readerID, documentID, borrowDate, dueDate, borrowingStatus) " +
                "VALUES ((SELECT readerID FROM readers WHERE readerName = ?), " +
                "(SELECT documentID FROM documents WHERE documentName = ?), ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newBorrowing.getReaderName());
            pstmt.setString(2, newBorrowing.getDocumentName());
            pstmt.setString(3, newBorrowing.getBorrowDate());
            pstmt.setString(4, newBorrowing.getDueDate());
            pstmt.setString(5, newBorrowing.getBorrowingStatus());
            pstmt.executeUpdate();
            loadBorrowingsData();
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this borrowing?");
        alert.setContentText("Borrowing ID: " + selectedBorrowing.getBorrowingID());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteBorrowingFromDatabase(selectedBorrowing.getBorrowingID());
        }
    }

    private void deleteBorrowingFromDatabase(int borrowingID) {
        String query = "DELETE FROM borrowings WHERE borrowingID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, borrowingID);
            pstmt.executeUpdate();
            loadBorrowingsData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFindBorrowing() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Borrowing> filteredList = FXCollections.observableArrayList();

        for (Borrowing borrowing : borrowingsList) {
            if (borrowing.getReaderName().toLowerCase().contains(searchText) ||
                    borrowing.getDocumentName().toLowerCase().contains(searchText) ||
                    borrowing.getBorrowingStatus().toLowerCase().contains(searchText)) {
                filteredList.add(borrowing);
            }
        }
        borrowingsTable.setItems(filteredList);
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
