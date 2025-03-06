package org.springframework.ai.openai.samples.helloworld.service.implement;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.springframework.ai.openai.samples.helloworld.dto.UserDTO;
import org.springframework.ai.openai.samples.helloworld.firebase.FirebaseInit;
import org.springframework.ai.openai.samples.helloworld.service.UserManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserManagementServiceImplement implements UserManagementService {

    private final FirebaseInit firebase;

    public UserManagementServiceImplement(FirebaseInit firebase) {
        this.firebase = firebase;
    }

    @Override
    public List<UserDTO> list() {
        List<UserDTO> response = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection().get();
            for (DocumentSnapshot doc : querySnapshotApiFuture.get().getDocuments()) {
                UserDTO user = doc.toObject(UserDTO.class);
                if (user != null) {
                    user.setId(doc.getId());
                    response.add(user);
                }
            }
            return response; // Return the list of users
        } catch (Exception e) {
            // e.printStackTrace(); // Log de error para diagnóstico
            return response; // Return an empty list if an exception occurs
        }
    }

    @Override
    public UserDTO getUser(String id) {
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                UserDTO user = document.toObject(UserDTO.class);
                assert user != null;
                user.setId(document.getId());
                return user;
            } else {
                return null; // Null, if the document does not exist
            }
        } catch (Exception e) {
            return null; // Return null if an exception occurs
        }
    }

    @Override
    public UserDTO add(UserDTO user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User or User ID cannot be null");
        }

        try {
            ApiFuture<WriteResult> writeResultApiFuture = getCollection().document(user.getId()).create(getDocData(user));
            WriteResult result = writeResultApiFuture.get();

            if (result != null) {
                return user; // Return the user if it was created successfully
            } else {
                throw new RuntimeException("Failed to create user");
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Log del error
            throw new RuntimeException("An error occurred while creating the user", e); // Excepción genérica
        }
    }

    @Override
    public UserDTO edit(String id, UserDTO user) {
        if (!user.getId().equals(id)) {
            throw new IllegalArgumentException("User ID cannot be null or different from the ID in the URL");
        }
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get(); // Search for the user if it exists
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                ApiFuture<WriteResult> writeResultApiFuture = getCollection().document(id).set(getDocData(user));
                writeResultApiFuture.get();
                return user; // Return the user if it was updated successfully
            } else {
                return null; // Return null if the user does not exist
            }
        } catch (Exception e) {
            return null; // Return null if an exception occurs
        }
    }


    @Override
    public UserDTO delete(String id) {
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                // El usuario existe, lo eliminamos
                ApiFuture<WriteResult> writeResultApiFuture = getCollection().document(id).delete();
                writeResultApiFuture.get(); // Espera que la eliminación se complete
                return document.toObject(UserDTO.class); // Devuelve el objeto eliminado
            } else {
                return null; // Si el usuario no existe, retorna null
            }
        } catch (Exception e) {
            return null; // En caso de cualquier error
        }
    }

    private CollectionReference getCollection() {
        return firebase.getFirestore().collection("users");
    }

    private static Map<String, Object> getDocData(UserDTO user) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("id", user.getId());
        docData.put("mail", user.getMail());
        docData.put("givenName", user.getGivenName());
        docData.put("familyName", user.getFamilyName());
        docData.put("displayName", user.getDisplayName());
        docData.put("photoURL", user.getPhotoURL());
        return docData;
    }
}
