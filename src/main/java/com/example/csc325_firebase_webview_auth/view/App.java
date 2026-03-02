package com.example.csc325_firebase_webview_auth.view;

import com.example.csc325_firebase_webview_auth.model.FirestoreContext;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App – Entry point
 * Starts with splash screen, then navigates to login.
 */
public class App extends Application {

    public static Firestore fstore;
    public static FirebaseAuth fauth;
    public static Scene scene;

    // Tracks currently logged-in user (set by LoginController)
    public static String currentUserEmail;
    public static String currentUserDisplayName;

    private final FirestoreContext contxtFirebase = new FirestoreContext();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Firebase
        fstore = contxtFirebase.firebase();
        fauth  = FirebaseAuth.getInstance();

        // Start with the Splash Screen
        scene = new Scene(loadFXML("/files/SplashScreen.fxml"), 600, 400);
        primaryStage.setTitle("CSC325 Student Registry");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        scene.setRoot(root);
        // Auto-resize window to preferred size
        if (scene.getWindow() instanceof Stage s) {
            s.sizeToScene();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}