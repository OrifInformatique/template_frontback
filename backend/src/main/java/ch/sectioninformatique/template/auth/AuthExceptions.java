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
            super("Invalid credentials", HttpStatus.UNAUTHORIZED);
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
     * Thrown when an authentication token is invalid or expired.
     */
    public static class InvalidTokenException extends AppException {
        public InvalidTokenException() {
            super("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when a refresh token is invalid or expired.
     */
    public static class InvalidRefreshTokenException extends AppException {
        public InvalidRefreshTokenException() {
            super("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when a user is not authenticated.
     */
    public static class UnauthenticatedException extends AppException {
        public UnauthenticatedException() {
            super("Authentication required", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when a user is not authorized to access a resource.
     */
    public static class UnauthorizedException extends AppException {
        public UnauthorizedException() {
            super("Access denied", HttpStatus.FORBIDDEN);
        }

        public UnauthorizedException(String message) {
            super(message, HttpStatus.FORBIDDEN);
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
            super("Login already exists", HttpStatus.BAD_REQUEST);
        }

        public LoginAlreadyExistsException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }
}
