package org.springframework.ai.openai.samples.helloworld.service;

import org.springframework.ai.openai.samples.helloworld.dto.ActivityDTO;

import java.util.List;

public interface ActivityManagementService {

    List<ActivityDTO> list();

    List<ActivityDTO> getActivitiesByTeacher(String teacherId);

    List<ActivityDTO> getActivitiesByCourse(String courseId);

    ActivityDTO getActivity(String id);

    ActivityDTO getLastUpdated(String id);

    String generateId();

    ActivityDTO add(ActivityDTO activity);

    ActivityDTO edit(String id, ActivityDTO activity);

    ActivityDTO delete(String id);
}
