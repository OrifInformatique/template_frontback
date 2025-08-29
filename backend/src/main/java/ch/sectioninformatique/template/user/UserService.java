package ch.sectioninformatique.template.user;

import org.springframework.stereotype.Service;

import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
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

    /** Repository for role data access */
    private final RoleRepository roleRepository;

    /** Mapper for converting between User entities and DTOs */
    private final UserMapper userMapper;

    /**
     * Promotes a user to the manager role.
     * This operation:
     * - Verifies the user exists
     * - Checks if the user is already an manager or admin
     * - Removes existing roles and assigns the manager role
     *
     * @param userId The ID of the user to promote
     * @return UserDto containing the updated user's information
     * @throws RuntimeException if the user is not found, already an manager, or the manager role is not found
     */
    public UserDto promoteToTestUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (user.getRole().getName().equals(RoleEnum.USER_TEST)) {
            throw new RuntimeException("The user is already a test user");
        }

        Role testUserRole = roleRepository.findByName(RoleEnum.USER_TEST)
            .orElseThrow(() -> new RuntimeException("Test user role not found"));
            
        user.getRoles().add(testUserRole);
        userRepository.save(user);
        return userMapper.toUserDto(user);
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
