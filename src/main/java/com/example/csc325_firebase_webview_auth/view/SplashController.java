package com.example.csc325_firebase_webview_auth.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    private final String[] messages = {
            "Connecting to Firebase...",
            "Loading authentication...",
            "Preparing data services...",
            "Almost ready..."
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Animate progress bar over 3 seconds, then switch to login
        Timeline timeline = new Timeline();
        int steps = 40;
        for (int i = 0; i <= steps; i++) {
            final double progress = i / (double) steps;
            final int msgIndex = Math.min((int)(progress * messages.length), messages.length - 1);
            KeyFrame kf = new KeyFrame(Duration.millis(i * 80), e -> {
                progressBar.setProgress(progress);
                statusLabel.setText(messages[msgIndex]);
            });
            timeline.getKeyFrames().add(kf);
        }

        // After animation completes, go to login screen
        timeline.setOnFinished(e -> {
            try {
                App.setRoot("/files/LoginView.fxml");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        timeline.play();
    }
}