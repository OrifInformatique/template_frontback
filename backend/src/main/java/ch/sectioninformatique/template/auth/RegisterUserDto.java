package ch.sectioninformatique.template.auth;

import lombok.Data;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * Data transfer object for the registration request
 */
@Data
public class RegisterUserDto {
    @NotNull(message = "Le prénom est requis")
    @Size(min = 3, message = "Le prénom doit contenir au moins 3 caractères")
    @Pattern(
        regexp = "^[a-zA-ZÀ-ÿ\\-\\s]*$",
        message = "Le prénom ne doit contenir que des lettres"
    )
    private String firstName;

    @NotNull(message = "Le nom est requis")
    @Size(min = 3, message = "Le nom doit contenir au moins 3 caractères")
    @Pattern(
        regexp = "^[a-zA-ZÀ-ÿ\\-\\s]*$",
        message = "Le nom ne doit contenir que des lettres"
    )
    private String lastName;

    @NotNull(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotNull(message = "Le mot de passe est requis")
    @Size(min = 8, max = 50, message = "Le mot de passe doit contenir entre 8 et 50 caractères")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    public RegisterUserDto() {
    }
    /**
     * Constructor for the RegisterUserDto class
     * 
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param email The email of the user
     * @param password The password of the user
     */
    public RegisterUserDto(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    /**
     * Set the first name of the user
     * 
     * @param firstName The first name of the user
     * @return The RegisterUserDto object
     */
    public RegisterUserDto setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Set the last name of the user
     * 
     * @param lastName The last name of the user
     * @return The RegisterUserDto object
     */
    public RegisterUserDto setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Set the email of the user
     * 
     * @param email The email of the user
     * @return The RegisterUserDto object
     */
    public RegisterUserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Set the password of the user
     * 
     * @param password The password of the user
     * @return The RegisterUserDto object
     */
    public RegisterUserDto setPassword(String password) {
        this.password = password;
        return this;
    }
}