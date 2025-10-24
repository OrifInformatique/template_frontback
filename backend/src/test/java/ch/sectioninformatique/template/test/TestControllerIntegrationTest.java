package ch.sectioninformatique.template.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.function.Consumer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Tag("integration")
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
public class TestControllerIntegrationTest {

    /** Helper method to perform HTTP requests and save responses and tokens to files */
    private void performRequest(
            String requestTypeString,
            String endpoint,
            String token,
            MediaType contentType,
            int expectedStatus,
            String responseFileName,
            String tokenFileName,
            ResultMatcher... extraExpectations
            ) throws Exception {

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

        // Perform request
        var request = mockMvc.perform(requestType
                .contentType(contentType)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));

        for (ResultMatcher matcher : extraExpectations) {
            request.andExpect(matcher);
        }
        
        MvcResult result = request.andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // Save response to file
        Path responsePath = Paths.get("target/test-data/" + responseFileName);
        Files.createDirectories(responsePath.getParent());
        Files.writeString(responsePath, responseBody);

        // Save token to file
        Path tokenPath = Paths.get("target/test-data/" + tokenFileName);
        Files.createDirectories(tokenPath.getParent());
        Files.writeString(tokenPath, token);
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
                "tests-get-hello-response.txt",
                "tests-get-hello-token.txt"
        );
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
                "tests-me-response.json",
                "tests-me-token.txt",
                jsonPath("$.firstName").value("Test"),
                jsonPath("$.lastName").value("User"),
                jsonPath("$.login").value("test.user@test.com")
        );
    }
}
