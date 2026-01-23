package ch.sectioninformatique.template.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserExceptions.UserDeletionException;
import ch.sectioninformatique.template.user.UserExceptions.UserAlreadyHasRoleException;
import ch.sectioninformatique.template.user.UserExceptions.UserPromotionException;
import ch.sectioninformatique.template.user.UserExceptions.UserNotFoundException;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserRepository;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the UserController REST endpoints.
 *
 * This class uses Spring Boot's test context to run against a real
 * application context (with database and services), while using MockMvc
 * to simulate HTTP requests and verify API responses.
 *
 * Tests verify:
 * - User listing with proper authorization
 * - Local user deletion (from database only)
 * - Global user deletion (from database and external auth service)
 * - Error handling for deletion failures
 *
 * It also integrates Spring REST Docs to automatically generate
 * documentation snippets during test execution.
 */
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class UserControllerTest {

    /** MockMvc for simulating HTTP requests in tests */
    @Autowired
    private MockMvc mockMvc;

    /** Real service for user operations (not mocked) */
    @Autowired
    private UserService userService;

    /** Provider for creating JWT tokens */
    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    /** User repository to verify persistence side-effects */
    @Autowired
    private UserRepository userRepository;

    /**
     * Mock client for external auth service (only used for global delete
     * operations)
     */
    @MockitoBean
    private AuthClient authClient;

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
        return userAuthenticationProvider.createToken(userDto);
    }

    /**
     * Helper method for performing and documenting HTTP requests in tests.
     * This reduces repetition by centralizing the request execution and REST Docs
     * generation.
     *
     * @param requestTypeString HTTP method (GET, DELETE, PUT, etc.)
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
            Consumer<ResultActions> script) throws Exception {

        var request = get(endpoint);
        if ("GET".equals(requestTypeString)) {
            request = get(endpoint);
        } else if ("DELETE".equals(requestTypeString)) {
            request = delete(endpoint);
        } else if ("PUT".equals(requestTypeString)) {
            request = put(endpoint);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + requestTypeString);
        }

        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        request.contentType(contentType);

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

        result.andDo(document("users/" + docsFileName, preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));
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
            Consumer<ResultActions> script) throws Exception {
        performRequest(requestTypeString, endpoint, token, contentType, expectedStatus, docsFileName, false, script);
    }

    /**
     * Test: GET /users/all
     *
     * Verify users with user:read authority can retrieve all users from the system.
     */
    @Test
    @Transactional
    public void allUsers_withReadAuthority_shouldReturn200AndUserList() throws Exception {
        String validToken = getValidTokenForUser("test.user@test.com");

        performRequest(
                "GET",
                "/users/all",
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
                });
    }

    /**
     * Test: DELETE /users/{id}/false
     *
     * Verify local user deletion removes user from database without calling
     * external auth service.
     */
    @Test
    @Transactional
    public void deleteLocalUser_withoutGlobalFlag_shouldReturn200AndDeleteFromDatabase() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        // Get a different test user to delete (not the admin performing the deletion)
        UserDto userToDelete = userService.findByLogin("test.user@test.com");
        assertNotNull(userToDelete, "Test user should exist");

        performRequest(
                "DELETE",
                "/users/" + userToDelete.getId() + "/false",
                adminToken,
                MediaType.APPLICATION_JSON,
                200,
                "delete-local",
                true,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value("Local User deleted successfully"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // Verify DB side effect and no call to external auth service
        Optional<User> deleted = userRepository.findByLogin(userToDelete.getLogin());
        assertTrue(deleted.isPresent());
        assertTrue(deleted.get().isDeleted());
        verifyNoInteractions(authClient);
    }

    /**
     * Test: DELETE /users/{id}/true
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

        // Mock the external auth service response
        Map<String, String> authServiceResponse = Map.of(
                "deletedUserLogin", "test.admin2@test.com",
                "message", "User deleted from auth service");
        when(authClient.deleteGlobalUser(eq("Bearer " + adminToken), eq(admin2User.getId())))
                .thenReturn(Mono.just(ResponseEntity.ok(authServiceResponse)));

        performRequest(
                "DELETE",
                "/users/" + admin2User.getId() + "/true",
                adminToken,
                MediaType.APPLICATION_JSON,
                200,
                "delete-global",
                true,
                null);

        // Verify the auth client was called
        verify(authClient).deleteGlobalUser(eq("Bearer " + adminToken), eq(admin2User.getId()));
    }

    /**
     * Test: DELETE /users/{id}/true
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
        when(authClient.deleteGlobalUser(eq("Bearer " + adminToken), eq(managerUser.getId())))
                .thenReturn(Mono.error(new UserDeletionException("Failed to delete user from auth service")));

        performRequest(
                "DELETE",
                "/users/" + managerUser.getId() + "/true",
                adminToken,
                MediaType.APPLICATION_JSON,
                400,
                "delete-global-error",
                true,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value("Failed to delete user: Failed to delete user from auth service"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /users/{userId}/promote-local-app-role
     * Exception: UserPromotionException (400 Bad Request)
     *
     * Documents:
     * - Exception: UserPromotionException
     * - HTTP Status: 400 BAD_REQUEST
     * - When thrown: When user promotion to a role fails (e.g., database error,
     * invalid state)
     * - Use case: Administrator attempts to promote a user but the operation fails
     * - Related exception: UserAlreadyHasRoleException - When user already has the
     * target role
     * - Response: JSON error message with details about promotion failure
     */
    @Test
    @Transactional
    public void promoteLocalAppRole_withValidUser_shouldReturn200Success() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        UserDto userToPromote = userService.findByLogin("test.user@test.com");
        assertNotNull(userToPromote, "Test user should exist");

        performRequest(
                "PUT",
                "/users/" + userToPromote.getId() + "/promote-local-app-role",
                adminToken,
                MediaType.APPLICATION_JSON,
                200,
                "promote-role-success",
                null);
    }

    /**
     * Test: PUT /users/{userId}/promote-local-app-role
     * Exception: UserAlreadyHasRoleException (409 Conflict)
     *
     * Documents:
     * - Exception: UserAlreadyHasRoleException
     * - HTTP Status: 409 CONFLICT
     * - When thrown: When attempting to promote a user to a role they already have
     * - Use case: Administrator tries to promote a user to LOCAL_APP_ROLE but they
     * already have it
     * - Related exception: UserPromotionException - General promotion failures
     * - Response: JSON error message indicating role conflict
     */
    @Test
    @Transactional
    public void promoteLocalAppRole_withAlreadyHasRole_shouldReturn409Conflict() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        UserDto userToPromote = userService.findByLogin("test.user@test.com");
        assertNotNull(userToPromote, "Test user should exist");

        // First promotion should succeed
        performRequest(
                "PUT",
                "/users/" + userToPromote.getId() + "/promote-local-app-role",
                adminToken,
                MediaType.APPLICATION_JSON,
                200,
                "promote-role-success-temp",
                null);

        // Refresh user from database to get updated roles
        userToPromote = userService.findByLogin("test.user@test.com");

        // Second promotion attempt should fail with 409 Conflict
        performRequest(
                "PUT",
                "/users/" + userToPromote.getId() + "/promote-local-app-role",
                adminToken,
                MediaType.APPLICATION_JSON,
                409,
                "promote-role-already-exists",
                null);
    }

    /**
     * Test: DELETE /users/{userId}/{global}
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
        when(authClient.deleteGlobalUser(any(String.class), any(Long.class)))
            .thenReturn(Mono.error(new UserDeletionException("Database constraint violation")));

        performRequest(
                "DELETE",
                "/users/" + userToDelete.getId() + "/true",
                adminToken,
                MediaType.APPLICATION_JSON,
                400,
                "delete-user-deletion-failed",
                true,
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value("Failed to delete user: Database constraint violation"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}/{global}
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
        Long nonExistentUserId = 99999L;

        performRequest(
                "DELETE",
                "/users/" + nonExistentUserId + "/false",
                adminToken,
                MediaType.APPLICATION_JSON,
                404,
                "delete-user-not-found",
                null);
    }
    
    /**
     * Test: Any protected endpoint (e.g., GET /users/all)
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
                "/users/all",
                null, // No token
                MediaType.APPLICATION_JSON,
                401,
                "users-all-unauthorized",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}/{global} without delete authority should return 403.
     */
    @Test
    @Transactional
    public void deleteUser_withInsufficientAuthority_shouldReturn403Forbidden() throws Exception {
        String userToken = getValidTokenForUser("test.user@test.com");
        UserDto targetUser = userService.findByLogin("test.manager@test.com");
        assertNotNull(targetUser, "Target user should exist");

        performRequest(
                "DELETE",
                "/users/" + targetUser.getId() + "/false",
                userToken,
                MediaType.APPLICATION_JSON,
                403,
                "delete-forbidden",
                null);
    }
}
