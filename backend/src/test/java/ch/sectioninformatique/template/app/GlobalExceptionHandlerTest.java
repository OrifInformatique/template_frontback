package ch.sectioninformatique.template.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests the error responses built by the GlobalExceptionHandler for Spring's
 * own exceptions.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * A missing required cookie is answered with the standard error body
     * (400, message), not Spring's default error with its stack trace.
     */
    @Test
    public void refresh_withoutRefreshTokenCookie_shouldReturnStandardError400() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("refresh_token")))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
