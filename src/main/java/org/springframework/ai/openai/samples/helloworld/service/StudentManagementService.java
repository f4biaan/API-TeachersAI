package org.springframework.ai.openai.samples.helloworld.service;

import org.springframework.ai.openai.samples.helloworld.dto.StudentDTO;

import java.util.List;

public interface StudentManagementService {
    List<StudentDTO> getStudentsByCourse(String courseId);

    List<String> addStudents(String course, List<StudentDTO> students);

    StudentDTO add(String courseId, StudentDTO student);

    StudentDTO edit(String courseId, StudentDTO student);

    StudentDTO delete(String courseId, String studentId);
}
