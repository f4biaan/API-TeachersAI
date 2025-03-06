package org.springframework.ai.openai.samples.helloworld.service.implement;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.springframework.ai.openai.samples.helloworld.dto.StudentDTO;
import org.springframework.ai.openai.samples.helloworld.firebase.FirebaseInit;
import org.springframework.ai.openai.samples.helloworld.service.StudentManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentManagementServiceImplement implements StudentManagementService {

    private final FirebaseInit firebase;

    public StudentManagementServiceImplement(FirebaseInit firebase) {
        this.firebase = firebase;
    }

    @Override
    public List<StudentDTO> getStudentsByCourse(String courseId) {
        ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollection(courseId).get();
        try {
            QuerySnapshot querySnapshot = querySnapshotApiFuture.get();
            if (querySnapshot.isEmpty()) {
                if (courseExists(courseId)) {
                    return null; // Retorna null si el curso no existe
                }
                return new ArrayList<>(); // Retorna una lista vacía si no hay estudiantes
            }

            List<StudentDTO> response = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                StudentDTO student = doc.toObject(StudentDTO.class);
                if (student != null) {
                    student.setId(doc.getId());
                    response.add(student);
                }
            }
            return response; // Retorna la lista de estudiantes si existen
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch students for course ID: " + courseId, e);
        }
    }

    @Override
    public List<String> addStudents(String course, List<StudentDTO> students) {

        if (courseExists(course)) {
            return null; // Retorna null si el curso no existe
        }
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException("Students list cannot be null or empty");
        }

        List<String> failedStudents = new ArrayList<>();
        for (StudentDTO student : students) {
            ApiFuture<WriteResult> writeResultApiFuture = getCollection(course).document(student.getId()).create(getDocData(student));
            try {
                if (writeResultApiFuture.get() == null) {
                    failedStudents.add(student.getId());
                }
            } catch (Exception e) {
                failedStudents.add(student.getId());
            }
        }
        return failedStudents;
    }


    @Override
    public StudentDTO add(String course, StudentDTO student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Student or Student ID cannot be null");
        }
        if (courseExists(course)) {
            return null; // Retorna null si el curso no existe
        }
        try {
            ApiFuture<WriteResult> writeResultApiFuture = getCollection(course).document(student.getId()).create(getDocData(student));
            if (null != writeResultApiFuture.get()) {
                return student;
            } else {
                throw new RuntimeException("Failed to add student");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the student", e);
        }
    }

    @Override
    public StudentDTO edit(String courseId, StudentDTO student) {
        if (courseExists(courseId)) {
            return null; // Retorna null si el curso no existe
        }
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection(courseId).document(student.getId()).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (!document.exists()) {
                throw new IllegalArgumentException("Student with ID: " + student.getId() + " does not exist in course with ID: " + courseId);
            }
            ApiFuture<WriteResult> writeResultApiFuture = getCollection(courseId).document(student.getId()).set(getDocData(student));
            writeResultApiFuture.get();
            return student;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while updating the student.", e);
        }
    }

    @Override
    public StudentDTO delete(String courseId, String studentId) {
        if (courseExists(courseId)) {
            return null; // Retorna null si el curso no existe
        }
//        ApiFuture<WriteResult> writeResultApiFuture = getCollection(courseId).document(studentId).delete();
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollection(courseId).document(studentId).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (document.exists()) {
                ApiFuture<WriteResult> writeResultApiFuture = getCollection(courseId).document(studentId).delete();
                writeResultApiFuture.get();
                return document.toObject(StudentDTO.class);
            } else {
                throw new IllegalArgumentException("Student with ID: " + studentId + " does not exist in course with ID: " + courseId);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the student.", e);
        }
    }

    private boolean courseExists(String courseId) {
        ApiFuture<DocumentSnapshot> docSnapshot = firebase.getFirestore()
                .collection("courses")
                .document(courseId)
                .get();

        try {
            return !docSnapshot.get().exists(); // Verifica si el curso no existe
        } catch (Exception e) {
            return true; // Si ocurre un error, asumimos que si existe
        }
    }


    private CollectionReference getCollection(String courseID) {
        return firebase.getFirestore().collection("courses").document(courseID).collection("students");
    }

    private static Map<String, Object> getDocData(StudentDTO student) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("id", student.getId());
        docData.put("email", student.getEmail());
        docData.put("username", student.getUsername());
        docData.put("name", student.getName());
        return docData;
    }
}
