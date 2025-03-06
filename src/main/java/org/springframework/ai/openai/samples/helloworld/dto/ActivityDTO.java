package org.springframework.ai.openai.samples.helloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "DTO for structuring activity data")
public class ActivityDTO {
    @Schema(description = "Unique identifier of the activity", example = "123456")
    private String id;

    @Schema(description = "Name of the activity", example = "Activity Name")
    private String name;

    @Schema(description = "Description of the activity", example = "Activity Description")
    private Date createdAt;

    @Schema(description = "Unique identifier of the teacher", example = "123456")
    private String teacherId;

    @Schema(description = "Unique identifier of the course", example = "123456")
    private String courseId;

    @Schema(description = "Type of activity", example = "Activity Type")
    private String typeActivity;

    @Schema(description = "Learning component of the activity", example = "Activity Learning Component")
    private String learningComponent;

    @Schema(description = "Academic level of the activity", example = "Activity Academic Level")
    private String academicLevel;

    @Schema(description = "Unit theme of the activity", example = "Activity Unit Theme")
    private String unitTheme;

    @Schema(description = "Expected learning outcomes of the activity", example = "Activity Expected Learning Outcomes")
    private String expectedLearningOutcomes;

    @Schema(description = "Didactic strategies of the activity", example = "Activity Didactic Strategies")
    private String didacticStrategies;

    @Schema(description = "Assessment rubric of the activity", example = "Activity Assessment Rubric")
    private String assessmentRubric;

    @Schema(description = "Solution of the activity", example = "Activity Solution")
    private String solution;

    @Schema(description = "Last update of the activity", example = "Activity Last Update")
    private Date lastUpdate;
}
