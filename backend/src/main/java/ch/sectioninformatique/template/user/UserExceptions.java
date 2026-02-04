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
            super("user.notFound", HttpStatus.NOT_FOUND);
        }

        public UserNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }

        public UserNotFoundException(Long userId) {
            super("user.notFound.id", HttpStatus.NOT_FOUND, new Object[] { userId });
        }
    }

    /**
     * Thrown when a user is not found by login.
     */
    public static class UserNotFoundByLoginException extends AppException {
        public UserNotFoundByLoginException(String login) {
            super("user.notFound.login", HttpStatus.NOT_FOUND, new Object[] { login });
        }
    }

    /**
     * Thrown when a user already has a specific role.
     */
    public static class UserAlreadyHasRoleException extends AppException {
        public UserAlreadyHasRoleException(String roleName) {
            super("user.role.alreadyHas", HttpStatus.CONFLICT, new Object[] { roleName });
        }

        public UserAlreadyHasRoleException() {
            super("user.role.alreadyHas", HttpStatus.CONFLICT);
        }
    }

    /**
     * Thrown when a user creation operation fails.
     */
    public static class UserCreationException extends AppException {
        public UserCreationException(String message) {
            super("user.create.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }

        public UserCreationException() {
            super("user.create.failed", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user update operation fails.
     */
    public static class UserUpdateException extends AppException {
        public UserUpdateException(String message) {
            super("user.update.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }

        public UserUpdateException() {
            super("user.update.failed", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user deletion operation fails.
     */
    public static class UserDeletionException extends AppException {
        public UserDeletionException(String message) {
            super("user.delete.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }

        public UserDeletionException(String messageKey, boolean useKey) {
            super(messageKey, HttpStatus.BAD_REQUEST);
        }

        public UserDeletionException() {
            super("user.delete.failed", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when attempting to delete a user that is already soft-deleted.
     */
    public static class UserAlreadyDeletedException extends AppException {
        public UserAlreadyDeletedException() {
            super("user.alreadyDeleted", HttpStatus.CONFLICT);
        }

        public UserAlreadyDeletedException(Long userId) {
            super("user.alreadyDeleted.id", HttpStatus.CONFLICT, new Object[] { userId });
        }
    }

    /**
     * Thrown when a role promotion operation fails.
     */
    public static class UserPromotionException extends AppException {
        public UserPromotionException(String message) {
            super("user.promote.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }
    }

    /**
     * Thrown when a role is not found.
     */
    public static class RoleNotFoundException extends AppException {
        public RoleNotFoundException(String roleName) {
            super("user.role.notFound", HttpStatus.NOT_FOUND, new Object[] { roleName });
        }

        public RoleNotFoundException() {
            super("user.role.notFound", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Thrown when the default role is not found during user creation.
     */
    public static class DefaultRoleNotFoundException extends AppException {
        public DefaultRoleNotFoundException() {
            super("user.role.defaultNotFound", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thrown when user data validation fails.
     */
    public static class UserValidationException extends AppException {
        public UserValidationException(String message) {
            super("user.validation.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }

        public UserValidationException(String messageKey, boolean useKey) {
            super(messageKey, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Thrown when a user mapping operation fails.
     */
    public static class UserMappingException extends AppException {
        public UserMappingException(String message) {
            super("user.mapping.failed", HttpStatus.INTERNAL_SERVER_ERROR, new Object[] { message });
        }
    }

    /**
     * Thrown when user seeding operation fails.
     */
    public static class UserSeedingException extends AppException {
        public UserSeedingException(String message) {
            super("user.seeding.failed", HttpStatus.INTERNAL_SERVER_ERROR, new Object[] { message });
        }
    }

    /**
     * Thrown when user retrieval operation fails.
     */
    public static class UserRetrievalException extends AppException {
        public UserRetrievalException(String message) {
            super("user.retrieve.failed", HttpStatus.INTERNAL_SERVER_ERROR, new Object[] { message });
        }
    }

    /**
     * Thrown when attempting to perform an operation on an inactive user.
     */
    public static class InactiveUserException extends AppException {
        public InactiveUserException() {
            super("user.inactive", HttpStatus.FORBIDDEN);
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
            super("user.role.update.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }
    }

    /**
     * Thrown when duplicate user data is detected.
     */
    public static class DuplicateUserException extends AppException {
        public DuplicateUserException(String message) {
            super("user.duplicate", HttpStatus.CONFLICT, new Object[] { message });
        }

        public DuplicateUserException() {
            super("user.duplicate", HttpStatus.CONFLICT);
        }
    }

    /**
     * Thrown when a permanent user deletion operation fails.
     */
    public static class PermanentUserDeletionException extends AppException {
        public PermanentUserDeletionException(String message) {
            super("user.delete.permanent.failed", HttpStatus.BAD_REQUEST, new Object[] { message });
        }

        public PermanentUserDeletionException() {
            super("user.delete.permanent.failed", HttpStatus.BAD_REQUEST);
        }
    }
}
