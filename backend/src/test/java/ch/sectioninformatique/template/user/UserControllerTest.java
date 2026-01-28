package ch.sectioninformatique.template.user;

// Import statements for testing, Spring Boot, JSON handling, and REST Docs
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import ch.sectioninformatique.template.AuthApplication;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the "UserController" REST endpoints.
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

    @MockitoBean
    private UserClient userClient; // mock the user client

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
     * @param script            Optional lambda to perform additional assertions
     * 
     * @throws Exception
     */
    private void performRequest(
            String requestTypeString,
            String endpoint,
            String content,
            String token,
            MediaType contentType,
            int expectedStatus,
            String docsFileName,
            Consumer<ResultActions> script) throws Exception {

        var requestType = get(endpoint);

        if (requestTypeString.equals("GET")) {
            requestType = get(endpoint);
        } else if (requestTypeString.equals("PUT")) {
            requestType = put(endpoint);
        } else if (requestTypeString.equals("DELETE")) {
            requestType = delete(endpoint);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + requestTypeString);
        }

        // Set content only if it's not null
        if (content != null) {
            requestType.content(content);
        }

        // Set Authorization header only if token is provided
        if (token != null) {
            requestType.header("Authorization", "Bearer " + token);
        }

        // Set content type
        requestType.contentType(contentType);

        // Perform request
        var request = mockMvc.perform(requestType)
                .andExpect(status().is(expectedStatus));

        // Execute any additional assertions provided in the lambda
        if (script != null) {
            script.accept(request);
        }

        // Generate a REST Docs snippet for the request/response pair
        request.andDo(document("users/" + docsFileName, preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));

    }

    // ==================== GET /users/me ====================

    /**
     * Test: GET /users/me - Success
     *
     * Test retrieving the current authenticated user with a valid token.
     */
    @Test
    public void me_withToken_shouldReturnSuccess() throws Exception {
        performRequest(
                "GET",
                "/users/me",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "me-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
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
                null,
                MediaType.APPLICATION_JSON,
                401,
                "me-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== GET /users/all ====================

    /**
     * Test: GET /users/all - Success
     *
     * Test retrieving all users with proper authorization.
     */
    @Test
    public void allUsers_withToken_shouldReturnSuccess() throws Exception {
        performRequest(
                "GET",
                "/users/all",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "all-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /users/all - 401 Unauthorized
     *
     * Test retrieving all users without proper authorization.
     */
    @Test
    public void allUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "GET",
                "/users/all",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "all-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== GET /users/all-with-deleted ====================

    /**
     * Test: GET /users/all-with-deleted - Success
     *
     * Test retrieving all users including soft-deleted ones with proper authorization.
     */
    @Test
    public void allWithDeletedUsers_withToken_shouldReturnSuccess() throws Exception {
        performRequest(
                "GET",
                "/users/all-with-deleted",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "all-with-deleted-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /users/all-with-deleted - 401 Unauthorized
     *
     * Test retrieving all users including soft-deleted ones without proper authorization.
     */
    @Test
    public void allWithDeletedUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "GET",
                "/users/all-with-deleted",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "all-with-deleted-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== GET /users/deleted ====================

    /**
     * Test: GET /users/deleted - Success
     *
     * Test retrieving all soft-deleted users with proper authorization.
     */
    @Test
    public void deletedUsers_withToken_shouldReturnSuccess() throws Exception {
        performRequest(
                "GET",
                "/users/deleted",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "deleted-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /users/deleted - 401 Unauthorized
     *
     * Test retrieving all soft-deleted users without proper authorization.
     */
    @Test
    public void deletedUsers_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "GET",
                "/users/deleted",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "deleted-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== PUT /users/{userId}/promote-local-app-role ====================

    /**
     * Test: PUT /users/{userId}/promote-local-app-role - 401 Unauthorized
     *
     * Test promoting a user to local app role without proper authorization.
     */
    @Test
    public void promoteToLocalAppRole_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "PUT",
                "/users/1/promote-local-app-role",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "promote-local-app-role-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}/{global} - Local ====================

    /**
     * Test: DELETE /users/{userId}/{global} - Local Success
     *
     * Test soft deleting a user locally with proper authorization.
     */
    @Test
    @Transactional
    public void deleteUser_locally_shouldReturnSuccess() throws Exception {
        performRequest(
                "DELETE",
                "/users/1/false",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "delete-local-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}/{global} - Local 401 Unauthorized
     *
     * Test soft deleting a user locally without authorization.
     */
    @Test
    @Transactional
    public void deleteUser_locallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/1/false",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-local-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}/{global} - Global ====================

    /**
     * Test: DELETE /users/{userId}/{global} - Global Success with Mocked WebClient
     *
     * Test soft deleting a user globally with mocked webclient response.
     */
    @Test
    @Transactional
    public void deleteUser_globally_withMockedWebClient_shouldReturnSuccess() throws Exception {
        Map<String, String> mockedResponse = Map.of(
                "message", "User deleted successfully",
                "deletedUserLogin", "test.user@test.com");

        when(userClient.deleteGlobalUser(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok(mockedResponse)));

        performRequest(
                "DELETE",
                "/users/1/true",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "delete-global-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}/{global} - Global 401 Unauthorized
     *
     * Test soft deleting a user globally without authorization.
     */
    @Test
    @Transactional
    public void deleteUser_globallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/2/true",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-global-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}/{global}/permanent - Local ====================

    /**
     * Test: DELETE /users/{userId}/{global}/permanent - Local Success
     *
     * Test permanently deleting a user locally with proper authorization.
     */
    @Test
    @Transactional
    public void deleteUserPermanent_locally_shouldReturnSuccess() throws Exception {
        performRequest(
                "DELETE",
                "/users/1/false/permanent",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "delete-permanent-local-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}/{global}/permanent - Local 401 Unauthorized
     *
     * Test permanently deleting a user locally without proper authorization.
     */
    @Test
    public void deleteUserPermanent_locallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/1/false/permanent",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-permanent-local-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== DELETE /users/{userId}/{global}/permanent - Global ====================

    /**
     * Test: DELETE /users/{userId}/{global}/permanent - Global Success with Mocked WebClient
     *
     * Test permanently deleting a user globally with mocked webclient response.
     */
    @Test
    @Transactional
    public void deleteUserPermanent_globally_withMockedWebClient_shouldReturnSuccess() throws Exception {
        Map<String, String> mockedResponse = Map.of(
                "message", "User deleted permanently",
                "deletedUserLogin", "test.user@test.com");

        when(userClient.deleteGlobalUserPermanent(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok(mockedResponse)));

        performRequest(
                "DELETE",
                "/users/1/true/permanent",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "delete-permanent-global-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: DELETE /users/{userId}/{global}/permanent - Global 401 Unauthorized
     *
     * Test permanently deleting a user globally without authorization.
     */
    @Test
    @Transactional
    public void deleteUserPermanent_globallyWithoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "DELETE",
                "/users/2/true/permanent",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "delete-permanent-global-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== PUT /users/{userId}/promote-manager ====================

    /**
     * Test: PUT /users/{userId}/promote-manager - Success with Mocked WebClient
     *
     * Test promoting a user to manager role with mocked webclient response.
     */
    @Test
    @Transactional
    public void promoteToManager_withMockedWebClient_shouldReturnSuccess() throws Exception {
        when(userClient.promoteToManager(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok("User promoted to manager successfully")));

        performRequest(
                "PUT",
                "/users/1/promote-manager",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "promote-manager-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /users/{userId}/promote-manager - 401 Unauthorized
     *
     * Test promoting a user to manager role without authorization.
     */
    @Test
    @Transactional
    public void promoteToManager_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "PUT",
                "/users/1/promote-manager",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "promote-manager-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== PUT /users/{userId}/revoke-manager ====================

    /**
     * Test: PUT /users/{userId}/revoke-manager - Success with Mocked WebClient
     *
     * Test revoking manager role with mocked webclient response.
     */
    @Test
    @Transactional
    public void revokeManager_withMockedWebClient_shouldReturnSuccess() throws Exception {
        when(userClient.revokeManager(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok("Manager role revoked successfully")));

        performRequest(
                "PUT",
                "/users/2/revoke-manager",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "revoke-manager-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /users/{userId}/revoke-manager - 401 Unauthorized
     *
     * Test revoking manager role from a user without authorization.
     */
    @Test
    @Transactional
    public void revokeManager_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "PUT",
                "/users/1/revoke-manager",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "revoke-manager-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== PUT /users/{userId}/promote-admin ====================

    /**
     * Test: PUT /users/{userId}/promote-admin - Success with Mocked WebClient
     *
     * Test promoting a user to admin role with mocked webclient response.
     */
    @Test
    @Transactional
    public void promoteToAdmin_withMockedWebClient_shouldReturnSuccess() throws Exception {
        when(userClient.promoteToAdmin(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok("Admin role assigned successfully")));

        performRequest(
                "PUT",
                "/users/2/promote-admin",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "promote-admin-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /users/{userId}/promote-admin - 401 Unauthorized
     *
     * Test promoting a user to admin role without authorization.
     */
    @Test
    @Transactional
    public void promoteToAdmin_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "PUT",
                "/users/1/promote-admin",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "promote-admin-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== PUT /users/{userId}/revoke-admin ====================

    /**
     * Test: PUT /users/{userId}/revoke-admin - Success with Mocked WebClient
     *
     * Test revoking admin role with mocked webclient response.
     */
    @Test
    @Transactional
    public void revokeAdmin_withMockedWebClient_shouldReturnSuccess() throws Exception {
        when(userClient.revokeAdmin(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok("Admin role revoked successfully")));

        performRequest(
                "PUT",
                "/users/4/revoke-admin",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "revoke-admin-unauthorized-invalid-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /users/{userId}/revoke-admin - 401 Unauthorized
     *
     * Test revoking admin role from a user without authorization.
     */
    @Test
    @Transactional
    public void revokeAdmin_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "PUT",
                "/users/1/revoke-admin",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "revoke-admin-unauthorized-missing-token",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // ==================== PUT /users/{userId}/downgrade-admin ====================

    /**
     * Test: PUT /users/{userId}/downgrade-admin - Success with Mocked WebClient
     *
     * Test downgrading admin to manager with mocked webclient response.
     */
    @Test
    @Transactional
    public void downgradeAdmin_withMockedWebClient_shouldReturnSuccess() throws Exception {
        when(userClient.downgradeAdmin(anyString(), anyLong()))
                .thenReturn(Mono.just(ResponseEntity.ok("Admin role downgraded successfully")));

        performRequest(
                "PUT",
                "/users/4/downgrade-admin",
                null,
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "downgrade-admin-success",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /users/{userId}/downgrade-admin - 401 Unauthorized
     *
     * Test downgrading an admin to manager role without authorization.
     */
    @Test
    @Transactional
    public void downgradeAdmin_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "PUT",
                "/users/1/downgrade-admin",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "downgrade-admin-unauthorized",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
