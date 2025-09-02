package ch.sectioninformatique.template.user;

import java.util.ArrayList;
import java.util.List;

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
     * @throws RuntimeException if the user is not found, already an manager, or the
     *                          manager role is not found
     */
    public UserDto promoteToTestUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Role role : user.getAppSpecificRoles()) {
            if (role.getName().equals(RoleEnum.USER_TEST)) {
                throw new RuntimeException("The user is already a test user");
            }
        }

        Role testUserRole = roleRepository.findByName(RoleEnum.USER_TEST)
                .orElseThrow(() -> new RuntimeException("Test user role not found"));

        user.getAppSpecificRoles().add(testUserRole);
        userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return List of all User entities
     */
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
}
