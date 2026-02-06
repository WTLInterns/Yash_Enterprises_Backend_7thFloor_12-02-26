package com.company.attendance.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseAdminConfig {

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials.classpath:}")
    private String credentialsClasspath;

    @PostConstruct
    public void init() {
        if (!firebaseEnabled) {
            log.warn("Firebase is disabled via firebase.enabled=false");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream in = openCredentials()) {
            if (in == null) {
                log.warn("Firebase credentials not found; FCM will be skipped");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin initialized");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin: {}", e.getMessage());
        }
    }

    private InputStream openCredentials() {
        try {
            if (credentialsPath != null && !credentialsPath.isBlank()) {
                return new FileInputStream(credentialsPath);
            }
        } catch (Exception e) {
            log.warn("Unable to open firebase.credentials.path: {}", e.getMessage());
        }

        try {
            if (credentialsClasspath != null && !credentialsClasspath.isBlank()) {
                return new ClassPathResource(credentialsClasspath).getInputStream();
            }
        } catch (Exception e) {
            log.warn("Unable to open firebase.credentials.classpath: {}", e.getMessage());
        }

        return null;
    }
}
