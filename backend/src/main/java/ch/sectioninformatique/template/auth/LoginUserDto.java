package ch.sectioninformatique.template.auth;

import lombok.Data;

/**
 * Data transfer object for the login request
 */
@Data
public class LoginUserDto {
    private String email;
    private String password;
}