package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReaderController extends Controller {
    @FXML
    private TableView<Reader> readerTable;

    @FXML
    private Button addReaderButton, editReaderButton, deleteReaderButton;

    @FXML
    private TextField searchField;

    private ObservableList<Reader> readerList;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadReaderData();

        // Configure columns for TableView
        TableColumn<Reader, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("readerID"));

        TableColumn<Reader, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("readerName"));

        TableColumn<Reader, String> fullNameColumn = new TableColumn<>("Full Name");
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

        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("readerID");
                String name = rs.getString("readerName");
                String fullName = rs.getString("fullName");
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
        // Create a new Stage for adding a reader
        Stage addReaderStage = new Stage();
        Main.registerStage(addReaderStage);
        addReaderStage.setTitle("Add Reader");

        // Name field
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setPrefWidth(300);

        // Full Name field
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setPrefWidth(300);

        // Email field
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(300);

        // Phone Number field
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        phoneField.setPrefWidth(300);

        // Buttons
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");

        addButton.setPrefWidth(100);
        cancelButton.setPrefWidth(100);

        // Add button action
        addButton.setOnAction(event -> {
            if (nameField.getText().isEmpty() || fullNameField.getText().isEmpty() ||
                    emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                showAlert("Input Error", "Please fill in all fields.");
                return;
            }

            // Create a new Reader object
            Reader newReader = new Reader(0, nameField.getText(), fullNameField.getText(),
                    emailField.getText(), phoneField.getText());

            // Kiểm tra xem reader đã tồn tại chưa
            if (DatabaseHelper.getInstance().isReaderExist(newReader)) {
                // Nếu reader tồn tại, hiển thị thông báo lỗi
                showAlert("Duplicate Entry", "A reader with the same name, email, or phone number already exists.");
                return;
            }

            // Nếu không có lỗi, thêm reader vào cơ sở dữ liệu
            DatabaseHelper.getInstance().addReaderToDatabase(newReader);
            loadReaderData();  // Tải lại dữ liệu sau khi thêm thành công

            // Close the add reader window
            addReaderStage.close();
        });

        // Cancel button action
        cancelButton.setOnAction(event -> addReaderStage.close());

        // Button layout
        HBox buttonBox = new HBox(10, addButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout for the input fields and labels
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Full Name:"), fullNameField,
                new Label("Email:"), emailField,
                new Label("Phone Number:"), phoneField,
                buttonBox
        );
        vbox.setStyle("-fx-padding: 20px;");

        // Scene
        Scene scene = new Scene(vbox, 400, 320);
        addReaderStage.setScene(scene);
        addReaderStage.show();
    }

    @FXML
    private void handleEditReader() {
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();
        if (selectedReader == null) {
            showAlert("Selection Error", "No reader selected for editing.");
            return;
        }

        Stage editReaderStage = new Stage();
        Main.registerStage(editReaderStage);
        editReaderStage.setTitle("Edit Reader");

        // Name field
        TextField nameField = new TextField(selectedReader.getReaderName());
        nameField.setPrefWidth(300);

        // Full Name field
        TextField fullNameField = new TextField(selectedReader.getFullName());
        fullNameField.setPrefWidth(300);

        // Email field
        TextField emailField = new TextField(selectedReader.getEmail());
        emailField.setPrefWidth(300);

        // Phone Number field
        TextField phoneField = new TextField(selectedReader.getPhoneNumber());
        phoneField.setPrefWidth(300);

        // Save button
        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(100);
        saveButton.setOnAction(event -> {
            if (nameField.getText().isEmpty() || fullNameField.getText().isEmpty() ||
                    emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                showAlert("Input Error", "Please fill in all fields.");
                return;
            }

            // Create a new Reader object with the updated data
            Reader updatedReader = new Reader(
                    selectedReader.getReaderID(), // Preserve the original ID for update
                    nameField.getText(),
                    fullNameField.getText(),
                    emailField.getText(),
                    phoneField.getText()
            );

            // Kiểm tra trùng lặp thông tin trong cơ sở dữ liệu
            if (DatabaseHelper.getInstance().isReaderExist(updatedReader)) {
                showAlert("Duplicate Entry", "A reader with the same name, email, or phone number already exists.");
                return;
            }

            // Nếu không trùng lặp, cập nhật thông tin vào cơ sở dữ liệu
            DatabaseHelper.getInstance().updateReaderInDatabase(updatedReader);
            loadReaderData();

            // Close the window after saving
            editReaderStage.close();
        });

        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(event -> editReaderStage.close());

        // Button layout
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Form layout (VBox)
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Full Name:"), fullNameField,
                new Label("Email:"), emailField,
                new Label("Phone Number:"), phoneField,
                buttonBox
        );
        vbox.setStyle("-fx-padding: 20px;");

        // Scene setup
        Scene scene = new Scene(vbox, 400, 320);
        editReaderStage.setScene(scene);
        editReaderStage.show();
    }

    @FXML
    private void handleDeleteReader() {
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();
        if (selectedReader == null) {
            showAlert("No Selection", "Please select a reader to delete.");
            return;
        }
        // Gọi phương thức isReaderBorrowing qua DatabaseHelper
        if (DatabaseHelper.getInstance().isReaderBorrowing(selectedReader.getReaderID())) {
            showAlert("Cannot Delete", "The selected reader is currently borrowing books. Please resolve their borrowings first.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this reader?");
        alert.setContentText(selectedReader.getReaderName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseHelper.getInstance().deleteReaderFromDatabase(selectedReader.getReaderID());
            loadReaderData();
        }
    }

    @FXML
    private void handleFindReader() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Input Error", "Please enter a search term.");
            return;
        }

        FilteredList<Reader> filteredList = new FilteredList<>(readerList, reader -> {
            String readerName = reader.getReaderName() != null ? reader.getReaderName().toLowerCase() : "";
            String fullName = reader.getFullName() != null ? reader.getFullName().toLowerCase() : "";
            String email = reader.getEmail() != null ? reader.getEmail().toLowerCase() : "";
            String phoneNumber = reader.getPhoneNumber() != null ? reader.getPhoneNumber().toLowerCase() : "";

            return readerName.contains(searchQuery.toLowerCase()) ||
                    fullName.contains(searchQuery.toLowerCase()) ||
                    email.contains(searchQuery.toLowerCase()) ||
                    phoneNumber.contains(searchQuery.toLowerCase());
        });

        readerTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showAlert("No Results", "No readers found matching the search term.");
        }
    }
}
