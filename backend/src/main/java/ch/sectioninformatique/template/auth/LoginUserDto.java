package ch.sectioninformatique.template.auth;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class LoginUserDto {
    private String email;
    private String password;
}