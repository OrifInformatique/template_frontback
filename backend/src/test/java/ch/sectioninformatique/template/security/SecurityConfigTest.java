package ch.sectioninformatique.template.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;

/**
 * Tests the security configuration of the filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    /**
     * The API is authenticated by the JWT of each request only: the HTTP session
     * (kept for the Azure OAuth2 flow) must not remember the authentication, or
     * its JSESSIONID cookie alone would authenticate later requests.
     */
    @Test
    public void request_withSessionOfAuthenticatedRequestButNoToken_shouldReturn401() throws Exception {
        UserDto user = userService.findByLogin("test.user@test.com");
        String token = userAuthenticationProvider.createToken(SpringAuthPermissions.grant(user));

        // Authenticated request with a token: a session is created (policy ALWAYS)
        MockHttpSession session = (MockHttpSession) mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);

        // Same session, no token: must not be authenticated
        mockMvc.perform(get("/users/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
