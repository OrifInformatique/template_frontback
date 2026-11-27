package ch.sectioninformatique.template.auth;

import java.util.List;

import ch.sectioninformatique.template.user.UserUpdateDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data transfer object for user registration.
 * This record class holds the information required to create a new user
 * account ({@code POST /users}). Validated as spring-auth validates it (names,
 * email login, password of 8 to 72 characters), to answer with the invalid
 * fields before calling spring-auth. Also used internally, without validation,
 * to register locally a user authenticated by spring-auth.
 *
 * @param firstName         The user's first name
 * @param lastName          The user's last name
 * @param login             The user's login identifier
 * @param password          The user's password as a character array
 * @param mainRole          The user's main role
 * @param appSpecificRoles  The user's appSpecificRoles
 */
public record RegisterDto(
        @NotBlank @Pattern(regexp = UserUpdateDto.NAME_PATTERN) String firstName,
        @NotBlank @Pattern(regexp = UserUpdateDto.NAME_PATTERN) String lastName,
        @NotBlank @Email String login,
        @NotNull @Size(min = 8, max = 72) char[] password,
        String mainRole,
        List<String> appSpecificRoles) {}
