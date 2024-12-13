package ch.sectioninformatique.template.auth;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class LoginResponse {
    private String token;
    private long expiresIn;
}