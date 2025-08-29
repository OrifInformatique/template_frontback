package ch.sectioninformatique.template.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.auth.CredentialsDto;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing items in the system.
 * This controller provides endpoints for CRUD operations on items,
 * with appropriate security checks and authorization requirements.
 * All responses are automatically converted to JSON format.
 */
@RequestMapping("/test")
@RequiredArgsConstructor
@RestController
public class testController {
    
    

    @Autowired
    private Environment environment;
    
    private UserAuthenticationProvider userAuthenticationProvider;
    private final UserService userService;

    

    /**
     * Returns system information and environment variables.
     * This endpoint is used to verify that the application is running
     * and to display configuration information.
     *
     * @return A formatted string containing system information
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String getHello() {
        return "<strong>Hello World !</strong><br>" +
                "<strong>JAVA_HOME : </strong>" + environment.getProperty("JAVA_HOME") + "<br>" +
                "<strong>Spring active profile : </strong>" + environment.getProperty("spring.profiles.active") + "<br>"
                +
                "<strong>Database used : </strong>" + environment.getProperty("spring.datasource.url");
    }

    /**
     * Retrieves the currently authenticated user's information.
     * This endpoint:
     * - Requires user authentication
     * - Returns the user's profile information
     * - Is accessible to all authenticated users
     *
     * @return ResponseEntity containing the current user's DTO
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        
        UserDto currentUser = (UserDto) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

}
