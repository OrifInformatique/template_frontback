package ch.sectioninformatique.template.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class TestControllerIntegrationTest {

    /**
     * Performs an HTTP request and saves the response and token to files.
     * 
     * @param requestTypeString The HTTP request method (GET, POST, PUT, DELETE)
     * @param endpoint          The endpoint to send the request to
     * @param token             The authentication token to include in the request
     * @param contentType       The content type of the request
     * @param expectedStatus    The expected HTTP status code of the response
     * @param responseFileName  The name of the file to save the response body
     * @param tokenFileName     The name of the file to save the token
     * @param extraExpectations Additional expectations to apply to the response
     * @throws Exception
     */
    private void performRequest(
            String requestTypeString,
            String endpoint,
            String token,
            MediaType contentType,
            int expectedStatus,
            String docsFileName,
            ResultMatcher... extraExpectations) throws Exception {

        // Perform request
        ResultActions request = TestControllerHelper.performTest(
                mockMvc,
                requestTypeString,
                endpoint,
                token,
                contentType,
                expectedStatus);

        if (extraExpectations != null) {
            for (ResultMatcher matcher : extraExpectations) {
                request.andExpect(matcher);
            }
        }

        request.andDo(document("tests/" + docsFileName, preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));
    }

    /** Service for handling user-related operations */
    @Autowired
    private UserService userService;

    /** Provider for user authentication and token generation */
    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    /** MockMvc for simulating HTTP requests in tests */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Tests the /tests/ endpoint with real user data.
     * This test:
     * - Retrieves a test user from the database
     * - Generates an authentication token for the user
     * - Performs a GET request to the /tests/ endpoint with the token
     * - Verifies the response status is OK
     * - Saves the response and token to files for later use
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    @Transactional
    public void getHello_withRealData_shouldReturnSuccess() throws Exception {
        UserDto userDto = userService.findByLogin("test.user@test.com");

        String token = userAuthenticationProvider.createToken(userDto);
        performRequest(
                "GET",
                "/tests/",
                token,
                MediaType.ALL,
                200,
                "get-hello");
    }

    /**
     * Tests the /tests/me endpoint with real user data.
     * This test:
     * - Retrieves a test user from the database
     * - Generates an authentication token for the user
     * - Performs a GET request to the /tests/me endpoint with the token
     * - Verifies the response contains the correct user information
     * - Saves the response and token to files for later use
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    @Transactional
    public void me_withRealData_shouldReturnSuccess() throws Exception {
        UserDto userDto = userService.findByLogin("test.user@test.com");

        String token = userAuthenticationProvider.createToken(userDto);
        performRequest(
                "GET",
                "/tests/me",
                token,
                MediaType.APPLICATION_JSON,
                200,
                "me",
                jsonPath("$.firstName").value("Test"),
                jsonPath("$.lastName").value("User"),
                jsonPath("$.login").value("test.user@test.com"));
    }

    /**
     * Tests the /tests/me endpoint with real user data.
     * This test:
     * - Retrieves a test user from the database
     * - Generates an authentication token for the user
     * - Performs a GET request to the /tests/me endpoint with the token
     * - Verifies the response contains the correct user information
     * - Saves the response and token to files for later use
     *
     * @throws Exception if an error occurs during the test
     */
    @Test
    @Transactional
    public void promoteToTestAdmin_withRealData_shouldReturnSuccess() throws Exception {
        UserDto userDto = userService.findByLogin("test.user@test.com");

        UserDto adminDto = userService.findByLogin("test.admin@test.com");

        String token = userAuthenticationProvider.createToken(adminDto);
        performRequest(
                "PUT",
                "/tests/" + userDto.getId() + "/promote-test",
                token,
                MediaType.APPLICATION_JSON,
                200,
                "promote-test",
                jsonPath("$.message").exists());
    }
}
