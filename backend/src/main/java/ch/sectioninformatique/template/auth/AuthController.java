package ch.sectioninformatique.template.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Controller handling user authentication and registration.
 * This controller provides endpoints for user login and registration,
 * managing the authentication process and user creation.
 */
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    /** Service for handling user-related operations */
    private final UserService userService;

    private final UserAuthenticationProvider userAuthenticationProvider;

    /** Client to send authentication requests to the spring-auth application */
    @Autowired
    private final AuthClient authClient;

    /**
     * Handles POST requests to "/login"
     * Accepts login credentials (login and password) as a request body, validated
     * for correctness
     * Calls the authentication client to perform login with provided credentials
     * Returns a reactive Mono<ResponseEntity<UserDto>>containing the login response
     * (e.g., token or
     * status message)
     * 
     * @param credentialsDto
     * @return Mono<ResponseEntity<UserDto>> with login response
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<UserDto>> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        return authClient.login(credentialsDto)
                .map(responseEntity -> {
                    UserDto userDto = responseEntity.getBody();
                    if (userDto != null) {
                        userDto.setPermissions(userAuthenticationProvider.getLocalPermissions(userDto));
                        return ResponseEntity.ok(userDto);
                    } else {
                        // Handle null body just in case
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                });
    }

    /**
     * Handles POST requests to "/register"
     * Accepts user registration data as a request body, validated for correctness
     * Calls the authentication client to perform registration with provided user
     * data
     * On successful registration, also registers the user locally in the system
     * Returns a reactive Mono<ResponseEntity<UserDto>> containing the registration
     * response or error message
     * 
     * @param user
     * @return Mono<ResponseEntity<UserDto>> with registration response
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<UserDto>> register(@RequestBody @Valid RegisterDto user) {
        return authClient.register(user)
                .flatMap(response -> {
                    UserDto userDto = response.getBody();

                    if (userDto != null) {
                        // Register user locally
                        userService.register(user);

                        // Set local permissions
                        userDto.setPermissions(userAuthenticationProvider.getLocalPermissions(userDto));

                        return Mono.<ResponseEntity<UserDto>>just(ResponseEntity.ok(userDto));
                    } else {
                        return Mono.<ResponseEntity<UserDto>>just(
                                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    }
                })
                .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
    }

    /**
     * Handles PUT requests to "/set-password"
     * Accepts new password data as a request body, validated for correctness
     * Calls the authentication client to set the new password for the user
     * Returns a reactive Mono<ResponseEntity<MessageResponseDto>> containing the
     * response message
     * 
     * @param token          The authorization token from the request header
     * @param setPasswordDto The PasswordUpdateDto containing the new password data
     * @return Mono<ResponseEntity<MessageResponseDto>> with set password response
     */
    @PutMapping("/update-password")
    public Mono<ResponseEntity<MessageResponseDto>> updatePassword(@RequestHeader("Authorization") String token, @RequestBody @Valid PasswordUpdateDto updatePasswordDto) {
        return authClient.updatePassword(token, updatePasswordDto)
                .map(responseEntity -> {
                    MessageResponseDto messageResponse = responseEntity.getBody();
                    if (messageResponse != null) {
                        return ResponseEntity.ok(messageResponse);
                    } else {
                        // Handle null body just in case
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                });
    }
}
