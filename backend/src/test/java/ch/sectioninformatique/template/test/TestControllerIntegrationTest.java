package ch.sectioninformatique.template.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Tag("integration")
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
public class TestControllerIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    public void me_withRealData_shouldReturnSuccess() throws Exception {
        UserDto userDto = userService.findByLogin("test.user@test.com");

        String token = userAuthenticationProvider.createToken(userDto);

        MvcResult result = mockMvc.perform(get("/tests/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.login").value("test.user@test.com"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        // Save response to file for later tests
        Path path = Paths.get("target/test-data/users-me-response.json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, responseBody);

        // Save token to file for later tests
        Path pathToken = Paths.get("target/test-data/users-me-token.txt");
        Files.createDirectories(pathToken.getParent());
        Files.writeString(pathToken, token);
    }

}
