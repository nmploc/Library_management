package library;

import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.net.URL;

public class SignUpController extends Controller {

    @FXML
    private CheckBox checkBox;

    @FXML
    private Button showPassword;

    @FXML
    private Button showConfirmPassword;

    @FXML
    private TextField nameField, phoneField, emailField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private TextField passwordTextField, confirmPasswordTextField;

    @FXML
    private Text nameError, passwordError, checkboxError;

    @FXML
    private Button signUpButton;

    @FXML
    private TextField FullName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initially, hide the text fields and only show the password fields
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        confirmPasswordTextField.setManaged(false);
        confirmPasswordTextField.setVisible(false);

        // Sync the text fields with password fields
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordTextField.textProperty());
        // Disable the sign-up button by default
        signUpButton.setDisable(true);

        // Validate input fields to enable the sign-up button
        addInputValidation();
    }

    // Method to validate input fields and enable the sign-up button
    private void addInputValidation() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> checkInputs());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkInputs());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkInputs());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkInputs());
    }

    private void checkInputs() {
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isPasswordEmpty = passwordField.getText().trim().isEmpty();
        boolean isConfirmPasswordEmpty = confirmPasswordField.getText().trim().isEmpty();
        boolean isCheckBoxSelected = checkBox.isSelected();

        if (isNameEmpty) {
            nameError.setText("Please enter a name.");
        } else {
            nameError.setText("");
        }

        if (isPasswordEmpty || isConfirmPasswordEmpty || !passwordField.getText().equals(confirmPasswordField.getText())) {
            passwordError.setText("Passwords do not match.");
        } else {
            passwordError.setText("");
        }

        if (!isCheckBoxSelected) {
            checkboxError.setText("You must agree to the terms.");
        } else {
            checkboxError.setText("");
        }

        // Enable the sign-up button only when all inputs are valid
        signUpButton.setDisable(isNameEmpty || isPasswordEmpty ||
                isConfirmPasswordEmpty || !isCheckBoxSelected ||
                !passwordField.getText().equals(confirmPasswordField.getText()));
    }

    @FXML
    private void handleShowPassword(ActionEvent actionEvent) {
        boolean isPasswordVisible = passwordField.isVisible();
        if (!isPasswordVisible) {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            showPassword.setText("\uD83D\uDE48");
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            showPassword.setText("\uD83D\uDE49");
        }
    }

    @FXML
    public void handleShowConfirmPassword(ActionEvent actionEvent) {
        boolean isConfirmPasswordVisible = confirmPasswordField.isVisible();
        if (!isConfirmPasswordVisible) {
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            showConfirmPassword.setText("\uD83D\uDE48");
        } else {
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            showConfirmPassword.setText("\uD83D\uDE49");
        }
    }

    public boolean checkIfUserExists(String username, String phoneNumber, String email) {
        String query = "SELECT COUNT(*) FROM users WHERE userName = ? OR phoneNumber = ? OR gmail = ?";

        try (PreparedStatement stmt = DatabaseHelper.getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, phoneNumber);
            stmt.setString(3, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    void handleBack(ActionEvent actionEvent) {
        loadNewScene("LoginScene", actionEvent);
    }

    @FXML
    public void handleSignUp(ActionEvent actionEvent) {
        // Get the user input
        String username = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String userfullname = FullName.getText().trim();

        // Check if user exists in the database
        if (checkIfUserExists(username, phone, email)) {
            nameError.setText("Username, phone, or email already exists!");
        }

        if (registerUser(userfullname, username, password, phone, email)) {
            showAlert("Success", "Sign up successfully");
            loadNewScene("LoginScene", actionEvent);
        }
    }

    public boolean registerUser(String userFullName, String username, String password, String phoneNumber, String email) {
        if (checkIfUserExists(username, phoneNumber, email)) {
            System.out.println("User already exists.");
            return false;
        }

        String insertQuery = "INSERT INTO users (userFullName, username, hashedPassword, phoneNumber, gmail) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseHelper.getConnection();
             PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            String hashedPassword = PasswordEncoder.hashedpassword(password);

            stmt.setString(1, userFullName);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, email);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User registered successfully.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

