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
         * Generates REST documentation for the /tests/ endpoint using mocked service.
         * This test:
         * - Reads pre-saved token from file
         * - Mocks the SecurityContext to simulate an authenticated user
         * - Performs a GET request to the /tests/ endpoint
         * - Verifies the response status is OK
         * - Generates REST documentation snippets for the endpoint
         * 
         * @throws Exception
         */
        @Test
        void getHello_withMockedService_generatesDoc() throws Exception {

                Path pathToken = Paths.get("target/test-data/tests-get-hello-token.txt");
                if (!Files.exists(pathToken)) {
                        throw new IllegalStateException(
                                        "Missing required token data. Make sure TestControllerIntegrationTest ran first.");
                }
                String getHelloToken = Files.readString(pathToken);

                when(authentication.getPrincipal()).thenReturn(null);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                this.mockMvc.perform(get("/tests/")
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + getHelloToken))
                                .andExpect(status().isOk())
                                .andDo(document("tests/get-hello", preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint())));
        }

        /**
         * Generates REST documentation for the /tests/me endpoint using mocked UserService.
         * This test:
         * - Reads pre-saved user response data and token from files
         * - Mocks the SecurityContext to simulate an authenticated user
         * - Mocks the UserService to return the user data
         * - Performs a GET request to the /tests/me endpoint
         * - Verifies the response status is OK
         * - Generates REST documentation snippets for the endpoint
         * 
         * @throws Exception
         */
        @Test
        void me_withMockedService_generatesDoc() throws Exception {

                Path path = Paths.get("target/test-data/tests-me-response.json");
                if (!Files.exists(path)) {
                        throw new IllegalStateException(
                                        "Missing required me response data. Make sure TestControllerIntegrationTest ran first.");
                }
                String meResponseJson = Files.readString(path);

                Path pathToken = Paths.get("target/test-data/tests-me-token.txt");
                if (!Files.exists(pathToken)) {
                        throw new IllegalStateException(
                                        "Missing required token data. Make sure TestControllerIntegrationTest ran first.");
                }
                String meToken = Files.readString(pathToken);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(meResponseJson);

                UserDto userDto = UserDto.builder()
                                .id(jsonNode.get("id").asLong())
                                .firstName(jsonNode.get("firstName").asText())
                                .lastName(jsonNode.get("lastName").asText())
                                .login(jsonNode.get("login").asText())
                                .token(jsonNode.get("token").asText(meToken))
                                .mainRole(jsonNode.get("mainRole").asText("USER"))
                                .appSpecificRoles(
                                                objectMapper.convertValue(
                                                                jsonNode.get("appSpecificRoles"),
                                                                new TypeReference<List<String>>() {
                                                                }))
                                .permissions(
                                                objectMapper.convertValue(
                                                                jsonNode.get("permissions"),
                                                                new TypeReference<List<String>>() {
                                                                }))
                                .build();

                when(authentication.getPrincipal()).thenReturn(userDto);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(securityContext.getAuthentication()).thenReturn(authentication);

                SecurityContextHolder.setContext(securityContext);

                when(userService.me(userDto)).thenReturn(userDto);

                this.mockMvc.perform(get("/tests/me")
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + meToken))
                                .andExpect(status().isOk())
                                .andDo(document("tests/me", preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint())));
        }

}
