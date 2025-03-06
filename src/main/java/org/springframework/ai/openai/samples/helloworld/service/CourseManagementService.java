package org.springframework.ai.openai.samples.helloworld.service;

import org.springframework.ai.openai.samples.helloworld.dto.CourseDTO;

import java.util.List;

public interface CourseManagementService {

    List<CourseDTO> list();

    List<CourseDTO> getCoursesByTeacher(String teacherId);

    CourseDTO getCourse(String id);

    String generateId();

    CourseDTO add(CourseDTO activity);

    CourseDTO edit(String id, CourseDTO activity);

    CourseDTO delete(String id);
}
