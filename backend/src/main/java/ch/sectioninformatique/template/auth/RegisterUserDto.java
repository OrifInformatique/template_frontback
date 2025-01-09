package ch.sectioninformatique.template.auth;

import lombok.Data;

import java.util.Objects;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
public class RegisterUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public RegisterUserDto() {
    }

    public RegisterUserDto(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public RegisterUserDto setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public RegisterUserDto setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public RegisterUserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public RegisterUserDto setPassword(String password) {
        this.password = password;
        return this;
    }
}