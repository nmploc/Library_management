package library;

import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import java.awt.*;
import javafx.scene.control.TextField;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class ReportController extends Controller {

    @FXML
    private ComboBox<String> choice;

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    private ListView<String> selectedFile;

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
        }
    }

    @FXML
    public void setFileChooser() {
        FileChooser fc = new FileChooser();
       // fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
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
