package ch.sectioninformatique.template.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.app.exceptions.AppException;
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

    /** Client to send authentication requests to the spring-auth application */
    @Autowired
    private final AuthClient authClient;

    /**
     * Handles POST requests to "/login"
     * Accepts login credentials (login and password) as a request body, validated
     * for correctness
     * Calls the authentication client to perform login with provided credentials
     * Returns a reactive Mono<ResponseEntity<UserDto>>containing the login response (e.g., token or
     * status message)
     * 
     * @param credentialsDto
     * @return Mono<ResponseEntity<UserDto>> with login response
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<UserDto>> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        return ResponseEntity.ok(authClient.login(credentialsDto)).getBody();
    }

    /**
     * Handles POST requests to "/register"
     * Accepts user registration data as a request body, validated for correctness
     * Calls the authentication client to perform registration with provided user data
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
                    // On successful registration, also register user locally
                    userService.register(user);

                    // Return HTTP 200 OK with the response body
                    return Mono.just(response);
                })
                .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
    }

    /**
     * Handles PUT requests to "/update-password"
     * Accepts new password data as a request body, validated for correctness
     * Calls the authentication client to set the new password for the user
     * Returns a reactive Mono<ResponseEntity<MessageResponseDto>> containing the
     * response message
     * 
     * @param token The authorization token from the request header
     * @param updatePasswordDto The UpdatePasswordDto containing the old and new passwords
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

    /**
     * Handles DELETE requests to "/{userId}/{local}"
     * Deletes a user either locally or from the auth service based on the 'local'
     * flag
     * If 'local' is true, deletes the user from the local database
     * If 'local' is false, deletes the user from the local database and calls the
     * authClient to delete the user from the auth
     * service
     * Returns a ResponseEntity with success message or error details
     * 
     * @param userId The ID of the user to delete
     * @param local  Flag indicating whether to delete locally or from auth service
     * @return ResponseEntity with deletion result message
     */
    @DeleteMapping("/{userId}/{local}")
    public Mono<ResponseEntity<?>> deleteUser(@RequestHeader("Authorization") String token, @PathVariable Long userId,
            @PathVariable boolean local) {
        if (local) {
            userService.deleteUser(userId);
            return Mono.just(ResponseEntity.ok(Map.of("message", "Local User deleted successfully")));
        } else {
            // Call authClient to delete user from auth service if needed
            return authClient.deleteUser(token, userId)
                    .flatMap(response -> {

                        // Extract body from ResponseEntity
                        Map<String, String> body = response.getBody();
                        if (body != null && body.containsKey("deletedUserLogin")) {
                            
                            // Delete user locally
                            userService.deleteUserByLogin(body.get("deletedUserLogin"));

                            // Include both message in response
                            return Mono.just((ResponseEntity<?>) ResponseEntity.ok(Map.of(
                                    "message", body.get("message"))));

                        } else {
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("message", "Failed to delete user: missing response data")));
                        }
                    })
                    .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
        }
    }
}
