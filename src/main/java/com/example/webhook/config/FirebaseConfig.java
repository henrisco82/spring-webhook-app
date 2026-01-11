package com.example.webhook.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.client.email}")
    private String clientEmail;

    @Value("${firebase.private.key}")
    private String privateKey;

    @Bean
    public Firestore firestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            if (projectId != null && clientEmail != null && privateKey != null) {
                // Formatting private key to handle newlines correctly if passed as single
                // string
                String formattedKey = privateKey.replace("\\n", "\n");

                // Construct credentials manually since we are using individual fields
                // Normally we load from a file or JSON string, but user provided individual
                // fields in Next.js version
                // We'll mimic the construction of credentials similar to how Node SDK does it
                // if possible,
                // but Java SDK usually prefers a ServiceAccountCredentials object or a JSON
                // stream.
                // Let's create a JSON string from the fields to feed into
                // GoogleCredentials.fromStream

                String jsonCredentials = String.format(
                        "{" +
                                "  \"type\": \"service_account\"," +
                                "  \"project_id\": \"%s\"," +
                                "  \"private_key_id\": \"ignore\"," +
                                "  \"private_key\": \"%s\"," +
                                "  \"client_email\": \"%s\"," +
                                "  \"client_id\": \"ignore\"," +
                                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\"," +
                                "  \"token_uri\": \"https://oauth2.googleapis.com/token\"," +
                                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\"," +
                                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/%s\"" +
                                "}",
                        projectId, formattedKey.replace("\n", "\\n"), clientEmail, clientEmail // Note: escaping newline
                                                                                               // for JSON
                );

                GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(jsonCredentials.getBytes(StandardCharsets.UTF_8)));

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
            } else {
                System.out.println("Firebase Admin credentials missing");
                return null; // Or throw exception based on preference
            }
        }
        return FirestoreClient.getFirestore();
    }
}
