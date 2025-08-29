package ch.sectioninformatique.template.user;

import java.nio.CharBuffer;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.CredentialsDto;
import ch.sectioninformatique.template.security.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing user-related operations.
 * This class provides functionality for:
 * - User authentication and registration
 * - User role management (promotion, revocation)
 * - User deletion
 * - Azure user integration
 * - User search and retrieval
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    /** Repository for user data access */
    private final UserRepository userRepository;

    /** Encoder for password hashing */
    private final PasswordEncoder passwordEncoder;

    /** Mapper for converting between User entities and DTOs */
    private final UserMapper userMapper;

    /**
     * Authenticates a user with their credentials.
     *
     * @param credentialsDto The user's login credentials
     * @return UserDto containing the authenticated user's information
     * @throws AppException if the user is not found or the password is invalid
     */
    public UserDto login(CredentialsDto credentialsDto) {
        User user = userRepository.findByLogin(credentialsDto.login())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()), user.getPassword())) {
            return userMapper.toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    /**
     * Checks if an actor can perform an action on a target based on their roles.
     * The hierarchy is:
     * - ADMIN can perform actions on all roles
     * - MANGER can perform actions on USER and MANAGER roles
     * - USER cannot perform actions on any role
     *
     * @param actorRole  The role of the actor performing the action
     * @param targetRole The role of the target of the action
     * @return true if the actor can perform the action, false otherwise
     */
    private boolean canPerformAction(RoleEnum actorRole, RoleEnum targetRole) {
        switch (actorRole) {
            case ADMIN:
                return true;
            case MANAGER:
                if (targetRole == RoleEnum.ADMIN) {
                    return false;
                }
                return true;
            case USER:
                return false;
            default:
                return false;
        }
    }
}
