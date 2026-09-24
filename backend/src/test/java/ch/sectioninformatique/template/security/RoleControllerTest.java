package ch.sectioninformatique.template.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;

/**
 * Integration tests for the {@code GET /roles} endpoint, verifying that the
 * {@code ?scope=} query parameter selects local roles, main roles, or both.
 */
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    /** A valid access token for a seeded user (any authenticated user may list roles). */
    private String token() {
        UserDto user = userService.findByLogin("test.user@test.com");
        return userAuthenticationProvider.createToken(user);
    }

    @Test
    public void getRoles_withoutScope_returnsOnlyLocalRoles() throws Exception {
        mockMvc.perform(get("/roles").header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", Matchers.everyItem(Matchers.is("LOCAL"))))
                .andExpect(jsonPath("$[*].name", Matchers.hasItem("LOCAL_APP_ROLE")))
                .andExpect(jsonPath("$[*].name", Matchers.not(Matchers.hasItem("ADMIN"))));
    }

    @Test
    public void getRoles_withScopeMain_returnsOnlyMainRoles() throws Exception {
        mockMvc.perform(get("/roles").param("scope", "main")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].type", Matchers.everyItem(Matchers.is("MAIN"))))
                .andExpect(jsonPath("$[*].name",
                        Matchers.containsInAnyOrder("USER", "MANAGER", "ADMIN")))
                .andExpect(jsonPath("$[*].id", Matchers.everyItem(Matchers.nullValue())));
    }

    @Test
    public void getRoles_withScopeAll_returnsMainAndLocalRoles() throws Exception {
        mockMvc.perform(get("/roles").param("scope", "ALL")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", Matchers.hasItem("ADMIN")))
                .andExpect(jsonPath("$[*].name", Matchers.hasItem("LOCAL_APP_ROLE")))
                .andExpect(jsonPath("$[*].type", Matchers.hasItems("MAIN", "LOCAL")));
    }

    @Test
    public void getRoles_withUnknownScope_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/roles").param("scope", "bogus")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
                .andExpect(status().isBadRequest());
    }
}
