package org.springframework.ai.openai.samples.helloworld.service;

import org.springframework.ai.openai.samples.helloworld.dto.UserDTO;

import java.util.List;

public interface UserManagementService {
    List<UserDTO> list();

    UserDTO getUser(String id);

    UserDTO add(UserDTO user);

    UserDTO edit(String id, UserDTO user);

    UserDTO delete(String id);
}
