package org.springframework.ai.openai.samples.helloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "DTO for structuring course data")
public class CourseDTO {
    @Schema(description = "Unique identifier of the course", example = "123456")
    private String id;

    @Schema(description = "Name of the course", example = "Architecture and Engineering Faculty")
    private String faculty;

    @Schema(description = "Department of the course", example = "Computer Science Department")
    private String department;

    @Schema(description = "Degree of the course", example = "Computing")
    private String degree;

    @Schema(description = "Subject of the course", example = "Fundamentals of Programming")
    private String subject;

    @Schema(description = "Subject code of the course", example = "CS101")
    private String subjectCode;

    @Schema(description = "Modality of the course", example = "MAD")
    private String modality;

    @Schema(description = "Teacher's identifier", example = "123456")
    private String teacherId;

    @Schema(description = "Academic period of the course", example = "Oct24-Feb25")
    private String academicPeriod;

    @Schema(description = "Academic level of the course", example = "1")
    private Integer academicLevel;

    @Schema(description = "Date of creation of the course", example = "2021-10-24T00:00:00.000Z")
    private Date createdAt;
}
