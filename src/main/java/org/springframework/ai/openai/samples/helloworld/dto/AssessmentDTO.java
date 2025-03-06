package org.springframework.ai.openai.samples.helloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(name = "AssessmentDTO", description = "Assessment data transfer object")
public class AssessmentDTO {
    @Schema(description = "Unique identifier of the assessment equal to the student's ID", example = "123456")
    String id;

    @Schema(description = "Student's submission", example = "File content")
    String submission;

    @Schema(description = "File type of the submission", example = "java")
    String fileType;

    @Schema(description = "status of the assessment", example = "pending",
            allowableValues = {"reviewed", "pending", "missing"})
    String status;  // 'reviewed', 'pending', or 'missing'

    @Schema(description = "Feedback from the teacher", example = "Feedback from teacher")
    String feedback;

    @Schema(description = "AI-generated assessment details")
    private AIAssessment aiAssessment;

    @Schema(description = "Teacher's reassessment details")
    private ReAssessment reAssessment;

    @Data
    @Schema(name = "AIAssessment", description = "AI-generated assessment details")
    public static class AIAssessment {
        @Schema(description = "AI generation", example = "Response from AI")
        private String aiGeneration;

        @Schema(description = "Rating of the generation", example = "good", allowableValues = {"good", "bad"})
        private String generationRating; // 'good' or 'bad'

        @Schema(description = "Global grade", example = "8.5")
        private Double globalGrade;

        @Schema(description = "Feedback from the AI")
        private Map<String, ComponentGrade> componentsGrades; // Key: component, Value: grade
    }

    @Data
    @Schema(name = "ReAssessment", description = "AI-generated reassessment with Teacher's feedback component details")
    public static class ReAssessment {
        @Schema(description = "AI generation", example = "Response from AI")
        private String aiGeneration;

        @Schema(description = "Rating of the generation", example = "good", allowableValues = {"good", "bad"})
        private String generationRating; // 'good' or 'bad'

        @Schema(description = "Teacher's comment", example = "Teacher's comment")
        private String teacherComment;

        @Schema(description = "Global grade", example = "8.5")
        private Double globalGrade;

        @Schema(description = "Feedback from the AI")
        private Map<String, ComponentGrade> componentsGrades; // Key: component, Value: grade
    }

    @Data
    @Schema(name = "ComponentGrade", description = "Grade details for a specific component of the submission evaluation")
    public static class ComponentGrade {
        @Schema(description = "Content of the component", example = "Component content")
        private String content;

        @Schema(description = "Grade obtained for the component", example = "8.5")
        private Double grade;

        @Schema(description = "Maximum grade of the component", example = "10.0")
        private Double maxGrade;
    }
}

