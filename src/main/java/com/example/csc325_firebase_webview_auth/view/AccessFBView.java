package com.example.csc325_firebase_webview_auth.view;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.net.URL;
import java.util.ResourceBundle;

public class AccessFBView implements Initializable {

    // Form fields
    @FXML private TextField nameField;
    @FXML private TextField majorField;
    @FXML private TextField ageField;
    @FXML private Button writeButton;
    @FXML private Button readButton;

    // TableView
    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> majorColumn;
    @FXML private TableColumn<Person, Integer> ageColumn;

    // Profile
    @FXML private ImageView profileImageView;
    @FXML private Label loggedInLabel;
    @FXML private Label statusLabel;

    private final ObservableList<Person> personList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bind table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        personTable.setItems(personList);

        // ViewModel binding for write button
        AccessDataViewModel vm = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(vm.userNameProperty());
        majorField.textProperty().bindBidirectional(vm.userMajorProperty());
        writeButton.disableProperty().bind(vm.isWritePossibleProperty().not());

        // Load default profile image
        try {
            Image defaultImage = new Image(
                    getClass().getResourceAsStream("/files/profile_empty.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Profile image not found: " + e.getMessage());
        }

        // Show logged-in user
        if (App.currentUserDisplayName != null && !App.currentUserDisplayName.isEmpty()) {
            loggedInLabel.setText("Signed in as:\n" + App.currentUserDisplayName);
        } else {
            loggedInLabel.setText("Not signed in");
        }

        setStatus("Ready");
    }

    // =========================================================
    //  MENU ACTIONS
    // =========================================================

    @FXML
    private void openLoginView(ActionEvent e) {
        try {
            App.setRoot("/files/LoginView.fxml");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSignOut(ActionEvent e) {
        App.currentUserEmail = null;
        App.currentUserDisplayName = null;
        try {
            App.setRoot("/files/LoginView.fxml");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleClose(ActionEvent e) {
        javafx.application.Platform.exit();
    }

    @FXML
    private void clearTable(ActionEvent e) {
        personList.clear();
        setStatus("Table cleared.");
    }

    @FXML
    private void showAbout(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("CSC325 Student Registry");
        alert.setContentText(
                "Firebase-powered JavaFX application.\n\n" +
                        "Features:\n" +
                        "• Splash screen\n" +
                        "• Firebase Authentication\n" +
                        "• Firestore read/write\n" +
                        "• TableView display\n" +
                        "• Profile picture upload\n\n" +
                        "Course: CSC325\nFarmingdale State College"
        );
        alert.showAndWait();
    }

    // =========================================================
    //  FORM / DATA ACTIONS
    // =========================================================

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

    @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

    @FXML
    private void clearForm(ActionEvent event) {
        nameField.clear();
        majorField.clear();
        ageField.clear();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    // =========================================================
    //  FIREBASE WRITE
    // =========================================================

    public void addData() {
        String name  = nameField.getText().trim();
        String major = majorField.getText().trim();
        String ageStr = ageField.getText().trim();

        if (name.isEmpty() || major.isEmpty() || ageStr.isEmpty()) {
            setStatus("Please fill in all fields.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            setStatus("Age must be a number.");
            return;
        }

        DocumentReference docRef = App.fstore.collection("References")
                .document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", name);
        data.put("Major", major);
        data.put("Age", age);

        ApiFuture<WriteResult> result = docRef.set(data);

        // Add to table immediately (optimistic)
        Person newPerson = new Person(name, major, age);
        personList.add(newPerson);

        clearForm(null);
        setStatus("Record added: " + name);
    }

    // =========================================================
    //  FIREBASE READ → TableView
    // =========================================================

    public boolean readFirebase() {
        setStatus("Loading records from Firebase...");
        personList.clear();

        new Thread(() -> {
            try {
                ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                ObservableList<Person> fetched = FXCollections.observableArrayList();
                for (QueryDocumentSnapshot doc : documents) {
                    String name  = String.valueOf(doc.getData().getOrDefault("Name", ""));
                    String major = String.valueOf(doc.getData().getOrDefault("Major", ""));
                    int age = 0;
                    try {
                        age = Integer.parseInt(String.valueOf(doc.getData().getOrDefault("Age", "0")));
                    } catch (NumberFormatException ignored) {}
                    fetched.add(new Person(name, major, age));
                }

                final int count = fetched.size();
                Platform.runLater(() -> {
                    personList.setAll(fetched);
                    setStatus("Loaded " + count + " record(s) from Firebase.");
                });

            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> setStatus("Error reading from Firebase: " + ex.getMessage()));
            }
        }).start();

        return true;
    }

    // =========================================================
    //  PROFILE PICTURE UPLOAD → Firebase Storage
    // =========================================================

    @FXML
    private void uploadProfilePicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(
                profileImageView.getScene().getWindow()
        );

        if (selectedFile == null) return;

        // Update local preview immediately
        Image localImage = new Image(selectedFile.toURI().toString());
        profileImageView.setImage(localImage);
        setStatus("Uploading profile picture...");

        // Upload to Firebase Storage in background
        new Thread(() -> {
            try {
                uploadToFirebaseStorage(selectedFile);
                Platform.runLater(() -> setStatus("Profile picture uploaded successfully!"));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        setStatus("Upload failed: " + ex.getMessage()
                                + "  (Image shown locally)"));
            }
        }).start();
    }

    /**
     * Uploads a file to Firebase Storage using the Firebase Admin SDK.
     * Requires firebase-admin with storage support and a storage bucket
     * configured in FirestoreContext / FirebaseOptions (storageBucket).
     */
    private void uploadToFirebaseStorage(File file) throws Exception {
        // Build a unique storage path for this user's profile picture
        String uid = (App.currentUserEmail != null)
                ? App.currentUserEmail.replace("@", "_").replace(".", "_")
                : "anonymous";
        String storagePath = "profilePictures/" + uid + "_" + file.getName();

        com.google.cloud.storage.Storage storage =
                com.google.firebase.cloud.StorageClient.getInstance()
                        .bucket()
                        .getStorage();

        com.google.cloud.storage.BlobId blobId = com.google.cloud.storage.BlobId.of(
                com.google.firebase.cloud.StorageClient.getInstance().bucket().getName(),
                storagePath
        );
        com.google.cloud.storage.BlobInfo blobInfo = com.google.cloud.storage.BlobInfo
                .newBuilder(blobId)
                .setContentType(detectContentType(file.getName()))
                .build();

        try (FileInputStream fis = new FileInputStream(file)) {
            storage.create(blobInfo, fis.readAllBytes());
        }

        System.out.println("Uploaded profile picture to: " + storagePath);
    }

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        return "image/jpeg";
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    public ObservableList<Person> getListOfUsers() {
        return personList;
    }
}