package org.springframework.ai.openai.samples.helloworld.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for structuring user data")
public class UserDTO {
    @Schema(description = "Unique identifier of the user", example = "123456")
    private String id;

    @Schema(description = "Email address of the user", example = "user_mail@utpl.edu.ec")
    private String mail;

    @Schema(description = "User's username", example = "user_name")
    private String givenName;

    @Schema(description = "Full name of the user", example = "User Name")
    private String familyName;

    @Schema(description = "Display name of the user", example = "User Name")
    private String displayName;

    @Schema(description = "URL of the user's photo", example = "https://example.com/user_photo.jpg")
    private String photoURL;
}
