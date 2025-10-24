package ch.sectioninformatique.template.test;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.restdocs.RestDocumentationExtension;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserMapper;
import ch.sectioninformatique.template.user.UserRepository;
import ch.sectioninformatique.template.user.UserService;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(controllers = TestController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class TestControllerDocTest {

        /** Mocked Authentication object */
        @MockBean
        private Authentication authentication;

        /** Mocked SecurityContext object */
        @MockBean
        private SecurityContext securityContext;

        /** MockMvc for performing HTTP requests in tests */
        @Autowired
        private MockMvc mockMvc;

        /** Mocked UserService */
        @MockBean
        private UserService userService;

        /** Mocked UserAuthenticationProvider */
        @MockBean
        private UserAuthenticationProvider userAuthenticationProvider;

        /** Mocked UserMapper */
        @MockBean
        private UserMapper userMapper;

        /** Mocked RoleRepository */
        @MockBean
        private RoleRepository roleRepository;

        /** Mocked UserRepository */
        @MockBean
        private UserRepository userRepository;

        /** Mocked AuthClient */
        @MockBean
        private AuthClient authClient;

        /**
         * Performs a mocked HTTP GET request to the specified endpoint,
         * using token and response data from files, and generates REST Docs.
         * 
         * @param endpoint          The endpoint to send the request to
         * @param responseFileName  The name of the file containing the mocked response JSON
         * @param tokenFileName     The name of the file containing the authentication token
         * @param documentName      The name for the generated REST Docs snippet
         * @param mockUserConsumer  A consumer to customize user mocking behavior
         * @throws Exception
         */
        private void performMockedRequest(
                        String endpoint,
                        String responseFileName,
                        String tokenFileName,
                        String documentName,
                        Consumer<UserDto> mockUserConsumer) throws Exception {

                // Read token from file
                Path tokenPath = Paths.get("target/test-data/" + tokenFileName);
                if (!Files.exists(tokenPath)) {
                        throw new IllegalStateException("Missing token file: " + tokenPath);
                }
                String token = Files.readString(tokenPath);

                UserDto userDto = null;

                // If a response file is provided, read and parse it
                if (responseFileName != null) {
                        Path responsePath = Paths.get("target/test-data/" + responseFileName);
                        if (!Files.exists(responsePath)) {
                                throw new IllegalStateException("Missing response file: " + responsePath);
                        }

                        String jsonResponse = Files.readString(responsePath);
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

                        userDto = UserDto.builder()
                                        .id(jsonNode.get("id").asLong())
                                        .firstName(jsonNode.get("firstName").asText())
                                        .lastName(jsonNode.get("lastName").asText())
                                        .login(jsonNode.get("login").asText())
                                        .token(jsonNode.has("token") ? jsonNode.get("token").asText(token) : token)
                                        .mainRole(jsonNode.has("mainRole") ? jsonNode.get("mainRole").asText("USER")
                                                        : "USER")
                                        .appSpecificRoles(new ObjectMapper().convertValue(
                                                        jsonNode.get("appSpecificRoles"),
                                                        new TypeReference<List<String>>() {
                                                        }))
                                        .permissions(new ObjectMapper().convertValue(
                                                        jsonNode.get("permissions"), new TypeReference<List<String>>() {
                                                        }))
                                        .build();
                }

                // Allow test method to customize user mocking
                if (mockUserConsumer != null && userDto != null) {
                        mockUserConsumer.accept(userDto);
                }

                // Default authentication mocking
                when(authentication.getPrincipal()).thenReturn(userDto);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                if (userDto != null) {
                        when(userService.me(userDto)).thenReturn(userDto);
                }

                // Perform and document
                mockMvc.perform(get(endpoint)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andDo(document(documentName,
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint())));
        }

        /**
         * Test generating REST Docs for GET /tests/ endpoint.
         * 
         * @throws Exception
         */
        @Test
        void getHello_withMockedService_generatesDoc() throws Exception {
                performMockedRequest(
                                "/tests/",
                                null, // No response body for this test
                                "tests-get-hello-token.txt", // Token file
                                "tests/get-hello", // REST Docs name
                                userDto -> {
                                        // No special mocking needed for /tests/
                                });
        }

        /**
         * Test generating REST Docs for GET /tests/me endpoint.
         * 
         * @throws Exception
         */
        @Test
        void me_withMockedService_generatesDoc() throws Exception {
                performMockedRequest(
                                "/tests/me",
                                "tests-me-response.json", // JSON file from integration test
                                "tests-me-token.txt", // Token file
                                "tests/me", // REST Docs name
                                userDto -> {
                                        // Customize user mocking if needed
                                        when(userService.me(userDto)).thenReturn(userDto);
                                });
        }

}
