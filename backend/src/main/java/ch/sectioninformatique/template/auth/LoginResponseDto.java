package ch.sectioninformatique.template.auth;

import lombok.Data;

/**
 * Data transfer object for the login response
 */
@Data
public class LoginResponseDto {
    private String token;
    private long expiresIn;
}