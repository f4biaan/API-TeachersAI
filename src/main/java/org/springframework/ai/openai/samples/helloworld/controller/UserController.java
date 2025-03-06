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
import org.springframework.ai.openai.samples.helloworld.dto.UserDTO;
import org.springframework.ai.openai.samples.helloworld.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

    private final UserManagementService userService;

    @Autowired
    public UserController(UserManagementService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/{id}")
    @Operation(
            summary = "Get a user by ID",
            description = "Retrieves a specific user based on the provided ID.",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the user to retrieve", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = UserDTO.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found for the given ID", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"User not found for ID: 123\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"An unexpected error occurred: Database connection failed\" }")
            ))
    })
    public ResponseEntity<?> getUser(@PathVariable(value = "id") String id) {
        try {
            UserDTO user = userService.getUser(id);
            return getResponseEntity(id, user);
        } catch (NoSuchElementException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 si no existe
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 si hay otros errores
        }
    }

    @GetMapping(value = "/list")
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(type = "object", implementation = UserDTO.class)
                            )
                    )),
            @ApiResponse(responseCode = "204", description = "No users found"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{ \"message\": \"An unexpected error occurred: Database connection failed\" }")
                    ))
    })
    public ResponseEntity<?> listUsers() {
        try {
            List<UserDTO> users = userService.list();
            if (users != null && !users.isEmpty()) {
                return ResponseEntity.ok(users); // Retorna 200 OK con la lista de usuarios
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Retorna 204 si no hay usuarios
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/add")
    @Operation(
            summary = "Add a new user",
            description = "Creates a new user in the system.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", implementation = UserDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = UserDTO.class)
            )),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Invalid data: User ID cannot be null\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"An unexpected error occurred: Database connection failed\" }")
            ))
    })
    public ResponseEntity<?> addUser(@RequestBody UserDTO user) {
        try {
            UserDTO createdUser = userService.add(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser); // 201 con el usuario creado
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 400 si los datos son inválidos
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping(value = "/{id}/update")
    @Operation(
            summary = "Update a user",
            description = "Updates an existing user in the system by the provided ID.",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the user to update", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User data to update", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = UserDTO.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found for the given ID", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"User not found for ID: 123\" }")
            )),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"Invalid data: User ID cannot be null or different from the ID in the URL\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"An unexpected error occurred: Database connection failed\" }")
            ))
    })
    public ResponseEntity<?> edit(@PathVariable(value = "id") String id, @RequestBody UserDTO user) {
        try {
            UserDTO updatedUser = userService.edit(id, user);
            return getResponseEntity(id, updatedUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 400 si los datos son inválidos
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 si hay errores inesperados
        }
    }

    @DeleteMapping(value = "/{id}/delete")
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user from the system by the provided ID.",
            parameters = {
                    @Parameter(name = "id", description = "The ID of the user to delete", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"User deleted successfully\" }")
            )),
            @ApiResponse(responseCode = "404", description = "User not found for the given ID", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"User not found for ID: 123\" }")
            )),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", example = "{ \"message\": \"An unexpected error occurred: Database connection failed\" }")
            ))
    })
    public ResponseEntity<?> delete(@PathVariable(value = "id") String id) {
        try {
            UserDTO deletedUser = userService.delete(id);
            if (deletedUser != null) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "User deleted successfully");
                return ResponseEntity.status(HttpStatus.OK).body(successResponse); // 200 si el usuario se elimina correctamente
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "User not found for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 si el usuario no se encuentra
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 500 si ocurre un error inesperado
        }
    }

    @NotNull
    private ResponseEntity<?> getResponseEntity(@PathVariable("id") String id, UserDTO user) {
        if (user != null) {
            return ResponseEntity.ok(user); // Retorna 200 OK con el usuario
        } else {
            // Si no se encuentra el usuario, devuelve un mensaje de error como un Map
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // 404 si no existe
        }
    }
}
