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
import org.springframework.ai.openai.samples.helloworld.dto.ActivityDTO;
import org.springframework.ai.openai.samples.helloworld.service.ActivityManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/activity")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Activity Management", description = "Endpoints for managing activities")
public class ActivityController {

    private final ActivityManagementService activityService;

    @Autowired
    public ActivityController(ActivityManagementService activityService) {
        this.activityService = activityService;
    }

    @GetMapping(value = "/generateId")
    @Operation(summary = "Generate a new activity ID", description = "Generate a new activity ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity ID generated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"id\": \"activityId\", \"collection\": \"activities\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> generateId() {
        try {
            HashMap<String, String> response = new HashMap<>();
            response.put("id", activityService.generateId());
            response.put("collection", "activities");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/list")
    @Operation(summary = "List all activities", description = "List all activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activities listed successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ActivityDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No activities found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> list() {
        try {
            List<ActivityDTO> activities = activityService.list();
            if (activities != null && !activities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(activities);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/teacher/{id}")
    @Operation(
            summary = "Get activities by teacher ID",
            description = "Get activities by teacher ID",
            parameters = {
                    @Parameter(name = "id", description = "Teacher ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activities listed successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ActivityDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No activities found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> getActivitiesByTeacher(@PathVariable(value = "id") String id) {
        try {
            List<ActivityDTO> activities = activityService.getActivitiesByTeacher(id);
            if (activities != null && !activities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(activities);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/course/{id}")
    @Operation(
            summary = "Get activities by course ID",
            description = "Get activities by course ID",
            parameters = {
                    @Parameter(name = "id", description = "Course ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activities listed successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ActivityDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No activities found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> getActivitiesByCourse(@PathVariable(value = "id") String id) {
        try {
            List<ActivityDTO> activities = activityService.getActivitiesByCourse(id);
            if (activities != null && !activities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(activities);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/{id}")
    @Operation(
            summary = "Get activity by ID",
            description = "Get activity by ID",
            parameters = {
                    @Parameter(name = "id", description = "Activity ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity fetched successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActivityDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"ID cannot be null or empty\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> getActivity(@PathVariable(value = "id") String id) {
        try {
            ActivityDTO activity = activityService.getActivity(id);
            return getResponseEntity(activity);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/teacher/{id}/last-updated")
    @Operation(
            summary = "Get last updated activity by teacher ID",
            description = "Get last updated activity by teacher ID",
            parameters = {
                    @Parameter(name = "id", description = "Teacher ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity fetched successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActivityDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Teacher ID cannot be null or empty\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Activity not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> getLastUpdated(@PathVariable(value = "id") String id) {
        try {
            ActivityDTO activity = activityService.getLastUpdated(id);
            return getResponseEntity(activity);
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

    @PostMapping(value = "/add")
    @Operation(
            summary = "Add a new activity",
            description = "Add a new activity",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Activity object to be added",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActivityDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity added successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActivityDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Activity or Activity ID cannot be null\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> add(@RequestBody ActivityDTO activity) {
        try {
            ActivityDTO newActivity = activityService.add(activity);
            return ResponseEntity.status(HttpStatus.OK).body(newActivity);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping(value = "/{id}/update")
    @Operation(
            summary = "Edit an existing activity",
            description = "Edit an existing activity",
            parameters = {
                    @Parameter(name = "id", description = "Activity ID", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Activity object to be updated",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActivityDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActivityDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Activity or Activity ID cannot be null\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Activity not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> edit(@PathVariable(value = "id") String id, @RequestBody ActivityDTO activity) {
        try {
            ActivityDTO updatedActivity = activityService.edit(id, activity);
            return getResponseEntity(updatedActivity);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping(value = "/{id}/delete")
    @Operation(
            summary = "Delete an existing activity",
            description = "Delete an existing activity",
            parameters = {
                    @Parameter(name = "id", description = "Activity ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity deleted successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActivityDTO.class)
            )),
            @ApiResponse(responseCode = "404", description = "Activity not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Activity not found\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Internal server error\"}")
            ))
    })
    public ResponseEntity<?> delete(@PathVariable(value = "id") String id) {
        try {
            ActivityDTO deletedActivity = activityService.delete(id);
            return getResponseEntity(deletedActivity);
        } catch  (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @NotNull
    private ResponseEntity<?> getResponseEntity(ActivityDTO activity) {
        if (activity != null) {
            return ResponseEntity.status(HttpStatus.OK).body(activity);
        } else {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Activity not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}

