package ch.sectioninformatique.template.security;

import ch.sectioninformatique.template.app.exceptions.AppException;
import org.springframework.http.HttpStatus;

/**
 * Container class for security-related exceptions.
 * This class groups all security-specific exceptions as static inner classes.
 */
public class SecurityExceptions {

    /**
     * Thrown when JWT token verification fails.
     */
    public static class JwtVerificationException extends AppException {
        public JwtVerificationException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }

        public JwtVerificationException() {
            super("JWT token verification failed", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token has expired.
     */
    public static class JwtTokenExpiredException extends AppException {
        public JwtTokenExpiredException() {
            super("JWT token has expired", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token signature is invalid.
     */
    public static class InvalidJwtSignatureException extends AppException {
        public InvalidJwtSignatureException() {
            super("Invalid JWT signature", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token format is malformed.
     */
    public static class MalformedJwtException extends AppException {
        public MalformedJwtException() {
            super("Malformed JWT token", HttpStatus.BAD_REQUEST);
        }

        public MalformedJwtException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when the token type is invalid for a specific endpoint.
     */
    public static class InvalidTokenTypeException extends AppException {
        public InvalidTokenTypeException() {
            super("Invalid token type for this endpoint", HttpStatus.UNAUTHORIZED);
        }

        public InvalidTokenTypeException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when authentication is required but not provided.
     */
    public static class AuthenticationRequiredException extends AppException {
        public AuthenticationRequiredException() {
            super("Authentication required", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when the Authorization header is missing.
     */
    public static class MissingAuthorizationHeaderException extends AppException {
        public MissingAuthorizationHeaderException() {
            super("Missing authorization header", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when the Authorization header format is invalid.
     */
    public static class InvalidAuthorizationHeaderException extends AppException {
        public InvalidAuthorizationHeaderException() {
            super("Invalid authorization header format", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when access to a resource is denied due to insufficient permissions.
     */
    public static class AccessDeniedException extends AppException {
        public AccessDeniedException() {
            super("Access denied", HttpStatus.FORBIDDEN);
        }

        public AccessDeniedException(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when a user lacks the required role for an operation.
     */
    public static class InsufficientRoleException extends AppException {
        public InsufficientRoleException(String requiredRole) {
            super("Insufficient role. Required: " + requiredRole, HttpStatus.FORBIDDEN);
        }

        public InsufficientRoleException() {
            super("Insufficient role", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when a user lacks the required permission for an operation.
     */
    public static class InsufficientPermissionException extends AppException {
        public InsufficientPermissionException(String requiredPermission) {
            super("Insufficient permission. Required: " + requiredPermission, HttpStatus.FORBIDDEN);
        }

        public InsufficientPermissionException() {
            super("Insufficient permission", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when security context is invalid or corrupted.
     */
    public static class InvalidSecurityContextException extends AppException {
        public InvalidSecurityContextException() {
            super("Invalid security context", HttpStatus.UNAUTHORIZED);
        }

        public InvalidSecurityContextException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token creation fails.
     */
    public static class TokenCreationException extends AppException {
        public TokenCreationException(String message) {
            super("Failed to create token: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        public TokenCreationException() {
            super("Failed to create token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when security configuration fails.
     */
    public static class SecurityConfigurationException extends AppException {
        public SecurityConfigurationException(String message) {
            super("Security configuration error: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when CORS configuration is violated.
     */
    public static class CorsViolationException extends AppException {
        public CorsViolationException() {
            super("CORS policy violation", HttpStatus.FORBIDDEN);
        }

        public CorsViolationException(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when session has expired or is invalid.
     */
    public static class InvalidSessionException extends AppException {
        public InvalidSessionException() {
            super("Invalid or expired session", HttpStatus.UNAUTHORIZED);
        }

        public InvalidSessionException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when authentication provider fails.
     */
    public static class AuthenticationProviderException extends AppException {
        public AuthenticationProviderException(String message) {
            super("Authentication provider error: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
