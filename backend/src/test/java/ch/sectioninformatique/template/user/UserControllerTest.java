package ch.sectioninformatique.template.user;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import ch.sectioninformatique.template.security.MainRoleEnum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static ch.sectioninformatique.template.RestDocsSensitiveDataMasking.maskSensitiveData;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.restdocs.snippet.Snippet;

import ch.sectioninformatique.template.security.SpringAuthPermissions;
import ch.sectioninformatique.template.RestDocsSnippets;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.auth.RegisterDto;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserExceptions.UserDeletionException;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the UserController REST endpoints.
 *
 * This class uses Spring Boot's test context to run against a real
 * application context (with database and services), while using MockMvc
 * to simulate HTTP requests and verify API responses.
 *
 * It also integrates Spring REST Docs to automatically generate
 * documentation snippets during test execution.
 */
@SpringBootTest(classes = AuthApplication.class) // Loads the full Spring Boot application context for tests
@AutoConfigureMockMvc // Automatically configures MockMvc for simulating HTTP requests
@AutoConfigureRestDocs(outputDir = "target/generated-snippets") // Configures REST Docs to generate API documentation
public class UserControllerTest {

    /** Service for handling user-related operations */
    @Autowired
    private UserService userService;

    /** MockMvc for simulating HTTP requests in tests */
    @Autowired
    private MockMvc mockMvc;

