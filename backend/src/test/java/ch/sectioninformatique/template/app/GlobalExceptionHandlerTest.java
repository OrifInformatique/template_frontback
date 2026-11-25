package ch.sectioninformatique.template.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import ch.sectioninformatique.template.security.SpringAuthPermissions;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserService;

/**
 * Tests that every error, whatever component produces it (exception handler,
 * JWT filter, authentication entry point, controller), is answered with the
 * same body: {timestamp, status, error, message} (+ fieldErrors on validation).
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    private String userToken() {
        return "Bearer " + userAuthenticationProvider.createToken(
                SpringAuthPermissions.grant(userService.findByLogin("test.user@test.com")));
    }

    /** Checks the standard error body, with the given status */
    private static void assertErrorBody(ResultActions result, int status, String reason) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").value(reason))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    private static ResultMatcher messageContains(String text) {
        return jsonPath("$.message").value(Matchers.containsString(text));
    }

    /** Missing refresh token cookie: 401, as in spring-auth (no stack trace) */
    @Test
    public void refresh_withoutRefreshTokenCookie_returns401() throws Exception {
        assertErrorBody(mockMvc.perform(post("/auth/refresh?lang=en")), 401, "Unauthorized");
    }

    /** Invalid token, rejected by the JWT filter */
    @Test
    public void request_withInvalidToken_returnsStandardError401() throws Exception {
        assertErrorBody(mockMvc.perform(get("/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt")), 401, "Unauthorized");
    }

    /** No token, rejected by the authentication entry point */
    @Test
    public void request_withoutToken_returnsStandardError401() throws Exception {
        assertErrorBody(mockMvc.perform(get("/users/me")), 401, "Unauthorized");
    }

    /** The "lang" parameter is honored by the security components too */
    @Test
    public void request_withoutToken_withLangParameter_returnsMessageInThatLanguage() throws Exception {
        mockMvc.perform(get("/users/me?lang=en"))
                .andExpect(messageContains("Invalid or missing authentication token"));
        // French, with a single apostrophe (bundles escape it as '')
        mockMvc.perform(get("/users/me?lang=fr"))
                .andExpect(jsonPath("$.message").value("Jeton d'authentification invalide ou manquant"));
    }

    /** Unsupported HTTP method: 405 */
    @Test
    public void unsupportedMethod_returnsStandardError405() throws Exception {
        assertErrorBody(mockMvc.perform(delete("/roles?lang=en")
                .header(HttpHeaders.AUTHORIZATION, userToken())), 405, "Method Not Allowed");
    }

    /** Unknown URL: 404 */
    @Test
    public void unknownUrl_returnsStandardError404() throws Exception {
        assertErrorBody(mockMvc.perform(get("/no-such-endpoint")
                .header(HttpHeaders.AUTHORIZATION, userToken())), 404, "Not Found");
    }

    /** Malformed JSON: 400 with a generic message, not the parser's */
    @Test
    public void malformedJson_returnsGenericMessage() throws Exception {
        String token = "Bearer " + userAuthenticationProvider.createToken(
                SpringAuthPermissions.grant(userService.findByLogin("test.manager@test.com")));
        ResultActions result = mockMvc.perform(post("/items?lang=en")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": "));
        assertErrorBody(result, 400, "Bad Request");
        result.andExpect(jsonPath("$.message").value("Malformed or missing JSON request body"));
    }

    /** No Azure login result in the session: 401 with a body (it used to be empty) */
    @Test
    public void tokens_withoutAzureLoginResult_returnsStandardError401() throws Exception {
        assertErrorBody(mockMvc.perform(get("/auth/tokens?lang=en")), 401, "Unauthorized");
    }
}
