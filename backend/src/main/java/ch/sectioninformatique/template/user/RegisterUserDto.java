package ch.sectioninformatique.template.user;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class RegisterUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}