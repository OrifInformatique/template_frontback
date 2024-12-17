package ch.sectioninformatique.template.user;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class LoginUserDto {
    private String email;
    private String password;
}