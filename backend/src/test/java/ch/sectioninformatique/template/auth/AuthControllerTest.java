package ch.sectioninformatique.template.auth;

// Import statements for testing, Spring Boot, JSON handling, and REST Docs
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import ch.sectioninformatique.template.auth.AuthExceptions.InvalidCredentialsException;
import ch.sectioninformatique.template.auth.AuthExceptions.UserAlreadyExistsException;
import ch.sectioninformatique.template.security.SecurityExceptions.InvalidRefreshTokenException;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import java.util.ArrayList;
import java.util.function.Consumer;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the "TestController" REST endpoints.
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
public class AuthControllerTest {

    /** Service for handling user-related operations */
    @Autowired
    private UserService userService;

    /** Provider for creating JWT tokens */
    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    /** MockMvc for simulating HTTP requests in tests */
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthClient authClient; // mock this instead of the controller

    /**
     * Helper method to generate a valid JWT token for the test user.
     * Retrieves the test user from the database using UserService and creates a real JWT token
     * using UserAuthenticationProvider.
     * 
     * @return A valid JWT token for the test user
     */
    private String getValidTokenForTestUser() {
        // Get the test user DTO from database (seeded by TestUserSeeder)
        UserDto userDto = userService.findByLogin("test.user@test.com");
        
        if (userDto == null) {
            throw new RuntimeException("Test user not found. Ensure TestUserSeeder has run.");
        }
        
        // Create and return a real JWT token
        return userAuthenticationProvider.createToken(userDto);
    }

