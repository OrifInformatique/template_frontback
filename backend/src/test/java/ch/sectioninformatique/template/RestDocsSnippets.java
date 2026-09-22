package ch.sectioninformatique.template;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Snippet;

/**
 * JSON field contracts for Spring REST Docs in template_frontback.
 * Derived from backend DTOs ({@code UserDto}, {@code RegisterDto}, {@code TokenResponseDto},
 * {@code MessageResponseDto}, {@code PasswordUpdateDto}) and the integration tests that
 * document HTTP examples.
 */
public final class RestDocsSnippets {

    private RestDocsSnippets() {
    }

    /** Matches login request body ({@code login}, {@code password}). */
    public static Snippet loginRequest() {
        return requestFields(
                fieldWithPath("login").description("Email used as login"),
                fieldWithPath("password").description("User password"));
    }

    /** Matches {@code UserDto} returned by auth and user endpoints. */
    public static Snippet userResponse() {
        return responseFields(userFields("", false));
    }

    /** Matches a JSON array of {@code UserDto}. */
    public static Snippet userListResponse() {
        return responseFields(userFields("[].", true));
    }

    /**
     * Matches {@code RegisterDto}. Only {@code firstName}, {@code lastName}, {@code login}
     * and {@code password} are required in documented happy-path tests; role fields are optional.
     */
    public static Snippet registerRequest() {
        return requestFields(
                fieldWithPath("firstName").description("First name"),
                fieldWithPath("lastName").description("Last name"),
                fieldWithPath("login").description("Email used as login"),
                fieldWithPath("password").description("Password"),
                fieldWithPath("mainRole").optional().type(JsonFieldType.STRING)
                        .description("Main role (USER, MANAGER or ADMIN)"),
                fieldWithPath("appSpecificRoles").optional().type(JsonFieldType.ARRAY)
                        .description("App-specific roles assigned at registration"));
    }

    /** Matches {@code TokenResponseDto} ({@code accessToken} only). */
    public static Snippet refreshResponse() {
        return responseFields(
                fieldWithPath("accessToken").description("New JWT access token"));
    }

    /** Matches {@code PasswordUpdateDto}. */
    public static Snippet passwordUpdateRequest() {
        return requestFields(
                fieldWithPath("oldPassword").description("Current password"),
                fieldWithPath("newPassword").description("New password"));
    }

    /** Matches {@code MessageResponseDto}. */
    public static Snippet messageResponse() {
        return responseFields(
                fieldWithPath("message").description("Localized status message"));
    }

    private static FieldDescriptor[] userFields(String prefix, boolean optionalItems) {
        var id = fieldWithPath(prefix + "id").description("User identifier");
        var firstName = fieldWithPath(prefix + "firstName").description("First name");
        var lastName = fieldWithPath(prefix + "lastName").description("Last name");
        var login = fieldWithPath(prefix + "login").description("Email used as login");
        var token = fieldWithPath(prefix + "token").optional().type(JsonFieldType.VARIES)
                .description("JWT access token, present after login or registration");
        var deleted = fieldWithPath(prefix + "deleted").description("Whether the user is soft-deleted");
        var mainRole = fieldWithPath(prefix + "mainRole").description("Main role (USER, MANAGER or ADMIN)");
        var appSpecificRoles = fieldWithPath(prefix + "appSpecificRoles").optional().type(JsonFieldType.ARRAY)
                .description("App-specific roles stored in the local database");
        var permissions = fieldWithPath(prefix + "permissions").optional().type(JsonFieldType.ARRAY)
                .description("Authorities granted to the user");

        if (optionalItems) {
            id = id.optional().type(JsonFieldType.NUMBER);
            firstName = firstName.optional().type(JsonFieldType.STRING);
            lastName = lastName.optional().type(JsonFieldType.STRING);
            login = login.optional().type(JsonFieldType.STRING);
            deleted = deleted.optional().type(JsonFieldType.BOOLEAN);
            mainRole = mainRole.optional().type(JsonFieldType.STRING);
            appSpecificRoles = appSpecificRoles.optional().type(JsonFieldType.ARRAY);
            permissions = permissions.optional().type(JsonFieldType.ARRAY);
        }

        return new FieldDescriptor[] {
                id, firstName, lastName, login, token, deleted, mainRole, appSpecificRoles, permissions
        };
    }
}
