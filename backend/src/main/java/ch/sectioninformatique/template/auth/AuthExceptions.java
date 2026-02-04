package ch.sectioninformatique.template.auth;

import ch.sectioninformatique.template.app.exceptions.AppException;
import org.springframework.http.HttpStatus;

/**
 * Container class for authentication and authorization related exceptions.
 * This class groups all auth-specific exceptions as static inner classes.
 */
public class AuthExceptions {

    /**
     * Thrown when provided credentials are invalid.
     */
    public static class InvalidCredentialsException extends AppException {
        public InvalidCredentialsException() {
            super("auth.invalidCredentials", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when a user registration fails.
     */
    public static class RegistrationFailedException extends AppException {
        public RegistrationFailedException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user is not found during authentication.
     */
    public static class UserNotFoundException extends AppException {
        public UserNotFoundException() {
            super("auth.userNotFound", HttpStatus.NOT_FOUND);
        }

        public UserNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Thrown when a user already exists during registration.
     */
    public static class UserAlreadyExistsException extends AppException {
        public UserAlreadyExistsException() {
            super("auth.userAlreadyExists", HttpStatus.CONFLICT);
        }

        public UserAlreadyExistsException(String message) {
            super(message, HttpStatus.CONFLICT);
        }
    }

    /**
     * Thrown when password update fails.
     */
    public static class PasswordUpdateFailedException extends AppException {
        public PasswordUpdateFailedException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when OAuth2 authentication fails.
     */
    public static class OAuth2AuthenticationException extends AppException {
        public OAuth2AuthenticationException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when a login is already taken.
     */
    public static class LoginAlreadyExistsException extends AppException {
        public LoginAlreadyExistsException() {
            super("auth.loginAlreadyExists", HttpStatus.BAD_REQUEST);
        }

        public LoginAlreadyExistsException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }
}
