package org.springframework.ai.openai.samples.helloworld.service;

import org.springframework.ai.openai.samples.helloworld.dto.AssessmentDTO;

import java.util.List;

public interface AssessmentManagementService {
    List<AssessmentDTO> generateAssessmentForActivity(String activityId);

    List<AssessmentDTO> getAssessmentByActivity(String activityId);

    List<AssessmentDTO> addSubmissions(String activityId, List<AssessmentDTO> assessments);

    AssessmentDTO studentReAssessment(String activityId, String studentId, String reAssessmentComment);

    AssessmentDTO getAssessmentByActivityAndStudent(String activityId, String studentId);

    AssessmentDTO updateAssessment(String activityId, String studentId, AssessmentDTO assessment);
}
