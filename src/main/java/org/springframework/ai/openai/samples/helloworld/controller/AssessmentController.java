package org.springframework.ai.openai.samples.helloworld.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.openai.samples.helloworld.dto.AssessmentDTO;
import org.springframework.ai.openai.samples.helloworld.service.AssessmentManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/ai-assessment")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "AIG Assessment Management", description = "Endpoints for managing AI-generated assessments")
public class AssessmentController {

    private final AssessmentManagementService assessmentService;

    @Autowired
    public AssessmentController(AssessmentManagementService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/activity/{id}/assess")
    @Operation(
            summary = "Generate assessment for activity",
            description = "Generate assessment for activity",
            parameters = {
                    @Parameter(name = "id", description = "Activity ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assessment generated successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AssessmentDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity or course not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> generateAssessmentForActivity(@PathVariable(value = "id") String activityId) {
        try {
            List<AssessmentDTO> assessments = assessmentService.generateAssessmentForActivity(activityId);
            return getResponseEntityList(assessments);
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/activity/{id}/list")
    @Operation(
            summary = "Get assessments by activity",
            description = "Get assessments by activity",
            parameters = {
                    @Parameter(name = "id", description = "Activity ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assessments found", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AssessmentDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity or course not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> getAssessmentByActivity(@PathVariable(value = "id") String activityId) {
        try {
            List<AssessmentDTO> assessments = assessmentService.getAssessmentByActivity(activityId);
            return getResponseEntityList(assessments);
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/activity/{id}/add-submissions")
    @Operation(
            summary = "Add submissions",
            description = "Add submissions",
            parameters = {
                    @Parameter(name = "id", description = "Activity ID", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of submissions",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AssessmentDTO.class))
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assessments added successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AssessmentDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity or course not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> addAssessments(
            @PathVariable(value = "id") String activityId,
            @RequestBody List<AssessmentDTO> assessments
    ) {
        try {
            List<AssessmentDTO> assessmentsAdded = assessmentService.addSubmissions(activityId, assessments);
            return getResponseEntityList(assessmentsAdded);
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/activity/{activityId}/student/{studentId}/re-assessment")
    @Operation(
            summary = "Student re-assessment",
            description = "Student re-assessment",
            parameters = {
                    @Parameter(name = "activityId", description = "Activity ID", required = true),
                    @Parameter(name = "studentId", description = "Student ID", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Re-assessment comment",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Re-assessment successful", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssessmentDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Bad request\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Activity, assessment or course not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity, assessment or course not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> studentReAssessment(
            @PathVariable(value = "activityId") String activityId,
            @PathVariable(value = "studentId") String studentId,
            @RequestBody String reAssessmentComment
    ) {
//        return new ResponseEntity(assessmentService.studentReAssessment(activityId, studentId, reAssessmentComment), HttpStatus.OK);
        try {
            AssessmentDTO assessment = assessmentService.studentReAssessment(activityId, studentId, reAssessmentComment);
            if (assessment != null) {
                return ResponseEntity.status(HttpStatus.OK).body(assessment);
            } else {
                HashMap<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Activity, assessment or course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/activity/{activityId}/student/{studentId}")
    @Operation(
            summary = "Get assessment by activity and student",
            description = "Get assessment by activity and student",
            parameters = {
                    @Parameter(name = "activityId", description = "Activity ID", required = true),
                    @Parameter(name = "studentId", description = "Student ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assessment found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssessmentDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Bad request\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Activity, assessment or course not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity, assessment or course not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> getAssessmentByActivityAndStudent(
            @PathVariable(value = "activityId") String activityId,
            @PathVariable(value = "studentId") String studentId
    ) {
        try {
            AssessmentDTO assessment = assessmentService.getAssessmentByActivityAndStudent(activityId, studentId);
            return getResponseEntity(assessment);
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/activity/{activityId}/student/{studentId}/update")
    @Operation(
            summary = "Update assessment",
            description = "Update assessment",
            parameters = {
                    @Parameter(name = "activityId", description = "Activity ID", required = true),
                    @Parameter(name = "studentId", description = "Student ID", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Assessment details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssessmentDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assessment updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AssessmentDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Bad request\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Activity, assessment or course not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity, assessment or course not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> updateAssessment(
            @PathVariable(value = "activityId") String activityId,
            @PathVariable(value = "studentId") String studentId,
            @RequestBody AssessmentDTO assessment
    ) {
//        return new ResponseEntity(assessmentService.updateAssessment(activityId, studentId, assessment), HttpStatus.OK);
        try {
            AssessmentDTO updatedAssessment = assessmentService.updateAssessment(activityId, studentId, assessment);
            return getResponseEntity(updatedAssessment);
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @NotNull
    private ResponseEntity<?> getResponseEntityList(List<AssessmentDTO> assessments) {
        if (assessments != null && !assessments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(assessments); // 200
        } else if (assessments == null) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Activity or course not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
        }
    }

    @NotNull
    private ResponseEntity<?> getResponseEntity(AssessmentDTO assessment) {
        if (assessment != null) {
            return ResponseEntity.status(HttpStatus.OK).body(assessment);
        } else {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Activity or student not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}