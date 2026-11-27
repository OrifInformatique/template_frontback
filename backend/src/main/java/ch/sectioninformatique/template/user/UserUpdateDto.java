package ch.sectioninformatique.template.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request body of a user update ({@code PUT /users/{userId}}).
 *
 * The profile fields (first name, last name, login) are sent to spring-auth,
 * which validates them. The main role, owned by spring-auth, is only changed
 * there if given and different from the current one. The app-specific roles are
 * local to this application, and replaced only if given.
 *
 * @param firstName        new first name
 * @param lastName         new last name, may be null (Azure accounts without last name)
 * @param login            new login (email)
 * @param mainRole         new main role name (USER, MANAGER, ADMIN), null to keep it
 * @param appSpecificRoles new local role names, null to keep them
 */
public record UserUpdateDto(
        @NotBlank @Pattern(regexp = NAME_PATTERN) String firstName,
        @Pattern(regexp = NAME_PATTERN) String lastName,
        @NotBlank @Email String login,
        String mainRole,
        List<String> appSpecificRoles) {

    /**
     * Allowed names, as validated by spring-auth (letters, with inner spaces,
     * apostrophes and hyphens): checked here too, to answer with the invalid
     * field before calling spring-auth.
     */
    public static final String NAME_PATTERN = "^[\\p{L}][\\p{L} '\\-]*[\\p{L}]$";

    /**
     * @return the profile fields, as expected by spring-auth's user update
     */
    public Map<String, String> profile() {
        // HashMap: the last name may be null
        Map<String, String> profile = new HashMap<>();
        profile.put("firstName", firstName);
        profile.put("lastName", lastName);
        profile.put("login", login);
        return profile;
    }
}
