package ch.sectioninformatique.template.security;

import ch.sectioninformatique.template.app.exceptions.AppException;
import org.springframework.http.HttpStatus;

/**
 * Container class for security-related exceptions.
 * This class groups all security-specific exceptions as static inner classes.
 */
public class SecurityExceptions {

    /**
     * Thrown when an authentication token is invalid or expired.
     */
    public static class InvalidTokenException extends AppException {
        public InvalidTokenException() {
            super("security.token.invalid", HttpStatus.UNAUTHORIZED);
        }

        public InvalidTokenException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when a refresh token is invalid or expired.
     */
    public static class InvalidRefreshTokenException extends AppException {
        public InvalidRefreshTokenException() {
            super("security.refresh.invalid", HttpStatus.UNAUTHORIZED);
        }

        public InvalidRefreshTokenException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token verification fails.
     */
    public static class JwtVerificationException extends AppException {
        public JwtVerificationException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }

        public JwtVerificationException() {
            super("security.jwt.verificationFailed", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token has expired.
     */
    public static class JwtTokenExpiredException extends AppException {
        public JwtTokenExpiredException() {
            super("security.jwt.expired", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token signature is invalid.
     */
    public static class InvalidJwtSignatureException extends AppException {
        public InvalidJwtSignatureException() {
            super("security.jwt.invalidSignature", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when JWT token format is malformed.
     */
    public static class MalformedJwtException extends AppException {
        public MalformedJwtException() {
            super("security.jwt.malformed", HttpStatus.BAD_REQUEST);
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
            super("security.jwt.invalidType", HttpStatus.UNAUTHORIZED);
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
            super("security.auth.required", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when the Authorization header is missing.
     */
    public static class MissingAuthorizationHeaderException extends AppException {
        public MissingAuthorizationHeaderException() {
            super("security.authHeader.missing", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Thrown when the Authorization header format is invalid.
     */
    public static class InvalidAuthorizationHeaderException extends AppException {
        public InvalidAuthorizationHeaderException() {
            super("security.authHeader.invalidFormat", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when access to a resource is denied due to insufficient permissions.
     */
    public static class AccessDeniedException extends AppException {
        public AccessDeniedException() {
            super("error.accessDenied", HttpStatus.FORBIDDEN);
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
            super("security.role.insufficient", HttpStatus.FORBIDDEN, new Object[] { requiredRole });
        }

        public InsufficientRoleException() {
            super("security.role.insufficient", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when a user lacks the required permission for an operation.
     */
    public static class InsufficientPermissionException extends AppException {
        public InsufficientPermissionException(String requiredPermission) {
            super("security.permission.insufficient", HttpStatus.FORBIDDEN, new Object[] { requiredPermission });
        }

        public InsufficientPermissionException() {
            super("security.permission.insufficient", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when security context is invalid or corrupted.
     */
    public static class InvalidSecurityContextException extends AppException {
        public InvalidSecurityContextException() {
            super("security.context.invalid", HttpStatus.UNAUTHORIZED);
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
            super("security.token.creationFailed", HttpStatus.INTERNAL_SERVER_ERROR, new Object[] { message });
        }

        public TokenCreationException() {
            super("security.token.creationFailed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when security configuration fails.
     */
    public static class SecurityConfigurationException extends AppException {
        public SecurityConfigurationException(String message) {
            super("security.config.error", HttpStatus.INTERNAL_SERVER_ERROR, new Object[] { message });
        }
    }

    /**
     * Thrown when CORS configuration is violated.
     */
    public static class CorsViolationException extends AppException {
        public CorsViolationException() {
            super("security.cors.violation", HttpStatus.FORBIDDEN);
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
            super("security.session.invalid", HttpStatus.UNAUTHORIZED);
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
            super("security.authProvider.error", HttpStatus.INTERNAL_SERVER_ERROR, new Object[] { message });
        }
    }
}
