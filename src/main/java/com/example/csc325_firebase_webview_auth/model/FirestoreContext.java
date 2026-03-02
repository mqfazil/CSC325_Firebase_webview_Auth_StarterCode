package com.example.csc325_firebase_webview_auth.model;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initializes Firebase (Firestore + Auth + Storage).
 * key.json must be placed in src/main/resources/files/key.json
 *
 * To enable Firebase Storage uploads, add your bucket name below
 * (format: "your-project-id.appspot.com")
 */
public class FirestoreContext {

    private static final String STORAGE_BUCKET = "csc325-83c8a.firebasestorage.app";

    public Firestore firebase() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/files/key.json");
            if (serviceAccount == null) {
                throw new IOException("key.json not found in /files/. " +
                        "Please add your Firebase service account key.");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(STORAGE_BUCKET)
                    .build();

            // Only initialize once
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully.");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("WARNING: Firebase initialization failed. " +
                    "Check key.json and STORAGE_BUCKET value.");
        }
        return FirestoreClient.getFirestore();
    }
}