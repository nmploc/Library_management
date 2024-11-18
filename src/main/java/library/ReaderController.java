package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.Optional;

public class ReaderController {
    @FXML
    private TableView<Reader> readerTable;

    @FXML
    private Button addReaderButton, editReaderButton, deleteReaderButton;

    @FXML
    private TextField searchField;

    private ObservableList<Reader> readerList;

    @FXML
    public void initialize() {
        loadReaderData();

        // Configure columns for TableView
        TableColumn<Reader, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("readerID"));

        TableColumn<Reader, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("readerName"));

        TableColumn<Reader, String> fullNameColumn = new TableColumn<>("Full Name");  // New column for full name
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Reader, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Reader, String> phoneColumn = new TableColumn<>("Phone Number");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        readerTable.getColumns().addAll(idColumn, nameColumn, fullNameColumn, emailColumn, phoneColumn);

        addReaderButton.setOnAction(event -> handleAddReader());
        editReaderButton.setOnAction(event -> handleEditReader());
        deleteReaderButton.setOnAction(event -> handleDeleteReader());
    }

    private void loadReaderData() {
        readerList = FXCollections.observableArrayList();
        String query = "SELECT readerID, readerName, fullName, email, phoneNumber FROM readers";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("readerID");
                String name = rs.getString("readerName");
                String fullName = rs.getString("fullName");  // Fetch full name
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phoneNumber");

                readerList.add(new Reader(id, name, fullName, email, phoneNumber));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        readerTable.setItems(readerList);
    }

    @FXML
    private void handleAddReader() {
        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle("Add Reader");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField fullNameField = new TextField();  // New field for full name
        fullNameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Full Name:"), fullNameField,  // Add full name field
                new Label("Email:"), emailField,
                new Label("Phone Number:"), phoneField
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isEmpty() || fullNameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return null;
                }
                String name = nameField.getText();
                String fullName = fullNameField.getText();  // Get full name
                String email = emailField.getText();
                String phoneNumber = phoneField.getText();

                return new Reader(0, name, fullName, email, phoneNumber);
            }
            return null;
        });

        Optional<Reader> result = dialog.showAndWait();
        result.ifPresent(this::addReaderToDatabase);
    }

    private void addReaderToDatabase(Reader newReader) {
        String insertQuery = "INSERT INTO readers (readerName, fullName, email, phoneNumber) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, newReader.getReaderName());
            pstmt.setString(2, newReader.getFullName());  // Insert full name
            pstmt.setString(3, newReader.getEmail());
            pstmt.setString(4, newReader.getPhoneNumber());
            pstmt.executeUpdate();
            loadReaderData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditReader() {
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();
        if (selectedReader == null) {
            showAlert("Selection Error", "No reader selected for editing.");
            return;
        }

        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle("Edit Reader");

        ButtonType editButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(selectedReader.getReaderName());
        nameField.setPromptText("Name");

        TextField fullNameField = new TextField(selectedReader.getFullName());  // Initialize full name field
        fullNameField.setPromptText("Full Name");

        TextField emailField = new TextField(selectedReader.getEmail());
        emailField.setPromptText("Email");

        TextField phoneField = new TextField(selectedReader.getPhoneNumber());
        phoneField.setPromptText("Phone Number");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Full Name:"), fullNameField,  // Add full name field
                new Label("Email:"), emailField,
                new Label("Phone Number:"), phoneField
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                if (nameField.getText().isEmpty() || fullNameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return null;
                }
                selectedReader.setReaderName(nameField.getText());
                selectedReader.setFullName(fullNameField.getText());  // Set full name
                selectedReader.setEmail(emailField.getText());
                selectedReader.setPhoneNumber(phoneField.getText());
                return selectedReader;
            }
            return null;
        });

        Optional<Reader> result = dialog.showAndWait();
        result.ifPresent(this::updateReaderInDatabase);
    }

    private void updateReaderInDatabase(Reader updatedReader) {
        String updateQuery = "UPDATE readers SET readerName = ?, fullName = ?, email = ?, phoneNumber = ? WHERE readerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, updatedReader.getReaderName());
            pstmt.setString(2, updatedReader.getFullName());  // Update full name
            pstmt.setString(3, updatedReader.getEmail());
            pstmt.setString(4, updatedReader.getPhoneNumber());
            pstmt.setInt(5, updatedReader.getReaderID());
            pstmt.executeUpdate();
            loadReaderData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteReader() {
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();
        if (selectedReader == null) {
            showAlert("No Selection", "Please select a reader to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this reader?");
        alert.setContentText(selectedReader.getReaderName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteReaderFromDatabase(selectedReader.getReaderID());
        }
    }

    private void deleteReaderFromDatabase(int readerID) {
        String deleteQuery = "DELETE FROM readers WHERE readerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setInt(1, readerID);
            pstmt.executeUpdate();
            loadReaderData();
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
    private void handleFindReader() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Input Error", "Please enter a search term.");
            return;
        }

        FilteredList<Reader> filteredList = new FilteredList<>(readerList, reader ->
                reader.getReaderName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        reader.getEmail().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        reader.getPhoneNumber().toLowerCase().contains(searchQuery.toLowerCase())
        );

        readerTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showAlert("No Results", "No readers found matching the search term.");
        }
    }
}
