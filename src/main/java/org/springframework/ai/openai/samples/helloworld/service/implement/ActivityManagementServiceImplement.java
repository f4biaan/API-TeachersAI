package org.springframework.ai.openai.samples.helloworld.service.implement;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.ai.openai.samples.helloworld.dto.ActivityDTO;
import org.springframework.ai.openai.samples.helloworld.firebase.FirebaseInit;
import org.springframework.ai.openai.samples.helloworld.service.ActivityManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityManagementServiceImplement implements ActivityManagementService {
    private final FirebaseInit firebaseInit;

    public ActivityManagementServiceImplement(FirebaseInit firebaseInit) {
        this.firebaseInit = firebaseInit;
    }

    @Override
    public List<ActivityDTO> list() {
        List<ActivityDTO> response = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection().get();
            for (DocumentSnapshot doc : querySnapshotApiFuture.get().getDocuments()) {
                ActivityDTO activity = doc.toObject(ActivityDTO.class);
                assert activity != null;
                activity.setId(doc.getId());
                response.add(activity);
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch activities", e);
        }
    }

    @Override
    public List<ActivityDTO> getActivitiesByTeacher(String teacherId) {
        List<ActivityDTO> response = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection().whereEqualTo("teacherId", teacherId).get();
            return getActivityDTOS(response, querySnapshotApiFuture);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch activities for teacher ID: " + teacherId, e);
        }
    }

    @Override
    public List<ActivityDTO> getActivitiesByCourse(String courseId) {
        List<ActivityDTO> response = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection().whereEqualTo("courseId", courseId).get();
            return getActivityDTOS(response, querySnapshotApiFuture);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch activities for course ID: " + courseId, e);
        }
    }

    @Override
    public ActivityDTO getActivity(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                ActivityDTO activity = document.toObject(ActivityDTO.class);
                assert activity != null;
                activity.setId(document.getId());
                return activity;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch activity for ID: " + id, e);
        }
    }

    @Override
    public ActivityDTO getLastUpdated(String teacherId) {
        if (teacherId == null || teacherId.isEmpty()) {
            throw new IllegalArgumentException("Teacher ID cannot be null or empty");
        }
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection()
                    .whereEqualTo("teacherId", teacherId)
                    .orderBy("lastUpdate", Query.Direction.DESCENDING)
                    .limit(1)
                    .get();
            List<QueryDocumentSnapshot> documents = querySnapshotApiFuture.get().getDocuments();
            if (documents.isEmpty()) {
                return null;
            }
            QueryDocumentSnapshot document = documents.get(0);
            if (document.exists()) {
                ActivityDTO activity = document.toObject(ActivityDTO.class);
                activity.setId(document.getId());
                return activity;
            }
            return null;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch last updated activity for teacher ID: " + teacherId, e);
        }
    }

    @Override
    public String generateId() {
        try {
            return getCollection().document().getId();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating ID", e);
        }
    }

    @Override
    public ActivityDTO add(ActivityDTO activity) {
        if (activity == null || activity.getId() == null) {
            throw new IllegalArgumentException("Activity or Activity ID cannot be null");
        }
        try {
            DocumentSnapshot document = getCollection().document(activity.getId()).get().get();
            if (document.exists()) {
                throw new IllegalArgumentException("Activity already exists");
            }
            WriteResult writeResult = getCollection().document(activity.getId()).create(getDocData(activity)).get();
            if (writeResult == null) {
                throw new RuntimeException("Failed to add the activity");
            }
            return activity;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the activity", e);
        }
    }

    @Override
    public ActivityDTO edit(String id, ActivityDTO activity) {
        if (activity == null || id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Activity or Activity ID cannot be null");
        }
        if (!activity.getId().equals(id)) {
            throw new IllegalArgumentException("Activity ID mismatch with the ID provided in the path");
        }
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                WriteResult writeResult = getCollection().document(id).set(getDocData(activity)).get();
                if (writeResult == null) {
                    throw new RuntimeException("Failed to update the activity");
                }
                return activity;
            }
            return null;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch activity for ID: " + id, e);
        }
    }

    @Override
    public ActivityDTO delete(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                ActivityDTO activity = document.toObject(ActivityDTO.class);
                assert activity != null;
                activity.setId(document.getId());
                WriteResult writeResult = getCollection().document(id).delete().get();
                if (writeResult == null) {
                    throw new RuntimeException("Failed to delete the activity");
                }
                return activity;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete activity for ID: " + id, e);
        }
        /*ApiFuture<WriteResult> writeResultApiFuture = getCollection().document(id).delete();
        try {
            if (null != writeResultApiFuture.get()) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }*/
    }

    private List<ActivityDTO> getActivityDTOS(List<ActivityDTO> response, ApiFuture<QuerySnapshot> querySnapshotApiFuture) {
        ActivityDTO activity;
        try {
            for (DocumentSnapshot doc : querySnapshotApiFuture.get().getDocuments()) {
                activity = doc.toObject(ActivityDTO.class);
                assert activity != null;
                activity.setId(doc.getId());
                response.add(activity);
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    private CollectionReference getCollection() {
        return firebaseInit.getFirestore().collection("activities");
    }

    private static Map<String, Object> getDocData(ActivityDTO activity) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("id", activity.getId());
        docData.put("name", activity.getName());
        docData.put("createdAt", activity.getCreatedAt());
        docData.put("teacherId", activity.getTeacherId());
        docData.put("courseId", activity.getCourseId());
        docData.put("typeActivity", activity.getTypeActivity());
        docData.put("learningComponent", activity.getLearningComponent());
        docData.put("academicLevel", activity.getAcademicLevel());
        docData.put("unitTheme", activity.getUnitTheme());
        docData.put("expectedLearningOutcomes", activity.getExpectedLearningOutcomes());
        docData.put("didacticStrategies", activity.getDidacticStrategies());
        docData.put("assessmentRubric", activity.getAssessmentRubric());
        docData.put("solution", activity.getSolution());
        docData.put("lastUpdate", activity.getLastUpdate());

        return docData;
    }
}
