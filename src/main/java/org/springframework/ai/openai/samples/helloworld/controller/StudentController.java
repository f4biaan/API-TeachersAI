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
import org.springframework.ai.openai.samples.helloworld.dto.StudentDTO;
import org.springframework.ai.openai.samples.helloworld.service.StudentManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Student Management", description = "Endpoints for managing students")
public class StudentController {

    private final StudentManagementService studentService;

    @Autowired
    public StudentController(StudentManagementService studentService) {
        this.studentService = studentService;
    }

    @GetMapping(value = "/course/{courseId}/list")
    @Operation(
            summary = "Get students by course",
            description = "Retrieve a list of students by course",
            parameters = {
                    @Parameter(name = "courseId", description = "The ID of the course to retrieve students for", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully", content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = StudentDTO.class))
            )),
            @ApiResponse(responseCode = "204", description = "No students found for the given course ID"),
            @ApiResponse(responseCode = "404", description = "Course with ID not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Course with ID not found: <courseId>\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"An unexpected error occurred: <error message>\"}")
            ))
    })
    public ResponseEntity<?> getStudentsByCourse(@PathVariable(value = "courseId") String courseId) {
        try {
            List<StudentDTO> students = studentService.getStudentsByCourse(courseId);
            if (students != null && !students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(students); // 200 OK
            } else if (students == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Course with ID not found: " + courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 Not Found
            } else {
                // 204 No Content: Sin cuerpo en la respuesta
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 Internal Server Error
        }
    }

    @PostMapping(value = "/course/{courseId}/add-students")
    @Operation(
            summary = "Add students to course",
            description = "Add a list of students to a course",
            parameters = {
                    @Parameter(name = "courseId", description = "The ID of the course to add students to", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The list of students to add",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = StudentDTO.class))
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students added successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"All students added successfully\"}")
            )),
            @ApiResponse(responseCode = "206", description = "Some students failed to be added", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"message\": \"Some students failed to be added\", \"error\": [\"<studentId>\", \"<studentId>\"]}")
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"<error message>\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Course with ID not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Course with ID not found: <courseId>\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"An unexpected error occurred: <error message>\"}")
            ))
    })
    public ResponseEntity<?> addStudents(@PathVariable(value = "courseId") String courseId, @RequestBody List<StudentDTO> students) {
        try {
            List<String> failedStudents = studentService.addStudents(courseId, students);
            Map<String, Object> response = new HashMap<>();
            if (failedStudents != null && !failedStudents.isEmpty()) {
                response.put("message", "Some students failed to be added");
                response.put("error", failedStudents);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);  // Retornar OK con el mensaje
            } else if (failedStudents == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Course with ID not found: " + courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 Not Found
            } else {
                response.put("message", "All students added successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response); // Retornar OK con la lista de estudiantes
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 400 Bad Request
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 Internal Server Error
        }
    }

    @PostMapping(value = "/course/{courseId}/add")
    @Operation(
            summary = "Add student to course",
            description = "Add a student to a course",
            parameters = {
                    @Parameter(name = "courseId", description = "The ID of the course to add the student to", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The student to add",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student added successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StudentDTO.class)
            )),
            @ApiResponse(responseCode = "404", description = "Course with ID not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Course with ID not found: <courseId>\"}")
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"<error message>\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"An unexpected error occurred: <error message>\"}")
            ))
    })
    public ResponseEntity<?> add(@PathVariable(value = "courseId") String courseId, @RequestBody StudentDTO student) {
        try {
            StudentDTO studentAdded = studentService.add(courseId, student);
            return getResponseEntity(courseId, studentAdded);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 400 Bad Request
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 Internal Server Error
        }
    }

    @PutMapping(value = "/course/{id}/update")
    @Operation(
            summary = "Update student",
            description = "Update a student's included in a course",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the student to edit", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The student to update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StudentDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"<error message>\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Course with ID not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Course with ID not found: <courseId>\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"An unexpected error occurred: <error message>\"}")
            ))
    })
    public ResponseEntity<?> edit(@PathVariable(value = "id") String courseId, @RequestBody StudentDTO student) {
        try {
            StudentDTO updatedStudent = studentService.edit(courseId, student);
            return getResponseEntity(courseId, updatedStudent);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping(value = "/{studentId}/course/{courseId}/delete")
    @Operation(
            summary = "Delete student",
            description = "Delete a student from a course",
            parameters = {
                    @Parameter(name = "studentId", description = "The ID of the student to delete", required = true),
                    @Parameter(name = "courseId", description = "The ID of the course to delete the student from", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student deleted successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StudentDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"<error message>\"}")
            )),
            @ApiResponse(responseCode = "404", description = "Course with ID not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"Course with ID not found: <courseId>\"}")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{\"error\": \"An unexpected error occurred: <error message>\"}")
            ))
    })
    public ResponseEntity<?> delete(@PathVariable(value = "studentId") String studentId, @PathVariable(value = "courseId") String courseId) {
        try {
            StudentDTO deletedStudent = studentService.delete(courseId, studentId);
            return getResponseEntity(courseId, deletedStudent);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorMessage = new HashMap<>();
            errorMessage.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @NotNull
    private ResponseEntity<?> getResponseEntity(@PathVariable("id") String courseId, StudentDTO updatedStudent) {
        if (updatedStudent == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Course with ID not found: " + courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(updatedStudent);
        }
    }
}
