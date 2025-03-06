package org.springframework.ai.openai.samples.helloworld.service.implement;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.ai.openai.samples.helloworld.dto.CourseDTO;
import org.springframework.ai.openai.samples.helloworld.firebase.FirebaseInit;
import org.springframework.ai.openai.samples.helloworld.service.CourseManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseManagementServiceImplement implements CourseManagementService {
    private final FirebaseInit firebaseInit;

    public CourseManagementServiceImplement(FirebaseInit firebaseInit) {
        this.firebaseInit = firebaseInit;
    }

    @Override
    public List<CourseDTO> list() {
        List<CourseDTO> response = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection().get();
            return getCourseDTOS(response, querySnapshotApiFuture);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch courses", e);
        }
    }

    @Override
    public List<CourseDTO> getCoursesByTeacher(String teacherId) {
        List<CourseDTO> response = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection().whereEqualTo("teacherId", teacherId).get();
            return getCourseDTOS(response, querySnapshotApiFuture);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch courses for teacher ID: " + teacherId, e);
        }
    }

    @Override
    public CourseDTO getCourse(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (!document.exists()) return null;
            CourseDTO course = document.toObject(CourseDTO.class);
            if (course == null) return null;
            course.setId(document.getId());
            return course;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch course for ID: " + id, e);
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
    public CourseDTO add(CourseDTO course) {
        if (course == null || course.getId() == null) {
            throw new IllegalArgumentException("Course or Course ID cannot be null");
        }
        try {
            DocumentSnapshot document = getCollection().document(course.getId()).get().get();
            if (document.exists()) {
                throw new IllegalArgumentException("Course already exists");
            }
            WriteResult writeResult = getCollection().document(course.getId()).create(getDocData(course)).get();
            if (writeResult == null) {
                throw new RuntimeException("Failed to add the course");
            }
            return course;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the course", e);
        }
    }

    @Override
    public CourseDTO edit(String id, CourseDTO course) {
        if (course == null || course.getId() == null)
            throw new IllegalArgumentException("Course or Course ID cannot be null");
        if (!course.getId().equals(id))
            throw new IllegalArgumentException("Course ID cannot be null or different from the ID in the path");
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (!document.exists()) return null;
            WriteResult writeResult = getCollection().document(id).set(getDocData(course)).get();
            if (writeResult == null) throw new RuntimeException("Failed to update the course");
            return course;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while updating the course", e);
        }
    }

    @Override
    public CourseDTO delete(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("ID cannot be null or empty");
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection().document(id).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (!document.exists()) return null;
            CourseDTO course = document.toObject(CourseDTO.class);
            assert course != null;
            course.setId(document.getId());
            WriteResult writeResult = getCollection().document(id).delete().get();
            if (writeResult == null) {
                throw new RuntimeException("Failed to delete the course");
            }
            return course;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the course", e);
        }
    }

    private List<CourseDTO> getCourseDTOS(List<CourseDTO> response, ApiFuture<QuerySnapshot> querySnapshotApiFuture) {
        CourseDTO course;
        try {
            for (DocumentSnapshot doc : querySnapshotApiFuture.get().getDocuments()) {
                course = doc.toObject(CourseDTO.class);
                assert course != null;
                course.setId(doc.getId());
                response.add(course);
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    private CollectionReference getCollection() {
        return firebaseInit.getFirestore().collection("courses");
    }

    private static Map<String, Object> getDocData(CourseDTO course) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("id", course.getId());
        docData.put("faculty", course.getFaculty());
        docData.put("department", course.getDepartment());
        docData.put("degree", course.getDegree());
        docData.put("subject", course.getSubject());
        docData.put("subjectCode", course.getSubjectCode());
        docData.put("modality", course.getModality());
        docData.put("teacherId", course.getTeacherId());
        docData.put("academicPeriod", course.getAcademicPeriod());
        docData.put("academicLevel", course.getAcademicLevel());
        docData.put("createdAt", course.getCreatedAt());

        return docData;
    }
}