    /** Provider for creating JWT tokens */
    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    /** User repository to verify persistence side-effects */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    /**
     * Mock client for external auth service (only used for global delete
     * operations)
     */
    @MockitoBean
    private AuthClient authClient;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Helper method to generate a valid JWT token for a test user by login.
     * Retrieves the user from the database using UserService and creates a real JWT
     * token
     * using UserAuthenticationProvider.
     * 
     * @param login The login email of the user to generate a token for
     * @return A valid JWT token for the specified user
     */
    private String getValidTokenForUser(String login) {
        // Get the user DTO from database (seeded by TestUserSeeder)
        UserDto userDto = userService.findByLogin(login);

        if (userDto == null) {
            throw new RuntimeException("User " + login + " not found. Ensure TestUserSeeder has run.");
        }

        // Create and return a real JWT token
        return userAuthenticationProvider.createToken(SpringAuthPermissions.grant(userDto));
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private UserDto createTemporaryUser() {
        String uniqueLogin = "temp.permanent." + System.currentTimeMillis() + "@test.com";
        // Explicit id (no longer generated) that does not collide with TestUserSeeder (1-4)
        userService.register(new RegisterDto("Temp", "User", uniqueLogin, null, null, null), 1000L);
        return userService.findByLogin(uniqueLogin);
    }

    /**
     * Helper method for performing and documenting HTTP requests in tests.
     * This reduces repetition by centralizing the request execution and REST Docs
     * generation.
     *
     * @param requestTypeString HTTP method (GET, POST, PUT, DELETE, etc.)
     * @param endpoint          API endpoint to call
     * @param token             Optional JWT token for authentication
     * @param contentType       Content type for the request
     * @param expectedStatus    Expected HTTP status code (e.g. 200)
     * @param docsFileName      Name for the generated REST Docs snippet
     * @param handleAsync       Whether to handle async request/response
     * @param script            Optional lambda to perform additional assertions
     * 
     * @throws Exception
     */
    private void performRequest(
            String requestTypeString,
            String endpoint,
            String token,
            MediaType contentType,
            int expectedStatus,
            String docsFileName,
            boolean handleAsync,
            Consumer<ResultActions> script,
            Snippet... snippets) throws Exception {
        performRequestWithBody(requestTypeString, endpoint, null, token, contentType, expectedStatus,
                docsFileName, handleAsync, script, snippets);
    }

    private void performRequestWithBody(
            String requestTypeString,
            String endpoint,
            String body,
            String token,
            MediaType contentType,
            int expectedStatus,
            String docsFileName,
            boolean handleAsync,
            Consumer<ResultActions> script,
            Snippet... snippets) throws Exception {

        var request = switch (requestTypeString) {
            case "GET" -> get(endpoint);
            case "DELETE" -> delete(endpoint);
            case "PUT" -> put(endpoint);
            case "POST" -> post(endpoint);
            default -> throw new IllegalArgumentException("Unsupported request type: " + requestTypeString);
        };

        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        request.contentType(contentType);
        if (body != null) {
            request.content(body);
        }

        var result = mockMvc.perform(request);

        if (handleAsync) {
            result.andExpect(request().asyncStarted());
            var mvcResult = result.andReturn();
            result = mockMvc.perform(asyncDispatch(mvcResult));
        }

        result.andExpect(status().is(expectedStatus));

        if (script != null) {
            script.accept(result);
        }

        result.andDo(document("users/" + docsFileName, preprocessRequest(maskSensitiveData(), prettyPrint()),
                preprocessResponse(maskSensitiveData(), prettyPrint()), snippets));
    }

    /**
     * Overloaded helper for non-async requests (backward compatibility)
     */
    private void performRequest(
        String requestTypeString,
        String endpoint,
        String token,
        MediaType contentType,
        int expectedStatus,
        String docsFileName,
        Consumer<ResultActions> script,
        Snippet... snippets) throws Exception {

            performRequest(requestTypeString, endpoint, token, contentType, expectedStatus, docsFileName, false, script, snippets);
    }

    // ==================== GET /users/me ====================

    /**
     * Test: GET /users/me - Success
     *
     * Test retrieving the current authenticated user with a valid token.
     */
    @Test
    public void me_withToken_shouldReturnSuccess() throws Exception {
        String validToken = getValidTokenForUser("test.user@test.com");
        performRequest(
            "GET",
            "/users/me",
            validToken,
            MediaType.APPLICATION_JSON,
            200,
            "me-success",
            response -> {
                try {
                    response.andExpect(status().isOk());
                    response.andExpect(jsonPath("$.login")
                        .value("test.user@test.com"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            RestDocsSnippets.userResponse());
    }

    /**
     * Test: GET /users/me - 401 Unauthorized
     *
     * Test retrieving the current authenticated user without a token.
     */
    @Test
    public void me_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
            "GET",
            "/users/me",
            null,
            MediaType.APPLICATION_JSON,
            401,
            "me-unauthorized-missing-token",
            response -> {
                try {
                    response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    // ==================== GET /users ====================

    /**
     * Test: GET /users?status=active - Success
     *
     * Test retrieving active users with proper authorization.
     */
    @Test
    public void allUsers_withToken_shouldReturnSuccess() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        performRequest(
            "GET",
            "/users?status=ACTIVE",
            adminToken,
            MediaType.APPLICATION_JSON,
            200,
            "all-users-success",
            response -> {
                try {
                    response.andExpect(status().isOk());
                    response.andExpect(jsonPath("$").isArray());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * Test: GET /users?status=active
     *
     * Verify users with user:read authority can retrieve the user list.
     */
    @Test
    @Transactional
    public void allUsers_withReadAuthority_shouldReturn200AndUserList() throws Exception {
        String validToken = getValidTokenForUser("test.user@test.com");

        performRequest(
            "GET",
            "/users?status=ACTIVE",
            validToken,
            MediaType.APPLICATION_JSON,
            200,
            "all-users",
            response -> {
                try {
                    // Verify response is an array
                    response.andExpect(jsonPath("$").isArray());
                    // Verify at least the test users exist
                    response.andExpect(jsonPath("$[?(@.login == 'test.user@test.com')]").exists());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            RestDocsSnippets.userListResponse());
    }

    /**
     * Test: GET /users - 401 Unauthorized
     *
     * Test retrieving all users without proper authorization.
     */
    @Test
    public void allUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "GET",
                "/users",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "all-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== GET /users?status=all ====================

    /**
     * Test: GET /users?status=all - Success
     *
     * Test retrieving all users including soft-deleted ones with proper authorization.
     */
    @Test
    public void allWithDeletedUsers_withToken_shouldReturnSuccess() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        performRequest(
                "GET",
                "/users?status=ALL",
                adminToken,
                MediaType.APPLICATION_JSON,
                200,
                "all-with-deleted-success",
                response -> {
                    try {
                        response.andExpect(status().isOk());
                        response.andExpect(jsonPath("$").isArray());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    /**
     * Test: DELETE /users/{id} (local soft delete, default)
     *
     * Verify local user deletion removes or flags the user deleted without calling
     * external auth service.
     */
    @Test
    @Transactional
    public void deleteLocalUser_withDefaultScope_shouldReturn204() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        // Get a different test user to delete (not the admin performing the deletion)
        UserDto userToDelete = userService.findByLogin("test.user@test.com");
        assertNotNull(userToDelete, "Test user should exist");

        performRequest(
                "DELETE",
                "/users/" + userToDelete.getId(),
                adminToken,
                MediaType.APPLICATION_JSON,
                204,
                "delete-local-success",
                true,
                response -> {
                    try {
                        response.andDo(print());
                        response.andExpect(status().isNoContent());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // Verify DB side effect (soft or hard delete) and no call to external auth service
        assertUserDeleted(userToDelete);
        verifyNoInteractions(authClient);
    }

    /**
     * Test: DELETE /users/{id}?scope=GLOBAL
     *
     * Verify global user deletion calls external auth service and removes from
     * database.
     */
    @Test
    @Transactional
    public void deleteGlobalUser_withGlobalFlag_shouldCallAuthServiceAndDeleteFromDatabase() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        // Get a test admin2 user to delete
        UserDto admin2User = userService.findByLogin("test.admin2@test.com");
        assertNotNull(admin2User, "Test admin2 user should exist");

        // Mock the external auth service response (204 No Content)
        when(authClient.deleteGlobalUser(eq("Bearer " + adminToken), eq(admin2User.getId()), eq(false)))
                .thenReturn(Mono.just(ResponseEntity.noContent().build()));

        performRequest(
                "DELETE",
                "/users/" + admin2User.getId() + "?scope=GLOBAL",
                adminToken,
                MediaType.APPLICATION_JSON,
                204,
                "delete-global",
                true,
                null);

        // Verify the auth client was called
        verify(authClient).deleteGlobalUser(eq("Bearer " + adminToken), eq(admin2User.getId()), eq(false));
        // Verify DB side effect (soft or hard delete)
        assertUserDeleted(admin2User);
    }

    /**
     * Assert the user is either absent (hard delete) or marked as deleted (soft delete).
     */
    private void assertUserDeleted(UserDto userDto) {
        Optional<User> deleted = userRepository.findByLogin(userDto.getLogin());
        boolean removedOrFlagged = deleted.isEmpty() || deleted.map(User::isDeleted).orElse(false);
        assertTrue(removedOrFlagged, "User should be removed or flagged as deleted in the database");
    }

    /**
     * Test: DELETE /users/{id}?scope=GLOBAL
     *
     * Verify error handling when global user deletion fails in external auth
     * service.
     */
    @Test
    @Transactional
    public void deleteGlobalUser_whenAuthServiceFails_shouldPropagateError() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        // Get a test manager user
        UserDto managerUser = userService.findByLogin("test.manager@test.com");
        assertNotNull(managerUser, "Test manager user should exist");

        // Mock the external auth service to return an error
        when(authClient.deleteGlobalUser(eq("Bearer " + adminToken), eq(managerUser.getId()), eq(false)))
                .thenReturn(Mono.error(new UserDeletionException("Failed to delete user from auth service")));

        performRequest(
                "DELETE",
                "/users/" + managerUser.getId() + "?scope=GLOBAL",
                adminToken,
                MediaType.APPLICATION_JSON,
                400,
                "delete-global-error",
                true,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(
                                getMessage("user.delete.failed", "Failed to delete user from auth service")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }



    /**
     * Test: DELETE /users/{userId}?scope=&permanent=
     * Exception: UserDeletionException (400 Bad Request)
     *
     * Documents:
     * - Exception: UserDeletionException
     * - HTTP Status: 400 BAD_REQUEST
     * - When thrown: When user deletion operation fails (e.g., database error,
     * referential constraints)
     * - Use case: Administrator attempts to delete a user but operation fails at
     * service level
     * - Related exceptions:
     * - UserAlreadyDeletedException: User was already deleted
     * - PermanentUserDeletionException: Permanent deletion fails
     * - Response: JSON error message with details about deletion failure
     * - Note: LocalUserDeletion vs GlobalUserDeletion both can throw this
     */
    @Test
    @Transactional
    public void deleteUser_withDeletionFailure_shouldReturn400UserDeletionFailed() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        UserDto userToDelete = userService.findByLogin("test.manager@test.com");
        assertNotNull(userToDelete, "Test manager user should exist");

        // Mock auth client to return error for global delete
        when(authClient.deleteGlobalUser(any(String.class), any(Long.class), eq(false)))
            .thenReturn(Mono.error(new UserDeletionException("Database constraint violation")));

        performRequest(
                "DELETE",
                "/users/" + userToDelete.getId() + "?scope=GLOBAL",
                adminToken,
                MediaType.APPLICATION_JSON,
                400,
                "delete-user-deletion-failed",
                true,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(
                                getMessage("user.delete.failed", "Database constraint violation")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}?scope=&permanent=
     * Exception: UserNotFoundException (404 Not Found)
     *
     * Documents:
     * - Exception: UserNotFoundException (from UserExceptions)
     * - HTTP Status: 404 NOT_FOUND
     * - When thrown: When querying or operating on a user that doesn't exist
     * - Use case: Administrator tries to delete a user with invalid ID
     * - Related exception: UserNotFoundByLoginException - For finding by login
     * - Note: Different from AuthExceptions.UserNotFoundException which is auth-specific
     * - Response: JSON error message indicating user was not found
     */
    @Test
    @Transactional
    public void deleteUser_withNonExistentId_shouldReturn404UserNotFound() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        long nonExistentUserId = 999_999L;

        performRequest(
                "DELETE",
                "/users/" + nonExistentUserId + "",
                adminToken,
                MediaType.APPLICATION_JSON,
                404,
                "delete-user-not-found",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("user.notFound")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    
    /**
     * Test: Any protected endpoint (e.g., GET /users?status=active)
     * Exception: SecurityExceptions.AuthenticationRequiredException (401
     * Unauthorized)
     *
     * Documents:
     * - Exception: AuthenticationRequiredException (from SecurityExceptions)
     * - HTTP Status: 401 UNAUTHORIZED
     * - When thrown: When authentication is required but not provided
     * - Use case: User tries to access protected endpoint without authorization
     * header or token
     * - Related exceptions:
     * - MissingAuthorizationHeaderException: Authorization header is absent
     * - InvalidAuthorizationHeaderException: Header format is incorrect
     * - InvalidTokenException: Token is invalid/expired
     * - Response: JSON error message from UserAuthenticationEntryPoint
     */
    @Test
    @Transactional
    public void allUsers_withoutAuthentication_shouldReturn401Unauthorized() throws Exception {
        performRequest(
                "GET",
                "/users?status=ACTIVE",
                null, // No token
                MediaType.APPLICATION_JSON,
                401,
                "users-all-unauthorized",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /users?status=all - 401 Unauthorized
     *
     * Test retrieving all users including soft-deleted ones without proper authorization.
     */
    @Test
    public void allWithDeletedUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "GET",
                "/users?status=ALL",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "all-with-deleted-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== GET /users?status=deleted ====================

    /**
     * Test: GET /users?status=deleted - Success
     *
     * Test retrieving only soft-deleted users with proper authorization.
     */
    @Test
    public void deletedUsers_withToken_shouldReturnSuccess() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        performRequest(
                "GET",
                "/users?status=DELETED",
                adminToken,
                MediaType.APPLICATION_JSON,
                200,
                "deleted-users-success",
                response -> {
                    try {
                        response.andExpect(status().isOk());
                        response.andExpect(jsonPath("$").isArray());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /users?status=deleted - 401 Unauthorized
     *
     * Test retrieving only soft-deleted users without proper authorization.
     */
    @Test
    public void deletedUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "GET",
                "/users?status=DELETED",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "deleted-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /users?status=bogus - 400 Bad Request
     *
     * An unknown value for the {@code state} filter is rejected with a
     * standardized error body.
     */
    @Test
    public void listUsers_withUnknownState_shouldReturnBadRequest() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        performRequest(
                "GET",
                "/users?status=BOGUS",
                adminToken,
                MediaType.APPLICATION_JSON,
                400,
                "list-invalid-state",
                response -> {
                    try {
                        response.andExpect(status().isBadRequest());
                        response.andExpect(jsonPath("$.message").exists());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    // ==================== DELETE /users/{userId} (scope=LOCAL) ====================

    /**
     * Test: DELETE /users/{userId} (scope=LOCAL) 401 Unauthorized
     *
     * Test soft deleting a user locally without authorization.
     */
    @Test
    @Transactional
    public void deleteUser_locallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        UserDto userToDelete = userService.findByLogin("test.user@test.com");
        performRequest(
                "DELETE",
                "/users/" + userToDelete.getId(),
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-local-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}?scope=GLOBAL ====================

    /**
     * Test: DELETE /users/{userId}?scope=GLOBAL Success with Mocked WebClient
     *
     * Test soft deleting a user globally with mocked webclient response.
     */
    @Test
    @Transactional
    public void deleteUser_globally_withMockedWebClient_shouldReturnSuccess() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        UserDto userToDelete = userService.findByLogin("test.user@test.com");
        assertNotNull(userToDelete, "Test user should exist");

        when(authClient.deleteGlobalUser(anyString(), anyLong(), eq(false)))
                .thenReturn(Mono.just(ResponseEntity.noContent().build()));

        performRequest(
                "DELETE",
            "/users/" + userToDelete.getId() + "?scope=GLOBAL",
                adminToken,
                MediaType.APPLICATION_JSON,
                204,
                "delete-global-success",
                true,
                response -> {
                    try {
                        response.andExpect(status().isNoContent());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}?scope=GLOBAL 401 Unauthorized
     *
     * Test soft deleting a user globally without authorization.
     */
    @Test
    @Transactional
    public void deleteUser_globallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/2?scope=GLOBAL",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-global-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}?permanent=true (scope=LOCAL) ====================

    /**
     * Test: DELETE /users/{userId}?permanent=true (scope=LOCAL) Success
     *
     * Test permanently deleting a user locally with proper authorization.
     */
    @Test
    @Transactional
    public void deleteUserPermanent_locally_shouldReturnSuccess() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        UserDto userToDelete = createTemporaryUser();
        assertNotNull(userToDelete, "Temporary user should exist");
        performRequest(
                "DELETE",
                "/users/" + userToDelete.getId() + "?permanent=true",
                adminToken,
                MediaType.APPLICATION_JSON,
                204,
                "delete-permanent-local-success",
                true, // Controller returns Mono, so MockMvc must async-dispatch to get the JSON body.
                response -> {
                    try {
                        response.andDo(print());
                        response.andExpect(status().isNoContent());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}?permanent=true (scope=LOCAL) 401 Unauthorized
     *
     * Test permanently deleting a user locally without proper authorization.
     */
    @Test
    public void deleteUserPermanent_locallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/2?permanent=true",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-permanent-local-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}?scope=GLOBAL&permanent=true ====================

    /**
     * Test: DELETE /users/{userId}?scope=GLOBAL&permanent=true Success with Mocked WebClient
     *
     * Test permanently deleting a user globally with mocked webclient response.
     */
    @Test
    @Transactional
    public void deleteUserPermanent_globally_withMockedWebClient_shouldReturnSuccess() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        UserDto userToDelete = createTemporaryUser();
        assertNotNull(userToDelete, "Temporary user should exist");

        when(authClient.deleteGlobalUser(anyString(), anyLong(), eq(true)))
                .thenReturn(Mono.just(ResponseEntity.noContent().build()));

        performRequest(
                "DELETE",
            "/users/" + userToDelete.getId() + "?scope=GLOBAL&permanent=true",
                adminToken,
                MediaType.APPLICATION_JSON,
                204,
                "delete-permanent-global-success",
                true,
                response -> {
                    try {
                        response.andDo(print());
                        response.andExpect(status().isNoContent());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // Hard delete: the row must be gone, not just flagged as deleted.
        // JPQL query (not findById) so the first-level cache can't hide the result.
        assertTrue(userRepository.findAllIncludingDeleted().stream()
                .noneMatch(u -> userToDelete.getId().equals(u.getId())),
                "User should be permanently removed from the database");
    }

    /**
     * Test: DELETE /users/{userId}?scope=GLOBAL&permanent=true 401 Unauthorized
     *
     * Test permanently deleting a user globally without authorization.
     */
    @Test
    @Transactional
    public void deleteUserPermanent_globallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/2?scope=GLOBAL&permanent=true",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-permanent-global-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                        response.andExpect(jsonPath("$.message")
                            .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }










    
    /**
     * Test: DELETE /users/{userId} without delete authority should return 403.
     */
    @Test
    @Transactional
    public void deleteUser_withInsufficientAuthority_shouldReturn403Forbidden() throws Exception {
        String userToken = getValidTokenForUser("test.user@test.com");
        UserDto targetUser = userService.findByLogin("test.manager@test.com");
        assertNotNull(targetUser, "Target user should exist");

        performRequest(
                "DELETE",
                "/users/" + targetUser.getId(),
                userToken,
                MediaType.APPLICATION_JSON,
                403,
                "delete-forbidden",
                null);
    }

    // ==================== GET /users/me: local roles and all permissions ====================

    /**
     * Test: GET /users/me returns the global permissions (from spring-auth) and
     * the local ones (from this application), like the login response.
     */
    @Test
    @Transactional
    public void me_shouldReturnGlobalAndLocalPermissions() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidTokenForUser("test.user@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions", org.hamcrest.Matchers.hasItems("user:read", "item:read")))
                .andExpect(jsonPath("$.appSpecificRoles").isArray());
    }

    // ==================== GET /users/{id} ====================

    /**
     * Test: GET /users/{id} returns the user.
     */
    @Test
    @Transactional
    public void getUser_withReadAuthority_shouldReturnUser() throws Exception {
        UserDto user = userService.findByLogin("test.manager@test.com");
        performRequest(
                "GET",
                "/users/" + user.getId(),
                getValidTokenForUser("test.admin@test.com"),
                MediaType.APPLICATION_JSON,
                200,
                "get-user-success",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.id").value(user.getId()));
                        response.andExpect(jsonPath("$.login").value("test.manager@test.com"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.userResponse());
    }

    /**
     * Test: GET /users/{id} also returns a soft-deleted user, flagged as deleted.
     */
    @Test
    @Transactional
    public void getUser_softDeleted_shouldReturnUserFlaggedDeleted() throws Exception {
        UserDto user = userService.findByLogin("test.manager@test.com");
        userService.deleteUser(user.getId());

        mockMvc.perform(get("/users/" + user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidTokenForUser("test.admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    /**
     * Test: GET /users/{id} with an unknown id returns 404.
     */
    @Test
    @Transactional
    public void getUser_unknownId_shouldReturn404() throws Exception {
        performRequest(
                "GET",
                "/users/999999",
                getValidTokenForUser("test.admin@test.com"),
                MediaType.APPLICATION_JSON,
                404,
                "get-user-not-found",
                null);
    }

    // ==================== PUT /users/{id} ====================

    /**
     * Test: PUT /users/{id} updates the user in spring-auth and locally, and
     * returns the updated user.
     */
    @Test
    @Transactional
    public void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto user = userService.findByLogin("test.manager@test.com");
        UserDto globalUser = UserDto.builder()
                .id(user.getId())
                .firstName("Renamed")
                .lastName("Manager")
                .login("test.manager@test.com")
                .mainRole("MANAGER")
                .build();
        when(authClient.updateUser(anyString(), eq(user.getId()), any()))
                .thenReturn(Mono.just(ResponseEntity.ok(globalUser)));

        performRequestWithBody(
                "PUT",
                "/users/" + user.getId(),
                "{\"firstName\":\"Renamed\",\"lastName\":\"Manager\",\"login\":\"test.manager@test.com\","
                        + "\"mainRole\":\"MANAGER\",\"appSpecificRoles\":[\"LOCAL_MANAGER\"]}",
                getValidTokenForUser("test.admin@test.com"),
                MediaType.APPLICATION_JSON,
                200,
                "update-success",
                false,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.firstName").value("Renamed"));
                        response.andExpect(jsonPath("$.appSpecificRoles[0]").value("LOCAL_MANAGER"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.userUpdateRequest(),
                RestDocsSnippets.userResponse());
    }

    /**
     * Test: PUT /users/{id} with an invalid body returns 400 with the invalid
     * fields, without calling spring-auth.
     */
    @Test
    @Transactional
    public void updateUser_withInvalidBody_shouldReturn400WithFieldErrors() throws Exception {
        UserDto user = userService.findByLogin("test.manager@test.com");
        performRequestWithBody(
                "PUT",
                "/users/" + user.getId(),
                "{\"firstName\":\"\",\"lastName\":\"Probe2\",\"login\":\"not-an-email\"}",
                getValidTokenForUser("test.admin@test.com"),
                MediaType.APPLICATION_JSON,
                400,
                "update-validation-error",
                false,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.fieldErrors.firstName").exists());
                        response.andExpect(jsonPath("$.fieldErrors.login").exists());
                        // Names are validated as by spring-auth: letters only (no digit)
                        response.andExpect(jsonPath("$.fieldErrors.lastName").exists());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        verifyNoInteractions(authClient);
    }

    // ==================== POST /users/{id}/restore ====================

    /**
     * Test: POST /users/{id}/restore restores the user and returns it.
     */
    @Test
    @Transactional
    public void restoreUser_shouldReturnRestoredUser() throws Exception {
        UserDto user = userService.findByLogin("test.manager@test.com");
        userService.deleteUser(user.getId());
        when(authClient.restoreGlobalUser(anyString(), eq(user.getId())))
                .thenReturn(Mono.just(ResponseEntity.noContent().build()));

        performRequest(
                "POST",
                "/users/" + user.getId() + "/restore",
                getValidTokenForUser("test.admin@test.com"),
                MediaType.APPLICATION_JSON,
                200,
                "restore-success",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.deleted").value(false));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.userResponse());
        assertFalse(userRepository.findById(user.getId()).orElseThrow().isDeleted());
    }

    // ==================== DELETE /users/{id}: parameters ====================

    /**
     * Test: DELETE /users/{id} with an unknown scope returns 400.
     */
    @Test
    @Transactional
    public void deleteUser_withUnknownScope_shouldReturn400() throws Exception {
        UserDto user = userService.findByLogin("test.manager@test.com");
        mockMvc.perform(delete("/users/" + user.getId() + "?scope=EVERYWHERE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidTokenForUser("test.admin@test.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("scope")));
        verifyNoInteractions(authClient);
    }
}
