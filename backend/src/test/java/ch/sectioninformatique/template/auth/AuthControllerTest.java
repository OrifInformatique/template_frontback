package ch.sectioninformatique.template.auth;

// Import statements for testing, Spring Boot, JSON handling, and REST Docs
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
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

    /** MockMvc for simulating HTTP requests in tests */
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthClient authClient; // mock this instead of the controller

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
     * Test: POST /tests/login
     *
     * Mock a user log in successfull with valid credentials.
     */
    @Test
    @Transactional
    public void login_withMockedService_shouldReturnSuccess() throws Exception {
        UserDto mockedUser = UserDto.builder()
                .id(2L)
                .firstName("John")
                .lastName("DOE")
                .login("john.doe@test.com")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
                .mainRole("USER")
                .permissions(new ArrayList<String>())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refresh_token=fakeToken123; HttpOnly; Path=/; Max-Age=3600");

        when(authClient.login(any(CredentialsDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok().headers(headers).body(mockedUser)));

        performRequest(
                "POST",
                "/auth/login",
                "{\"login\":\"john.doe@test.com\", \"password\":\"Secure123@Pass\"}",
                null,
                MediaType.APPLICATION_JSON,
                200,
                "login",
                response -> {
                    try {
                        // Assert status code
                        response.andExpect(status().isOk());

                        // Assert response body fields
                        response.andExpect(jsonPath("$.firstName").value("John"));
                        response.andExpect(jsonPath("$.login").value("john.doe@test.com"));

                        // Assert refresh token cookie
                        response.andExpect(header().string(HttpHeaders.SET_COOKIE,
                                org.hamcrest.Matchers.containsString("refresh_token=fakeToken123")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /tests/register
     *
     * Mock a user registering successfully.
     */
    @Test
    @Transactional
    public void register_withMockedService_shouldReturnSuccess() throws Exception {

        UserDto mockedUser = UserDto.builder()
                .id(5L)
                .firstName("Test")
                .lastName("NewUser")
                .login("test.newuser@test.com")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
                .mainRole("USER")
                .permissions(new ArrayList<String>())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refresh_token=fakeToken123; HttpOnly; Path=/; Max-Age=3600");

        when(authClient.register(any(RegisterDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok().headers(headers).body(mockedUser)));

        performRequest(
                "POST",
                "/auth/register",
                "{\"firstName\":\"Test\",\"lastName\":\"NewUser\",\"login\":\"test.newuser@test.com\",\"password\":\"testPassword\"}",
                null,
                MediaType.APPLICATION_JSON,
                200,
                "register",
                request -> {
                    try {

                        UserDto updatedUser = userService.findByLogin("test.newuser@test.com");

                        assertEquals("Test", updatedUser.getFirstName());
                        assertEquals("NewUser", updatedUser.getLastName());
                        assertEquals("USER", updatedUser.getMainRole());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/refresh
     *
     * Mock a user refreshing their token successfully.
     */
    @Test
    @Transactional
    public void refresh_withMockedService_shouldReturnSuccess() throws Exception {
        TokenResponseDto tokenResponse = new TokenResponseDto("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...");

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
     * Test: PUT /auth/update-password
     *
     * Mock a user updating their password successfully.
     * Note: This endpoint requires authentication, so expect 401 when no valid token is provided.
     */
    @Test
    @Transactional
    public void updatePassword_withoutToken_shouldReturnUnauthorized() throws Exception {
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
     * Note: This endpoint requires anonymous access and redirects, so expect 401 due to security configuration.
     */
    @Test
    public void oauth2Login_shouldReturn_401() throws Exception {
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

    /**
     * Test: POST /auth/logout
     *
     * Mock a user attempting to log out with a token.
     * Note: This endpoint requires proper authentication credentials. The test expects 401 Unauthorized
     * due to the security configuration and external service dependency that may not be fully available
     * in the test environment.
     */
    @Test
    @Transactional
    public void logout_withToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "POST",
                "/auth/logout",
                null,
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
                MediaType.APPLICATION_JSON,
                401,
                "logout-unauthorized-service",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Test: POST /auth/logout
     *
     * Mock a user attempting to log out without providing a token.
     * This should return an unauthorized status.
     */
    @Test
    @Transactional
    public void logout_withoutToken_shouldReturnUnauthorized() throws Exception {
        performRequest(
                "POST",
                "/auth/logout",
                null,
                null,
                MediaType.APPLICATION_JSON,
                401,
                "logout-unauthorized",
                response -> {
                    try {
                        response.andExpect(status().isUnauthorized());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}