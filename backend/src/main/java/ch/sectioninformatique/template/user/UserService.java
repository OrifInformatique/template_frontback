package ch.sectioninformatique.template.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.RegisterDto;
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
     * Promotes a user to the local admin role.
     * This operation:
     * - Verifies the user exists
     * - Checks if the user is already an manager or admin
     * - Removes existing roles and assigns the admin role
     *
     * @param userId The ID of the user to promote
     * @return UserDto containing the updated user's information
     * @throws RuntimeException if the user is not found, already an manager, or the
     *                          manager role is not found
     */
    public UserDto promoteToTestAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Role role : user.getAppSpecificRoles()) {
            if (role.getName().equals(RoleEnum.ADMIN_TEST)) {
                throw new RuntimeException("The user is already a test admin");
            }
        }

        Role testAdminRole = roleRepository.findByName(RoleEnum.ADMIN_TEST)
                .orElseThrow(() -> new RuntimeException("ADMIN_TEST role not found"));

        user.getAppSpecificRoles().add(testAdminRole);
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

    /**
     * Retrieves the user in the system.
     *
     * @return List of all User entities
     */
    public User me(UserDto currentUser) {
        List<User> users = userRepository.findAll();
        User localUser = null;
        for(User user : users){
            if(user.getUsername().contains(currentUser.getLogin())){
                localUser = user;
            }
        }
        return localUser;
    }

    /**
     * Registers a new user in the system.
     * This method:
     * - Checks if the login is already taken
     * - Assigns the default USER role
     * - Saves the user to the database
     *
     * @param userDto The user registration data
     * @return UserDto containing the created user's information
     * @throws AppException if the login already exists or the default role is not found
     */
    public User register(RegisterDto userDto) {
        Optional<User> optionalUser = userRepository.findByLogin(userDto.login());

        if (optionalUser.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(userDto);

        // Add default USER role
        Role userRole = roleRepository.findByName(RoleEnum.USER)
            .orElseThrow(() -> new AppException("Default role not found", HttpStatus.INTERNAL_SERVER_ERROR));
        user.setMainRole(userRole);

        User savedUser = userRepository.save(user);
        return savedUser;
    }
}
