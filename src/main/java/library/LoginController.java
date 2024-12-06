package library;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginController extends Controller {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordFieldHidden;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Button loginButton;


    //log in
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        passwordField.setPromptText("Password");
        passwordField.setText("");
        passwordFieldHidden.setVisible(false);

        setupFieldFocusListener(usernameField, "Username");
        setupFieldFocusListener(passwordField, "Password");
        setupFieldFocusListener(passwordFieldHidden, "Password");

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });
    }

    //
    public void setupFieldFocusListener(TextInputControl a, String p) {
        a.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                a.setPromptText("");
                a.setStyle("-fx-border-color: #4c70ba; -fx-border-radius: 5;");
            } else {
                a.setPromptText(p);
                a.setStyle("-fx-border-color: #A0A0A0; -fx-border-radius: 5;");
            }
        });
    }

    //ẩn pass
    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        boolean isHiddenVisible = passwordFieldHidden.isVisible();
        if (isHiddenVisible) {
            passwordField.setText(passwordFieldHidden.getText());
            passwordField.setVisible(true);
            passwordFieldHidden.setVisible(false);
            togglePasswordButton.setText("\uD83D\uDE48");
        } else {
            passwordFieldHidden.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordFieldHidden.setVisible(true);
            togglePasswordButton.setText("\uD83D\uDE49");
        }
    }

    //Login succes
    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordFieldHidden.getText();

        if (login(username, password)) {
            loadNewScene("BaseScene", actionEvent);
        }
    }

    // Random pass => ko lỗi
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    //todo: đang cần  App Passwords của gmail.
    public void sendEmail(String recipientEmail, String newPassword) {
        String host = "smtp.gmail.com";
        String from = "trungabc498@gmail.com";
        String password = "abcxyz"; // App Passwords.

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject("Your New Password");
            message.setText("Here is your new password: " + newPassword);

            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    // Tạo
    @FXML
    public void handleForgotPassword(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot password");
        dialog.setHeaderText("Please enter your email!");
        dialog.setContentText("Email:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(email -> {
            if (email.isEmpty()) {
                showAlert("Error", "Email cannot be empty.");
            } else {
                String newPassword = generateRandomPassword(8);
                String updatequery = "UPDATE users SET hashedPassword = ? WHERE gmail = ?";

                try (Connection connection = DatabaseHelper.getInstance().getConnection()) {
                    PreparedStatement stmt = connection.prepareStatement(updatequery);
                    String hashedPassword = PasswordEncoder.hashedpassword(newPassword);
                    stmt.setString(1, hashedPassword);
                    stmt.setString(2, email);

                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        sendEmail(email, newPassword);
                        showAlert("Success", "A new password has been sent to: " + email);
                        showAlert("pass", newPassword);
                    } else {
                        showAlert("Error", "No user found with that email address.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "An error occurred while updating the password.");
                }
            }
        });
    }

    // đăng nhập thành công
    public boolean login(String username, String password) {
        DatabaseHelper.getInstance();
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("hashedPassword");

                if (PasswordEncoder.hashedpassword(password).equals(storedPassword)) {
                    System.out.println("Login Successful!");
                    User.loadUserData(resultSet);
                    return true;
                } else {
                    showAlert("Error", "Incorrect Password!");
                    return false;
                }

            } else if (username.isEmpty() && password.isEmpty()) {
                showAlert("Error", "Please enter your username and password!");
                return false;
            } else if (username.isEmpty()) {
                showAlert("Error", "Please enter your username!");
                return false;
            } else if (password.isEmpty()) {
                showAlert("Error", "Please enter your password!");
                return false;
            } else {
                showAlert("Error", "User not found!");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    public void handleCreateAccount(ActionEvent actionEvent) {
        loadNewScene("SignUpScene", actionEvent);
    }
}

