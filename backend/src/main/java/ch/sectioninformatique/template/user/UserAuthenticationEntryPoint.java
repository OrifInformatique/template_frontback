package ch.sectioninformatique.template.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import ch.sectioninformatique.template.app.errors.ErrorDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // send "Unauthorized" if user not connected and try to
                                                                 // access a restricted page
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        String errorMessage = "Authentication failed";
        if (authException != null) {
            errorMessage = authException.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Invalid or missing authentication token";
            }
        }
        
        OBJECT_MAPPER.writeValue(response.getOutputStream(), new ErrorDto(errorMessage));
    }
}
