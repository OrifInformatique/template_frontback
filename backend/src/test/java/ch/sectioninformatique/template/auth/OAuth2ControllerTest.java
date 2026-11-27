package ch.sectioninformatique.template.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import reactor.core.publisher.Mono;

/**
 * Tests the Azure OAuth2 login endpoints (/oauth2), spring-auth being mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @MockitoBean
    private AuthClient authClient;

    @Value("${AFTER_OAUTH2_LOGIN_FRONTEND_CALLBACK}")
    private String frontendCallback;

    @Value("${SECURITY_JWT_TOKEN_REFRESH_TOKEN_LIFETIME}")
    private Duration refreshTokenLifetime;

    /**
     * GET /oauth2/login/azure redirects to spring-auth's Azure login.
     */
    @Test
    public void loginWithAzure_shouldRedirectToSpringAuth() throws Exception {
        java.net.URI springAuthLogin = java.net.URI.create("http://spring-auth/oauth2/login/azure?redirectUrl=x");
        when(authClient.buildAzureLoginUri()).thenReturn(springAuthLogin);

        mockMvc.perform(get("/oauth2/login/azure"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, springAuthLogin.toString()));
    }

    /**
     * Full round trip: the callback exchanges the auth code and redirects to the
     * frontend; the frontend then gets the user and the refresh token cookie
     * once (POST /oauth2/token); a second request gets 401.
     */
    @Test
    public void callbackThenToken_shouldReturnLoggedUserOnce() throws Exception {
        UserDto user = userService.findByLogin("test.user@test.com");
        UserDto globalUser = UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .login(user.getLogin())
                .mainRole(user.getMainRole())
                .token("access-token")
                .build();
        when(authClient.getTokenWithAuthCode(any(AuthCodeDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, "refresh_token=refresh-123; Path=/auth/refresh; HttpOnly")
                        .body(globalUser)));

        // spring-auth redirects the browser to the callback
        MockHttpSession session = (MockHttpSession) mockMvc.perform(get("/oauth2/callback")
                        .param("authCode", "code-xyz")
                        .param("userId", String.valueOf(user.getId())))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, frontendCallback))
                .andReturn().getRequest().getSession(false);

        // The frontend takes the result, with the session cookie
        mockMvc.perform(post("/oauth2/token").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("test.user@test.com"))
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.allOf(
                        Matchers.startsWith("refresh_token=refresh-123"),
                        // Lifetime of the refresh token, not of the access token
                        Matchers.containsString("Max-Age=" + refreshTokenLifetime.toSeconds()))));

        // Single use: the tokens are no longer in the session
        mockMvc.perform(post("/oauth2/token").session(session))
                .andExpect(status().isUnauthorized());
    }
}
