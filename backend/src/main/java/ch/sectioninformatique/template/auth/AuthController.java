package ch.sectioninformatique.template.auth;

import ch.sectioninformatique.template.jwt.JwtService;
import ch.sectioninformatique.template.user.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;


@RequestMapping("/auth")
@RestController
public class AuthController {
    private final JwtService jwtService;
    
    private final AuthService authService;

    /**
     * Constructor for the AuthController class
     * 
     * @param jwtService The JWT service
     * @param authService The authentication service
     */
    public AuthController(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    /**
     * Register a new user
     * 
     * @param registerUserDto The registration data for the user
     * @return The registered user
     */
    @PreAuthorize("permitAll")
    @PostMapping("/signup")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    /**
     * Login a user
     * 
     * @param request The login data for the user
     * @return The login response
     */
    @PreAuthorize("permitAll")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginUserDto request) {
        try {
            User authenticatedUser = authService.authenticate(request);

            String jwtToken = jwtService.generateToken(authenticatedUser);

            LoginResponseDto loginResponse = new LoginResponseDto();
            loginResponse.setToken(jwtToken);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());

            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        }
}