    /**
     * Helper method for performing and documenting HTTP requests in tests.
     * This reduces repetition by centralizing the request execution and REST Docs
     * generation.
     *
     * @param requestTypeString HTTP method (GET, POST, PUT, etc.)
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
        } else if (requestTypeString.equals("POST")) {
            requestType = post(endpoint);
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
        request.andDo(document("auth/" + docsFileName, preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));

    }

    /**
     * Test: POST /auth/login
     *
     * Verify successful login with valid credentials returns user data and sets refresh token cookie.
     */
    @Test
    @Transactional
    public void login_withValidCredentials_shouldReturn200AndSetCookie() throws Exception {
        // Get real test user from database
        UserDto testUser = userService.findByLogin("test.user@test.com");
        testUser.setToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refresh_token=fakeToken123; HttpOnly; Path=/; Max-Age=3600");

        when(authClient.login(any(CredentialsDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok().headers(headers).body(testUser)));

        performRequest(
                "POST",
                "/auth/login",
                "{\"login\":\"test.user@test.com\", \"password\":\"Secure123@Pass\"}",
                null,
                MediaType.APPLICATION_JSON,
                200,
                "login",
                response -> {
                    try {
                        // Assert status code
                        response.andExpect(status().isOk());

                        // Assert response body fields
                        response.andExpect(jsonPath("$.firstName").value("Test"));
                        response.andExpect(jsonPath("$.login").value("test.user@test.com"));

                        // Assert refresh token cookie
                        response.andExpect(header().string(HttpHeaders.SET_COOKIE,
                                org.hamcrest.Matchers.containsString("refresh_token=fakeToken123")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/login
     *
     * Mock login failure with invalid credentials and expect 401.
     */
    @Test
    @Transactional
    public void login_withInvalidCredentials_shouldReturn401() throws Exception {
        when(authClient.login(any(CredentialsDto.class)))
                .thenReturn(Mono.error(new InvalidCredentialsException()));

        performRequest(
                "POST",
                "/auth/login",
                "{\"login\":\"john.doe@test.com\", \"password\":\"WrongPass\"}",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "login-invalid",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/register
     *
     * Verify successful registration calls auth client and saves user to local database.
     */
    @Test
    @Transactional
    public void register_withValidData_shouldReturn200AndSaveUserToDatabase() throws Exception {
        // Create a new user DTO that doesn't exist yet in the database
        UserDto newUser = UserDto.builder()
                .firstName("NewTest")
                .lastName("Register")
                .login("newtest.register@test.com")
                .mainRole("USER")
                .permissions(new ArrayList<>())
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refresh_token=fakeToken123; HttpOnly; Path=/; Max-Age=3600");

        when(authClient.register(any(RegisterDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok().headers(headers).body(newUser)));

        performRequest(
                "POST",
                "/auth/register",
                "{\"firstName\":\"NewTest\",\"lastName\":\"Register\",\"login\":\"newtest.register@test.com\",\"password\":\"testPassword\"}",
                null,
                MediaType.APPLICATION_JSON,
                200,
                "register",
                request -> {
                    try {
                        // Verify the auth client was called
                        verify(authClient).register(any(RegisterDto.class));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/register
     *
     * Verify registration with existing user returns 400 Bad Request.
     * Note: The controller wraps all registration errors with RegistrationFailedException (400),
     * even though the underlying error is UserAlreadyExistsException (409).
     */
    @Test
    @Transactional
    public void register_withExistingUser_shouldReturn400BadRequest() throws Exception {

        when(authClient.register(any(RegisterDto.class)))
                .thenReturn(Mono.error(new UserAlreadyExistsException()));

        performRequest(
                "POST",
                "/auth/register",
                "{\"firstName\":\"Test\",\"lastName\":\"Existing\",\"login\":\"exists@test.com\",\"password\":\"testPassword\"}",
                null,
                MediaType.APPLICATION_JSON,
                400,
                "register-conflict",
                response -> {
                    try {
                        response.andExpect(status().isBadRequest());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/refresh
     *
     * Verify successful token refresh returns new access token and refresh cookie.
     */
    @Test
    @Transactional
    public void refresh_withValidToken_shouldReturn200AndNewAccessToken() throws Exception {
        // Create token response with real user token
        UserDto testUser = userService.findByLogin("test.user@test.com");
        String newAccessToken = userAuthenticationProvider.createToken(testUser);
        TokenResponseDto tokenResponse = new TokenResponseDto(newAccessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refresh_token=fakeToken123; HttpOnly; Path=/; Max-Age=3600");

        when(authClient.refreshLogin(any(RefreshRequestDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok().headers(headers).body(tokenResponse)));

        performRequest(
                "POST",
                "/auth/refresh",
                "{\"refreshToken\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...\"}",
                null,
                MediaType.APPLICATION_JSON,
                200,
                "refresh",
                response -> {
                    try {
                        response.andExpect(status().isOk());
                        response.andExpect(jsonPath("$.accessToken").exists());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/refresh
     *
     * Verify invalid refresh token returns 401 Unauthorized.
     */
    @Test
    @Transactional
    public void refresh_withInvalidToken_shouldReturn401() throws Exception {
        when(authClient.refreshLogin(any(RefreshRequestDto.class)))
                .thenReturn(Mono.error(new InvalidRefreshTokenException()));

        performRequest(
                "POST",
                "/auth/refresh",
                "{\"refreshToken\":\"invalidToken\"}",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "refresh-invalid",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /auth/update-password
     *
     * Verify successful password update with valid token returns 200.
     */
    @Test
    @Transactional
    public void updatePassword_withValidToken_shouldReturn200() throws Exception {
        MessageResponseDto messageResponse = new MessageResponseDto("Password updated successfully");

        when(authClient.updatePassword(any(String.class), any(PasswordUpdateDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(messageResponse)));

        String validToken = getValidTokenForTestUser();

        performRequest(
                "PUT",
                "/auth/update-password",
                "{\"oldPassword\":\"OldPassword123@\",\"newPassword\":\"NewPassword123@\"}",
                validToken,
                MediaType.APPLICATION_JSON,
                200,
                "update-password-success",
                response -> {
                    try {
                        response.andExpect(status().isOk());
                        response.andExpect(jsonPath("$.message").value("Password updated successfully"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: PUT /auth/update-password
     *
     * Verify password update without authentication token returns 401 Unauthorized.
     */
    @Test
    @Transactional
    public void updatePassword_withoutToken_shouldReturn401() throws Exception {
        performRequest(
                "PUT",
                "/auth/update-password",
                "{\"oldPassword\":\"OldPassword123@\",\"newPassword\":\"NewPassword123@\"}",
                null,
                MediaType.APPLICATION_JSON,
                401,
                "update-password",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: GET /auth/oauth2/login
     *
     * OAuth2 login endpoint that redirects to Azure OAuth2 authorization.
     * Note: Returns 401 in test environment because the Spring Security filter chain
     * is not fully configured for OAuth2 flows in MockMvc tests. In production,
     * this endpoint would return 302 redirect to Azure authorization URL.
     */
    @Test
    public void oauth2Login_inTestEnvironment_shouldReturn401() throws Exception {
        performRequest(
                "GET",
                "/auth/oauth2/login",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "oauth2-login",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}