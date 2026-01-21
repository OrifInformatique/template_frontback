package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.app.exceptions.AppException;
import org.springframework.http.HttpStatus;

/**
 * Container class for user-related exceptions.
 * This class groups all user-specific exceptions as static inner classes.
 */
public class UserExceptions {

    /**
     * Thrown when a user is not found.
     */
    public static class UserNotFoundException extends AppException {
        public UserNotFoundException() {
            super("User not found", HttpStatus.NOT_FOUND);
        }

        public UserNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }

        public UserNotFoundException(Long userId) {
            super("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Thrown when a user is not found by login.
     */
    public static class UserNotFoundByLoginException extends AppException {
        public UserNotFoundByLoginException(String login) {
            super("User not found with login: " + login, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Thrown when a login already exists.
     */
    public static class LoginAlreadyExistsException extends AppException {
        public LoginAlreadyExistsException() {
            super("Login already exists", HttpStatus.BAD_REQUEST);
        }

        public LoginAlreadyExistsException(String login) {
            super("Login already exists: " + login, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user already has a specific role.
     */
    public static class UserAlreadyHasRoleException extends AppException {
        public UserAlreadyHasRoleException(String roleName) {
            super("The user already has the " + roleName + " role", HttpStatus.CONFLICT);
        }

        public UserAlreadyHasRoleException() {
            super("User already has the specified role", HttpStatus.CONFLICT);
        }
    }

    /**
     * Thrown when a user creation operation fails.
     */
    public static class UserCreationException extends AppException {
        public UserCreationException(String message) {
            super("Failed to create user: " + message, HttpStatus.BAD_REQUEST);
        }

        public UserCreationException() {
            super("Failed to create user", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user update operation fails.
     */
    public static class UserUpdateException extends AppException {
        public UserUpdateException(String message) {
            super("Failed to update user: " + message, HttpStatus.BAD_REQUEST);
        }

        public UserUpdateException() {
            super("Failed to update user", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user deletion operation fails.
     */
    public static class UserDeletionException extends AppException {
        public UserDeletionException(String message) {
            super("Failed to delete user: " + message, HttpStatus.BAD_REQUEST);
        }

        public UserDeletionException() {
            super("Failed to delete user", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when attempting to delete a user that is already soft-deleted.
     */
    public static class UserAlreadyDeletedException extends AppException {
        public UserAlreadyDeletedException() {
            super("User is already deleted", HttpStatus.CONFLICT);
        }

        public UserAlreadyDeletedException(Long userId) {
            super("User with ID " + userId + " is already deleted", HttpStatus.CONFLICT);
        }
    }

    /**
     * Thrown when a role promotion operation fails.
     */
    public static class UserPromotionException extends AppException {
        public UserPromotionException(String message) {
            super("Failed to promote user: " + message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a role is not found.
     */
    public static class RoleNotFoundException extends AppException {
        public RoleNotFoundException(String roleName) {
            super(roleName + " role not found", HttpStatus.NOT_FOUND);
        }

        public RoleNotFoundException() {
            super("Role not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Thrown when the default role is not found during user creation.
     */
    public static class DefaultRoleNotFoundException extends AppException {
        public DefaultRoleNotFoundException() {
            super("Default role not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when user data validation fails.
     */
    public static class UserValidationException extends AppException {
        public UserValidationException(String message) {
            super("User validation failed: " + message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user mapping operation fails.
     */
    public static class UserMappingException extends AppException {
        public UserMappingException(String message) {
            super("Failed to map user: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when user seeding operation fails.
     */
    public static class UserSeedingException extends AppException {
        public UserSeedingException(String message) {
            super("User seeding failed: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when trying to access a user that requires specific permissions.
     */
    public static class InsufficientUserPermissionsException extends AppException {
        public InsufficientUserPermissionsException() {
            super("Insufficient permissions to access user data", HttpStatus.FORBIDDEN);
        }

        public InsufficientUserPermissionsException(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when user retrieval operation fails.
     */
    public static class UserRetrievalException extends AppException {
        public UserRetrievalException(String message) {
            super("Failed to retrieve user: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when attempting to perform an operation on an inactive user.
     */
    public static class InactiveUserException extends AppException {
        public InactiveUserException() {
            super("User account is inactive", HttpStatus.FORBIDDEN);
        }

        public InactiveUserException(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Thrown when a user's main role update fails.
     */
    public static class UserRoleUpdateException extends AppException {
        public UserRoleUpdateException(String message) {
            super("Failed to update user role: " + message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when duplicate user data is detected.
     */
    public static class DuplicateUserException extends AppException {
        public DuplicateUserException(String message) {
            super("Duplicate user detected: " + message, HttpStatus.CONFLICT);
        }

        public DuplicateUserException() {
            super("Duplicate user detected", HttpStatus.CONFLICT);
        }
    }

    /**
     * Thrown when a permanent user deletion operation fails.
     */
    public static class PermanentUserDeletionException extends AppException {
        public PermanentUserDeletionException(String message) {
            super("Failed to permanently delete user: " + message, HttpStatus.BAD_REQUEST);
        }

        public PermanentUserDeletionException() {
            super("Failed to permanently delete user", HttpStatus.BAD_REQUEST);
        }
    }
}
