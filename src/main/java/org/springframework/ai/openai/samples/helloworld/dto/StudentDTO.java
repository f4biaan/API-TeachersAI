package org.springframework.ai.openai.samples.helloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for structuring student data")
public class StudentDTO {
    @Schema(description = "Unique identifier of the student", example = "123456")
    private String id;

    @Schema(description = "Email address of the student", example = "student@example.com")
    private String email;

    @Schema(description = "Student's username", example = "student_user")
    private String username;

    @Schema(description = "Full name of the student", example = "Student Name")
    private String name;
}
