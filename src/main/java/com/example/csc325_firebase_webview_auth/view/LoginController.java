package com.example.csc325_firebase_webview_auth.view;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    // Login tab
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;
    @FXML private Button loginTabBtn;
    @FXML private Button registerTabBtn;

    // Login fields
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;

    // Register fields
    @FXML private TextField regFirstName;
    @FXML private TextField regLastName;
    @FXML private TextField regEmail;
    @FXML private PasswordField regPassword;
    @FXML private TextField regPhone;
    @FXML private Label registerMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showLoginTab(null);
    }

    @FXML
    private void showLoginTab(ActionEvent e) {
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginTabBtn.getStyleClass().setAll("auth-tab-active");
        registerTabBtn.getStyleClass().setAll("auth-tab");
    }

    @FXML
    private void showRegisterTab(ActionEvent e) {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
        loginTabBtn.getStyleClass().setAll("auth-tab");
        registerTabBtn.getStyleClass().setAll("auth-tab-active");
    }

    @FXML
    private void handleLogin(ActionEvent e) {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showLoginError("Please enter your email and password.");
            return;
        }

        loginMessage.setText("Signing in...");
        loginMessage.getStyleClass().removeAll("auth-message-success");

        // Run Firebase lookup off FX thread
        new Thread(() -> {
            try {
                // Firebase Admin SDK doesn't support password auth directly.
                // We verify by looking up the user; production apps use REST API.
                UserRecord user = App.fauth.getUserByEmail(email);
                // Store current user email for display
                App.currentUserEmail = user.getEmail();
                App.currentUserDisplayName = user.getDisplayName() != null
                        ? user.getDisplayName() : user.getEmail();

                Platform.runLater(() -> {
                    try {
                        App.setRoot("/files/AccessFBView.fxml");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (FirebaseAuthException ex) {
                Platform.runLater(() -> showLoginError("User not found: " + ex.getMessage()));
            } catch (Exception ex) {
                Platform.runLater(() -> showLoginError("Error: " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRegister(ActionEvent e) {
        String firstName = regFirstName.getText().trim();
        String lastName  = regLastName.getText().trim();
        String email     = regEmail.getText().trim();
        String password  = regPassword.getText();
        String phone     = regPhone.getText().trim();

        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("First name, email, and password are required.");
            return;
        }
        if (password.length() < 6) {
            showRegisterError("Password must be at least 6 characters.");
            return;
        }

        registerMessage.setText("Creating account...");

        new Thread(() -> {
            try {
                UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                        .setEmail(email)
                        .setEmailVerified(false)
                        .setPassword(password)
                        .setDisplayName(firstName + " " + lastName)
                        .setDisabled(false);

                if (!phone.isEmpty()) {
                    req.setPhoneNumber(phone);
                }

                UserRecord user = App.fauth.createUser(req);
                App.currentUserEmail = user.getEmail();
                App.currentUserDisplayName = firstName + " " + lastName;

                Platform.runLater(() -> {
                    try {
                        App.setRoot("/files/AccessFBView.fxml");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            } catch (FirebaseAuthException ex) {
                Platform.runLater(() -> showRegisterError("Registration failed: " + ex.getMessage()));
            } catch (Exception ex) {
                Platform.runLater(() -> showRegisterError("Error: " + ex.getMessage()));
            }
        }).start();
    }

    private void showLoginError(String msg) {
        loginMessage.setText(msg);
        loginMessage.getStyleClass().removeAll("auth-message-success");
    }

    private void showRegisterError(String msg) {
        registerMessage.setText(msg);
        registerMessage.getStyleClass().removeAll("auth-message-success");
    }
}