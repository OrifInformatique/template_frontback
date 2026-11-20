package ch.sectioninformatique.template.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import ch.sectioninformatique.template.app.errors.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Entry point for handling unauthenticated requests.
 * This class implements Spring Security's AuthenticationEntryPoint to provide
 * a custom response when an unauthenticated user attempts to access a protected resource:
 * HTTP 401 Unauthorized with the standard error body ({@code ErrorResponse}).
 */
@Component
@RequiredArgsConstructor
public class UserAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    /**
     * Handles an unauthenticated request to a protected resource.
     *
     * @param request       The request that resulted in an AuthenticationException
     * @param response      The response to write the error to
     * @param authException The exception that caused the invocation
     * @throws IOException if writing the response fails
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                "security.auth.missingOrInvalidToken");
    }
}
