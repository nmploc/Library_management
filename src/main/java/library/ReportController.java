package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController extends Controller {

    @FXML
    private ComboBox<String> choice;

    @FXML
    private javafx.scene.control.TextField textField;

    @FXML
    private javafx.scene.control.TextArea textArea;

    @FXML
    private ListView<String> selectedFile;

    @FXML
    private TableView<Report> submittedReportsTable;

    @FXML
    private TableColumn<Report, String> reportTypeColumn;

    @FXML
    private TableColumn<Report, String> titleColumn;

    @FXML
    private TableColumn<Report, String> contentColumn;

    private ObservableList<Report> reportList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectedFile.setVisible(false);
        initializeComboBox();

        selectedFile.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    private final Hyperlink hyperlink = new Hyperlink();

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            String name = new File(item).getName();
                            hyperlink.setText(name);
                            String finalItem = item;
                            hyperlink.setOnAction(event -> openFile(finalItem));
                            setGraphic(hyperlink);
                        }
                    }
                };
            }
        });
        selectedFile.setOnKeyPressed(this::removeSelectedFile);

        reportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));

        reportList = FXCollections.observableArrayList();
        submittedReportsTable.setItems(reportList);

        fetchReportsFromDatabase();
    }

    private void initializeComboBox() {
        String[] choices = {"Bug", "Document", "User"};
        choice.setItems(FXCollections.observableArrayList(choices));
        choice.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(choice.getPromptText());
                } else {
                    setText(item);
                }
            }
        });
        choice.setPromptText("Choice");
    }

    @FXML
    private void sendReport() {
        if (choice.getValue() == null) {
            showAlert("Can't create report!!!", "Please select an option first");
        } else if (textArea.getText().trim().isEmpty()) {
            showAlert("Can't create report!!!", "Content cannot be empty. Please enter some information!");
        } else {
            insertData();
            textArea.clear();
            textField.clear();
            choice.setValue(null);
            selectedFile.getItems().clear();
            selectedFile.setVisible(false);
            fetchReportsFromDatabase();
        }
    }

    @FXML
    public void setFileChooser() {
        FileChooser fc = new FileChooser();
        List<File> selectedFiles = fc.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                String filePath = file.getAbsolutePath();
                if (!filePath.isEmpty() && !selectedFile.getItems().contains(filePath)) {
                    this.selectedFile.getItems().add(filePath);
                }
            }
            selectedFile.setVisible(!selectedFile.getItems().isEmpty());
        }
    }

    @FXML
    private void removeSelectedFile(KeyEvent event) {
        if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
            SelectionModel<String> selectionModel = selectedFile.getSelectionModel();
            String selectedFileName = selectionModel.getSelectedItem();
            if (selectedFileName != null) {
                this.selectedFile.getItems().remove(selectedFileName);
            }
            selectedFile.setVisible(!selectedFile.getItems().isEmpty());
        }
    }

    private void openFile(String directoryPath) {
        try {
            if (directoryPath != null) {
                File file = new File(directoryPath);
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("File not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchReportsFromDatabase() {
        String query = "SELECT reportType, title, content FROM reports";
        DatabaseHelper.connectToDatabase();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            reportList.clear();

            while (rs.next()) {
                String reportType = rs.getString("reportType");
                String title = rs.getString("title");
                String content = rs.getString("content");
                reportList.add(new Report(reportType, title, content));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData() {
        String query = "INSERT INTO reports (userID, reportType, title, content) VALUES (?, ?, ?, ?)";

        DatabaseHelper.connectToDatabase();
        try (Connection conn = DatabaseHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            int userID = User.getID();

            String title = textField.getText();
            if (title.isEmpty()) {
                title = "none";
            }

            String content = textArea.getText();
            String type = choice.getValue();

            stmt.setInt(1, userID);
            stmt.setString(2, type);
            stmt.setString(3, title);
            stmt.setString(4, content);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                showAlert("Notification", "Report inserted successfully!");
            } else {
                showAlert("Notification", "Failed to insert report.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
