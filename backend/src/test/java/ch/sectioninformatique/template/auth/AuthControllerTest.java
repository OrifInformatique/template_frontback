package ch.sectioninformatique.template.auth;

// Import statements for testing, Spring Boot, JSON handling, and REST Docs
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.web.servlet.MockMvc;
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

    @MockBean
    private AuthClient authClient;

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
    public void login_withRealData_shouldReturnSuccess() throws Exception {
        UserDto mockedUser = UserDto.builder()
                .id(2L)
                .firstName("John")
                .lastName("DOE")
                .login("john.doe@test.com")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
                .refreshToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
                .mainRole("USER")
                .permissions(new ArrayList<String>())
                .build();

        when(authClient.login(any(CredentialsDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(mockedUser)));

        performRequest(
                "POST",
                "/auth/login",
                "{\"login\":\"john.doe@test.com\", \"password\":\"Secure123@Pass\"}",
                null,
                MediaType.APPLICATION_JSON,
                200,
                "login",
                null);
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
                .refreshToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
                .mainRole("USER")
                .permissions(new ArrayList<String>())
                .build();

        when(authClient.register(any(RegisterDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(mockedUser)));

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
}
