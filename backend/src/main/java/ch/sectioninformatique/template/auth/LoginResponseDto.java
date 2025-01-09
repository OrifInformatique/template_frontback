package ch.sectioninformatique.template.auth;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class LoginResponseDto {
    private String token;
    private long expiresIn;
}