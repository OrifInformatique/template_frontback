package ch.sectioninformatique.template.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    /** Mock client for external auth service (only used for global delete operations) */
    @MockitoBean
    private AuthClient authClient;

    /**
     * Helper method to generate a valid JWT token for a test user by login.
     * Retrieves the user from the database using UserService and creates a real JWT token
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
     * @param requestTypeString HTTP method (GET, DELETE, etc.)
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
            String token,
            MediaType contentType,
            int expectedStatus,
            String docsFileName,
            Consumer<ResultActions> script) throws Exception {

        var request = get(endpoint);
        if ("GET".equals(requestTypeString)) {
            request = get(endpoint);
        } else if ("DELETE".equals(requestTypeString)) {
            request = delete(endpoint);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + requestTypeString);
        }

        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        request.contentType(contentType);

        var result = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus));

        if (script != null) {
            script.accept(result);
        }

        result.andDo(document("users/" + docsFileName, preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));
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
     * Verify local user deletion removes user from database without calling external auth service.
     */
    @Test
    @Transactional
    public void deleteLocalUser_withoutGlobalFlag_shouldReturn200AndDeleteFromDatabase() throws Exception {
        String adminToken = getValidTokenForUser("test.admin@test.com");
        // Get a different test user to delete (not the admin performing the deletion)
        UserDto userToDelete = userService.findByLogin("test.user@test.com");
        assertNotNull(userToDelete, "Test user should exist");

        // For reactive Mono responses, we need to handle async results
        var mvcResult = mockMvc.perform(delete("/users/{userId}/{global}", userToDelete.getId(), false)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(document("users/delete-local",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())))
                .andReturn();

        // Get the async result (the Mono's resolved ResponseEntity)
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, String>> asyncResult = 
                (ResponseEntity<Map<String, String>>) mvcResult.getAsyncResult();
        
        // Verify the message in the async result body
        assertNotNull(asyncResult.getBody());
        assertEquals("Local User deleted successfully", asyncResult.getBody().get("message"));
    }

    /**
     * Test: DELETE /users/{id}/true
     *
     * Verify global user deletion calls external auth service and removes from database.
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

        // For reactive Mono responses, we need to handle async results
        var mvcResult = mockMvc.perform(delete("/users/{userId}/{global}", admin2User.getId(), true)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(document("users/delete-global",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())))
                .andReturn();

        // Get the async result (the Mono's resolved ResponseEntity)
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, String>> asyncResult = 
                (ResponseEntity<Map<String, String>>) mvcResult.getAsyncResult();
        
        // Verify the message in the async result body
        assertNotNull(asyncResult.getBody());
        assertEquals("User deleted from auth service", asyncResult.getBody().get("message"));
        
        // Verify the auth client was called
        verify(authClient).deleteGlobalUser(eq("Bearer " + adminToken), eq(admin2User.getId()));
    }

    /**
     * Test: DELETE /users/{id}/true
     *
     * Verify error handling when global user deletion fails in external auth service.
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

        // For reactive Mono error responses, the exception is in the async result
        var mvcResult = mockMvc.perform(delete("/users/{userId}/{global}", managerUser.getId(), true)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Initial status before async processing
                .andExpect(request().asyncStarted())
                .andDo(document("users/delete-global-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())))
                .andReturn();

        // The async result should be the UserDeletionException
        Object asyncResult = mvcResult.getAsyncResult();
        assertTrue(asyncResult instanceof UserDeletionException, 
                "Expected UserDeletionException but got: " + asyncResult.getClass());
        
        UserDeletionException exception = (UserDeletionException) asyncResult;
        assertTrue(exception.getMessage().contains("Failed to delete user"));
    }
}
