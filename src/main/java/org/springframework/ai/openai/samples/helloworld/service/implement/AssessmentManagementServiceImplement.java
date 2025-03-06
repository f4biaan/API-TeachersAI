package org.springframework.ai.openai.samples.helloworld.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.ResponseFormat;
import org.springframework.ai.openai.samples.helloworld.dto.ActivityDTO;
import org.springframework.ai.openai.samples.helloworld.dto.AssessmentDTO;
import org.springframework.ai.openai.samples.helloworld.dto.CourseDTO;
import org.springframework.ai.openai.samples.helloworld.dto.StudentDTO;
import org.springframework.ai.openai.samples.helloworld.firebase.FirebaseInit;
import org.springframework.ai.openai.samples.helloworld.service.AssessmentManagementService;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class AssessmentManagementServiceImplement implements AssessmentManagementService {
    private final FirebaseInit firebase;
    private final ChatClient chatClient;
    private static final String PROMPT_TEMPLATE = "Eres el docente de la asignatura de: {0}, quién tiene el rol de revisor de tareas. " +
            "Dentro de la unidad temática: {1}, se plantea la actividad: {2}, que tiene como objetivo llegar a los siguientes resultados de aprendizaje: {3}. " +
            "El planteamiento de la tarea es el siguiente: {4}. " + "La entrega que ha realizado el estudiante es la siguiente: {5}. " +
            "Entrégame el resultado del análisis de la respuesta del estudiante, en base a la siguiente rúbrica: {6}, " +
            "adicionalmente a este análisis incluye una calificación que este dentro del rango especificado dentro de la rúbrica." +
            "Proporciona un análisis específico para cada componente de la rúbrica. " +
            "Incluye observaciones claras y completas, con ejemplos específicos para respaldar tu evaluación. " +
            "Proporciona recomendaciones detalladas, incluso para componentes correctamente realizados, y justifica la calificación asignada dentro del rango de la rúbrica." +
            "verbosity tiene 3 valores: 'low' ofrecer Breve retroalimentación con observaciones generales; " +
            "'medium' ofrece Retroalimentación con observaciones y ejemplos clave. " +
            "'high' ofrece Retroalimentación detallada con observaciones completas y ejemplos específicos. ";

    private static final String JSON_SCHEMA = """
            { "type": "json_object",
                "properties": {
                    "componentsGrades": {
                        "item component of rubric evaluation": {
                            "type": "json_object",
                            "properties": {
                                "content": {"type": "string", "verbosity": "medium", "feedbackType": "constructive"},
                                "grade": {"type": "number", "strictnessLevel": "lenient"},
                                "maxGrade": {"type": "number"}
                            },
                            "required": ["content", "grade", "maxGrade"],
                            "additionalProperties": false
                        }
                    },
                    "globalGrade": {"type": "number"},
                },
                "required": ["componentsGrades", "globalGrade"],
                "strictnessGradesLevel": "moderate"
                "additionalProperties": false }
            """;
    /*
    "strictnessLevel" = 'lenient' | 'moderate' | 'strict'; // Nivel de exigencia
    "feedbackType": 'constructive' | 'neutral' | 'detailed'; // Tipo de retroalimentación
     */

    public AssessmentManagementServiceImplement(FirebaseInit firebase, ChatClient chatClient) {
        this.firebase = firebase;
        this.chatClient = chatClient;
    }

    @Override
    public List<AssessmentDTO> generateAssessmentForActivity(String activityId) {
        if (activityId == null || activityId.isBlank()) {
            throw new IllegalArgumentException("Activity ID cannot be null or empty.");
        }
        List<AssessmentDTO> processedAssessments = new ArrayList<>();
        try {
            DocumentReference activityRef = getDocumentReferenceActivity(activityId);
            ActivityDTO activity = activityRef.get().get().toObject(ActivityDTO.class);
            if (activity == null) {
                return null;
            }
            if (activity.getAssessmentRubric() == null || activity.getAssessmentRubric().isBlank()) {
                throw new IllegalArgumentException("Assessment rubric is required for activity ID: " + activityId);
            }
            ApiFuture<QuerySnapshot> querySnapshotApiFutureAssessments = getCollectionReferenceAssessments(activityId).get();
            CourseDTO course = getDocumentReferenceCourse(activity.getCourseId()).get().get().toObject(CourseDTO.class);
            if (course == null) {
                return null;
            }
            List<StudentDTO> students = getDocumentReferenceCourse(activity.getCourseId())
                    .collection("students").get().get().toObjects(StudentDTO.class);

            List<AssessmentDTO> assessments = querySnapshotApiFutureAssessments.get().getDocuments().stream()
                    .map(doc -> doc.toObject(AssessmentDTO.class))
                    .toList();

            for (StudentDTO studentDTO : students) {
                AssessmentDTO assessment = assessments.stream()
                        .filter(a -> a.getId().equals(studentDTO.getId()))
                        .findFirst()
                        .orElse(new AssessmentDTO());

                if (assessment.getSubmission() == null ||
                        (assessment.getAiAssessment() != null && assessment.getAiAssessment().getAiGeneration() != null)
                ) {
                    continue;
                }
                String prompt = getFormat(course, activity, assessment);
                ChatResponse response = getAssessmentByGPTModel(prompt);

                if (assessment.getAiAssessment() == null) {
                    assessment.setAiAssessment(new AssessmentDTO.AIAssessment());
                }
                assessment.getAiAssessment().setAiGeneration(response.getResult().getOutput().getContent());
                assessment.getAiAssessment().setGlobalGrade(getGlobalGrade(response.getResult().getOutput().getContent()));
                assessment.getAiAssessment().setComponentsGrades(getComponentsGrades(response.getResult().getOutput().getContent()));

                DocumentReference assessmentRef = getCollectionReferenceAssessments(activityId).document(studentDTO.getId());
                assessmentRef.set(getDocData(assessment)).get();

                processedAssessments.add(assessment);
            }
            return processedAssessments;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException("Failed to generate assessments for activity ID: " + activityId, e);
        }
    }

    @Override
    public List<AssessmentDTO> getAssessmentByActivity(String activityId) {
        if (activityId == null || activityId.isBlank())
            throw new IllegalArgumentException("Activity ID cannot be null or empty.");
        List<AssessmentDTO> response = new ArrayList<>();
        try {
            ApiFuture<DocumentSnapshot> docSnapshot = getDocumentReferenceActivity(activityId).get();
            if (!docSnapshot.get().exists()) return null;
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = getCollectionReferenceAssessments(activityId).get();
            List<QueryDocumentSnapshot> documents = querySnapshotApiFuture.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                AssessmentDTO assess = doc.toObject(AssessmentDTO.class);
                if (assess != null) {
                    assess.setId(doc.getId());
                    response.add(assess);
                }
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch assessments for activity ID: " + activityId, e);
        }
    }

    @Override
    public List<AssessmentDTO> addSubmissions(String activityId, List<AssessmentDTO> assessments) {
        if (activityId == null || activityId.isBlank())
            throw new IllegalArgumentException("Activity ID cannot be null or empty.");
        if (assessments == null || assessments.isEmpty())
            throw new IllegalArgumentException("Assessments list cannot be null or empty.");
        List<AssessmentDTO> addedAssessments = new ArrayList<>();
        try {
            ApiFuture<DocumentSnapshot> docSnapshot = getDocumentReferenceActivity(activityId).get();
            if (!docSnapshot.get().exists()) {
                return null;
            }
            for (AssessmentDTO assessment : assessments) {
                ApiFuture<WriteResult> future = getCollectionReferenceAssessments(activityId)
                        .document(assessment.getId())
                        .set(getDocData(assessment));
                future.get();
                addedAssessments.add(assessment);
            }
            return addedAssessments;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add submissions for activity ID: " + activityId, e);
        }
    }

    @Override
    public AssessmentDTO studentReAssessment(String activityId, String studentId, String reAssessmentComment) {
        if (activityId == null || activityId.isBlank())
            throw new IllegalArgumentException("Activity ID cannot be null or empty.");
        if (studentId == null || studentId.isBlank())
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        if (reAssessmentComment == null || reAssessmentComment.isBlank())
            throw new IllegalArgumentException("Re-assessment comment cannot be null or empty.");
        try {
            ApiFuture<DocumentSnapshot> activityFuture = getDocumentReferenceActivity(activityId).get();
            ActivityDTO activity = activityFuture.get().exists() ? activityFuture.get().toObject(ActivityDTO.class) : null;
            if (activity == null) return null;
            ApiFuture<DocumentSnapshot> assessmentFuture = getCollectionReferenceAssessments(activityId).document(studentId).get();
            AssessmentDTO assessment = assessmentFuture.get().exists() ? assessmentFuture.get().toObject(AssessmentDTO.class) : null;
            if (assessment == null) return null;
            ApiFuture<DocumentSnapshot> courseFuture = getDocumentReferenceCourse(activity.getCourseId()).get();
            CourseDTO course = courseFuture.get().exists() ? courseFuture.get().toObject(CourseDTO.class) : null;
            if (course == null) return null;

            String prompt = getFormat(course, activity, assessment);
            prompt += "Consideraciones estos detalles adicionales que se debe tomar en cuenta para la evaluación de cada uno de los componentes de la rúbrica de evaluación: " + reAssessmentComment;
            ChatResponse response = getAssessmentByGPTModel(prompt);

            if (assessment.getReAssessment() == null) assessment.setReAssessment(new AssessmentDTO.ReAssessment());

            assessment.getReAssessment().setAiGeneration(response.getResult().getOutput().getContent());
            assessment.getReAssessment().setTeacherComment(reAssessmentComment);
            assessment.getReAssessment().setGlobalGrade(getGlobalGrade(response.getResult().getOutput().getContent()));
            assessment.getReAssessment().setComponentsGrades(getComponentsGrades(response.getResult().getOutput().getContent()));
            DocumentReference assessmentRef = getCollectionReferenceAssessments(activityId).document(studentId);
            assessmentRef.set(getDocData(assessment)).get();
            return assessment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate re-assessment for student ID: " + studentId, e);
        }
    }

    @Override
    public AssessmentDTO getAssessmentByActivityAndStudent(String activityId, String studentId) {
        if (activityId == null || activityId.isBlank())
            throw new IllegalArgumentException("Activity ID cannot be null or empty.");
        if (studentId == null || studentId.isBlank())
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        ApiFuture<DocumentSnapshot> docFuture = getCollectionReferenceAssessments(activityId).document(studentId).get();
        try {
            DocumentSnapshot document = docFuture.get();
            if (!document.exists()) return null;
            AssessmentDTO assessment = docFuture.get().toObject(AssessmentDTO.class);
            if (assessment == null) return null;
            assessment.setId(assessment.getId());
            return assessment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch assessment for student ID: " + studentId, e);
        }
    }

    @Override
    public AssessmentDTO updateAssessment(String activityId, String studentId, AssessmentDTO assessment) {
        if (activityId == null || activityId.isBlank())
            throw new IllegalArgumentException("Activity ID cannot be null or empty.");
        if (studentId == null || studentId.isBlank())
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        if (assessment == null || assessment.getId() == null)
            throw new IllegalArgumentException("Assessment or Assessment ID cannot be null.");
        if (!studentId.equals(assessment.getId()))
            throw new IllegalArgumentException("Student ID and Assessment ID must be the same.");
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = getCollectionReferenceAssessments(activityId).document(studentId).get();
        try {
            DocumentSnapshot document = documentSnapshotApiFuture.get();
            if (!document.exists()) return null;
            WriteResult writeResult = getCollectionReferenceAssessments(activityId).document(studentId).set(getDocData(assessment)).get();
            if (writeResult == null) throw new RuntimeException("Failed to update the assessment");
            return assessment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update assessment for student ID: " + studentId, e);
        }
    }

    private static String getFormat(CourseDTO course, ActivityDTO activity, AssessmentDTO assessment) {
        return MessageFormat.format(
                PROMPT_TEMPLATE,
                course.getSubject(),
                activity.getUnitTheme(),
                activity.getName(),
                activity.getExpectedLearningOutcomes(),
                activity.getDidacticStrategies(),
                assessment.getSubmission(),
                activity.getAssessmentRubric()
        ) + " El formato de la Respuesta debe ser con la siguiente estructura:" + JSON_SCHEMA;
    }

    private ChatResponse getAssessmentByGPTModel(String prompt) {
        return chatClient.call(
                new Prompt(
                        prompt,
                        OpenAiChatOptions
                                .builder()
                                .withModel("gpt-4o")
                                .withTemperature(0.1F)
                                .withTopP(0.4F)
                                .withResponseFormat(new ResponseFormat("json_object"))
                                .withMaxTokens(1000)
                                .build())
        );
    }

    private DocumentReference getDocumentReferenceCourse(String courseId) {
        return firebase.getFirestore().collection("courses").document(courseId);
    }

    private DocumentReference getDocumentReferenceActivity(String activityId) {
        return firebase.getFirestore().collection("activities").document(activityId);
    }

    private CollectionReference getCollectionReferenceAssessments(String activityId) {
        return getDocumentReferenceActivity(activityId).collection("assessments");
    }

    private static Double getGlobalGrade(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("properties").path("globalGrade").asDouble();
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, AssessmentDTO.ComponentGrade> getComponentsGrades(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            // Obtener el nodo "componentsGrades"
            JsonNode componentsGradesNode = rootNode.path("properties").path("componentsGrades");
            Map<String, AssessmentDTO.ComponentGrade> componentsGrades = new HashMap<>();

            // Iterar sobre los elementos del nodo "componentsGrades"
            Iterator<Map.Entry<String, JsonNode>> fields = componentsGradesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String componentName = field.getKey();
                JsonNode componentData = field.getValue().path("properties");

                // Crear una instancia de ComponentGrade
                AssessmentDTO.ComponentGrade componentGrade = new AssessmentDTO.ComponentGrade();
                componentGrade.setContent(componentData.path("content").asText());
                componentGrade.setGrade(componentData.path("grade").asDouble());
                componentGrade.setMaxGrade(componentData.path("maxGrade").asDouble());

                // Agregar al mapa
                componentsGrades.put(componentName, componentGrade);
            }

            return componentsGrades;
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }

    }

    private static Map<String, Object> getDocData(AssessmentDTO assessment) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("id", assessment.getId());
        docData.put("submission", assessment.getSubmission());
        docData.put("fileType", assessment.getFileType());
        docData.put("status", assessment.getStatus());
        docData.put("feedback", assessment.getFeedback());


        // Manejar campos opcionales: AIAssessment y ReAssessment
        if (assessment.getAiAssessment() != null) {
            Map<String, Object> aiAssessmentData = new HashMap<>();
            AssessmentDTO.AIAssessment ai = assessment.getAiAssessment();
            aiAssessmentData.put("aiGeneration", ai.getAiGeneration());
            aiAssessmentData.put("generationRating", ai.getGenerationRating());
            aiAssessmentData.put("globalGrade", ai.getGlobalGrade());

            if (ai.getComponentsGrades() != null) {
                aiAssessmentData.put("componentsGrades", ai.getComponentsGrades());
            }

            docData.put("aiAssessment", aiAssessmentData);
        }

        if (assessment.getReAssessment() != null) {
            Map<String, Object> reAssessmentData = new HashMap<>();
            AssessmentDTO.ReAssessment re = assessment.getReAssessment();
            reAssessmentData.put("aiGeneration", re.getAiGeneration());
            reAssessmentData.put("generationRating", re.getGenerationRating());
            reAssessmentData.put("teacherComment", re.getTeacherComment());
            reAssessmentData.put("globalGrade", re.getGlobalGrade());

            if (re.getComponentsGrades() != null) {
                reAssessmentData.put("componentsGrades", re.getComponentsGrades());
            }

            docData.put("reAssessment", reAssessmentData);
        }

        return docData;
    }
}