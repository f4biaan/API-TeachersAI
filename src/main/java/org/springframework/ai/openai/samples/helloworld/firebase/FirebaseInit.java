package org.springframework.ai.openai.samples.helloworld.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseInit {

    @PostConstruct
    private void initFirestore() throws IOException {
        // Initialize Firebase
        // FirebaseApp.initializeApp(FirebaseOptions.fromResource("/firebase.json"));
        /* FileInputStream serviceAccount =
                new FileInputStream("path/to/serviceAccountKey.json");*/

        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKeyFirebase.json");

        assert serviceAccount != null;
        // FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://actividades-ia-docentes.firebaseio.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }

    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}
