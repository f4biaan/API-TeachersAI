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
import org.springframework.ai.openai.samples.helloworld.dto.CourseDTO;
import org.springframework.ai.openai.samples.helloworld.service.CourseManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Course Management", description = "Endpoints for managing courses")
public class CourseController {

    private final CourseManagementService courseService;

    @Autowired
    public CourseController(CourseManagementService courseService) {
        this.courseService = courseService;
    }

    @GetMapping(value = "/generateId")
    @Operation(summary = "Generate a new course ID", description = "Generate a new course ID for a new course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course ID generated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"id\": \"course_1234\", \"collection\": \"courses\" }"))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }"))
            )
    })
    public ResponseEntity<?> generateId() {
        try {
            HashMap<String, String> response = new HashMap<>();
            response.put("id", courseService.generateId());
            response.put("collection", "courses");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/list")
    @Operation(summary = "List all courses", description = "List all courses in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of courses returned successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CourseDTO.class))
            )),

            @ApiResponse(responseCode = "204", description = "No courses found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }"))
            )
    })
    public ResponseEntity<?> list() {
        try {
            List<CourseDTO> courses = courseService.list();
            if (courses != null && !courses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(courses);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/teacher/{id}")
    @Operation(
            summary = "Get courses by teacher",
            description = "Get all courses taught by a teacher",
            parameters = {
                    @Parameter(name = "id", description = "Teacher ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses returned successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CourseDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No courses found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }")
            ))
    })
    public ResponseEntity<?> getCoursesByTeacher(@PathVariable(value = "id") String id) {
        try {
            List<CourseDTO> courses = courseService.getCoursesByTeacher(id);
            if (courses != null && !courses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(courses);
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping(value = "/{id}")
    @Operation(
            summary = "Get course by ID",
            description = "Get a course by its ID",
            parameters = {
                    @Parameter(name = "id", description = "Course ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course returned successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CourseDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"ID cannot be null or empty\" }")
            )),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Course not found\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }")
            ))
    })
    public ResponseEntity<?> getCourse(@PathVariable(value = "id") String id) {
        try {
            CourseDTO course = courseService.getCourse(id);
            return getResponseEntity(course);
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

    @PostMapping(value = "/add")
    @Operation(
            summary = "Add a new course",
            description = "Add a new course to the system",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Course object to be added",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course added successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CourseDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Course or Course ID cannot be null\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }")
            ))
    })
    public ResponseEntity<?> add(@RequestBody CourseDTO course) {
        try {
            CourseDTO newCourse = courseService.add(course);
            return ResponseEntity.status(HttpStatus.OK).body(newCourse);
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
            summary = "Edit a course",
            description = "Edit an existing course",
            parameters = {
                    @Parameter(name = "id", description = "Course ID", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Course object to be updated",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CourseDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Course or Course ID cannot be null\" }")
            )),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Course not found\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }")
            ))
    })
    public ResponseEntity<?> edit(@PathVariable(value = "id") String id, @RequestBody CourseDTO course) {
        try {
            CourseDTO updatedCourse = courseService.edit(id, course);
            return getResponseEntity(updatedCourse);
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
            summary = "Delete a course",
            description = "Delete a course from the system",
            parameters = {
                    @Parameter(name = "id", description = "Course ID", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course deleted successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CourseDTO.class)
            )),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Course not found\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Internal server error\" }")
            ))
    })
    public ResponseEntity<?> delete(@PathVariable(value = "id") String id) {
        try {
            CourseDTO deletedCourse = courseService.delete(id);
            return getResponseEntity(deletedCourse);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @NotNull
    private ResponseEntity<?> getResponseEntity(CourseDTO course) {
        if (course != null) {
            return ResponseEntity.status(HttpStatus.OK).body(course);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

