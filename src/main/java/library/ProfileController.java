package library;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfileController extends Controller {
    @FXML
    private Circle avatar;

    @FXML
    private Label fullNameLabel;

    @FXML
    private DatePicker dateOfBirthField;

    @FXML
    private Button setAvatar, changeProfileButton;

    @FXML
    private TextField usernameField, fullNameField, emailField, phoneField, roleField;

    private final String DEFAULT_AVATAR = "src/main/resources/image/UserAvatar/userAvatar.png";
    private boolean isEditing = false;

    private int getCurrentUserId() {
        return 1000; // Replace with actual user session management
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        try {
            DatabaseHelper.getInstance();
            // Load user information from the database, including the avatar
            Connection conn = DatabaseHelper.getInstance().getConnection();
            int userId = getCurrentUserId();
            String query = "SELECT * FROM users WHERE userID = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                // Set field with data from the database
                configureDateOfBirthField();

                usernameField.setText(resultSet.getString("username"));
                fullNameField.setText(resultSet.getString("userFullName"));
                roleField.setText(resultSet.getString("role"));
                emailField.setText(resultSet.getString("gmail"));
                phoneField.setText(resultSet.getString("phoneNumber"));

                //Set dateOfBirth field
                Date dateOfBirth = resultSet.getDate("DateOfBirth");
                if (dateOfBirth != null) {
                    dateOfBirthField.setValue(dateOfBirth.toLocalDate());
                }
                dateOfBirthField.getEditor().setStyle("-fx-alignment: center;");
                dateOfBirthField.getStyleClass().add("date-picker-disabled");

                this.fullNameLabel.setText(resultSet.getString("userFullName"));

                //Load user's avatar
                String avatarName = resultSet.getString("avatar");
                String avatarPath;

                // If no custom avatar is set, load the default avatar
                if (avatarName == null || avatarName.isEmpty() || avatarName.equals("userAvatar.png")) {
                    avatarPath = DEFAULT_AVATAR;
                } else {
                    avatarPath = "src/main/resources/image/UserAvatar/" + avatarName;
                }

                // Set the avatar image
                File avatarFile = new File(avatarPath);
                if (avatarFile.exists()) {
                    avatar.setFill(new ImagePattern(new Image(avatarFile.toURI().toString())));
                } else {
                    avatar.setFill(new ImagePattern(new Image(DEFAULT_AVATAR)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangeProfile(ActionEvent actionEvent) {

        if (!isEditing) {
            // Enable editing of the fields
            fullNameField.setEditable(true);
            phoneField.setEditable(true);
            emailField.setEditable(true);
            dateOfBirthField.setDisable(false);
            dateOfBirthField.getEditor().setDisable(true);
            changeProfileButton.setText("Save"); // Change button text to 'Save'
            isEditing = true;
        } else {
            // Save the updated fields to the database
            saveProfileChanges();
            changeProfileButton.setText("Change profile"); // Change back to 'Change profile'
            isEditing = false;

            // Disable fields again after saving
            fullNameField.setEditable(false);
            phoneField.setEditable(false);
            emailField.setEditable(false);
            dateOfBirthField.setDisable(true);
        }
    }

    private void saveProfileChanges() {
        DatabaseHelper.getInstance();
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            int userId = getCurrentUserId(); // Retrieve this from the session or login logic
            String updateQuery = "UPDATE users SET userFullName = ?, phoneNumber = ?, gmail = ?, dateOfBirth = ? WHERE userID = ?";

            PreparedStatement statement = conn.prepareStatement(updateQuery);

            statement.setString(1, fullNameField.getText());
            statement.setString(2, phoneField.getText());
            statement.setString(3, emailField.getText());
            LocalDate newDateOfBirth = dateOfBirthField.getValue();
            if (newDateOfBirth != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = newDateOfBirth.format(formatter);
                statement.setString(4, formattedDate);
            } else {
                statement.setString(4, null);
            }
            statement.setInt(5, userId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Method to handle setting the avatar
    @FXML
    public void handleSetAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(setAvatar.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Define the directory to store user avatars
                String destinationPath = "src/main/resources/image/UserAvatar/";

                // Create destination directory if it doesn't exist
                File directory = new File(destinationPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Create a new file in the avatar directory
                String newAvatarName = String.valueOf(getCurrentUserId()) + "_" + selectedFile.getName();
                File destinationFile = new File(directory, newAvatarName);

                // Copy the selected file to the destination
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update the avatar in the database
                DatabaseHelper.getInstance();
                Connection conn = DatabaseHelper.getInstance().getConnection();
                String query = "UPDATE users SET avatar = ? WHERE userID = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, newAvatarName);
                pstmt.setInt(2, getCurrentUserId());
                pstmt.executeUpdate();

                // Set the new avatar in the Circle
                avatar.setFill(new ImagePattern(new Image(destinationFile.toURI().toString())));

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleChangePassword() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");

        // Create labels and password fields
        Label currentPasswordLabel = new Label("Current Password:");
        PasswordField currentPasswordField = new PasswordField();

        Label newPasswordLabel = new Label("New Password:");
        PasswordField newPasswordField = new PasswordField();

        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();

        // Create a grid pane to organize fields
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(currentPasswordLabel, 0, 0);
        gridPane.add(currentPasswordField, 1, 0);
        gridPane.add(newPasswordLabel, 0, 1);
        gridPane.add(newPasswordField, 1, 1);
        gridPane.add(confirmPasswordLabel, 0, 2);
        gridPane.add(confirmPasswordField, 1, 2);

        // Add the gridPane to the dialog
        dialog.getDialogPane().setContent(gridPane);

        // Add OK and Cancel buttons
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        // Show the dialog and wait for the response
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            // Validate fields
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Error", "All fields are required.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert("Error", "New password and confirm password do not match.");
                return;
            }

            // Perform the password change operation
            changePasswordInDatabase(currentPassword, newPassword);
        }
    }

    private void changePasswordInDatabase(String currentPassword, String newPassword) {
        int currentUserId = getCurrentUserId();
        // Check if the current password matches the database record
        DatabaseHelper.getInstance();
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String query = "SELECT hashedPassword FROM users WHERE userID = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbPasswordHash = rs.getString("hashedPassword");

                // Assuming you have a method to check hashed passwords
                if (currentPassword.equals(dbPasswordHash)) {
                    // Update the password if the current password is valid
                    String updateQuery = "UPDATE users SET hashedPassword = ? WHERE userID = ?";
                    PreparedStatement updatePs = conn.prepareStatement(updateQuery);

                    updatePs.setString(1, newPassword);
                    updatePs.setInt(2, currentUserId);

                    int updated = updatePs.executeUpdate();
                    if (updated > 0) {
                        showAlert("Success", "Password updated successfully.");
                    } else {
                        showAlert("Error", "Failed to update the password.");
                    }
                } else {
                    showAlert("Error", "Current password is incorrect.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while accessing the database.");
        }
    }

    // Method to configure the DatePicker to use "dd/MM/yyyy" format
    public void configureDateOfBirthField() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateOfBirthField.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return date.format(dateFormatter);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
    }
}
