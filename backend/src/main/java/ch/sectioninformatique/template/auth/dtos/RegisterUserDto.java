package ch.sectioninformatique.template.auth.dtos;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class RegisterUserDto {
    private String email;
    private String password;
    private String fullName;
}