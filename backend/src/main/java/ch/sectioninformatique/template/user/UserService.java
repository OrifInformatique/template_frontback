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
     * Promotes a user to the local application example role.
     * This operation:
     * - Verifies the user exists
     * - Checks if the user has already the local application example role
     * - Removes existing roles and assigns the local application example role
     *
     * @param userId The ID of the user to promote
     * @return UserDto containing the updated user's information
     * @throws RuntimeException if the user is not found, already has the role, or the
     *                          role is not found
     */
    public UserDto promoteToLocalAppRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Role role : user.getAppSpecificRoles()) {
            if (role.getName().equals(RoleEnum.LOCAL_APP_ROLE)) {
                throw new RuntimeException("The user has already the local app role");
            }
        }

        Role localAppRole = roleRepository.findByName(RoleEnum.LOCAL_APP_ROLE)
                .orElseThrow(() -> new RuntimeException("LOCAL_APP_ROLE role not found"));

        user.getAppSpecificRoles().add(localAppRole);
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
        for (User user : users) {
            if (user.getUsername().contains(currentUser.getLogin())) {
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
     * @return User containing the created user's information
     * @throws AppException if the login already exists or the default role is not
     *                      found
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

    /**
     * Get or create an authenticated user.
     * This method:
     * - Get the list of user in the database
     * - test the existence of the cureent user
     * - Register the current user if not in the database
     *
     * @param userDto The user registration data
     * @return Found or created user
     */
    public User getOrCreateAuthenticatedUser(UserDto userDto) {
        List<User> users = userRepository.findAll();

        User localUser = null;

        for (User user : users) {
            if (user.getUsername().contains(userDto.getLogin())) {
                localUser = user;
            }
        }

        if (localUser == null) {
            RegisterDto newUser = new RegisterDto(userDto.getFirstName(), userDto.getLastName(),
                    userDto.getLogin(), null);

            localUser = this.register(newUser);
        }

        return localUser;
    }

    /**
     * Update The main role of an autheticated user if different.
     * This method:
     * - Test if the current user's main role is the same as the one in the database
     * - Get the apropriate role in the database
     * - Change the local user Role for the corrext one
     *
     * @param localUser   The local user's data
     * @param currentUser The the transmitted user's data
     */
    public void updateMainRole(User localUser, UserDto currentUser) {

        String localMainRole = localUser.getMainRole().getName().name();

        if (!localMainRole.contains(currentUser.getMainRole())) {
            Role newMainRole = roleRepository.findByName(RoleEnum.valueOf(currentUser.getMainRole()))
                    .orElseThrow(() -> new RuntimeException("role not found"));

            localUser.setMainRole(newMainRole);
            userRepository.save(localUser);
        }

    }

    /**
     * get the list of roles attribuated to the user
     * This method:
     * - Test if the current user's has app specifique roles registered
     * - add the app specifique roles to a list 
     * - add the main role of the user to the list
     *
     * @param localUser   The local user's data
     * @return list of roles 
     */
    public List<String> getRolesList(User localUser) {

        List<String> allRoles = new ArrayList<>();

        if (localUser.getAppSpecificRoles() != null) {
            for (String role : localUser.getAppSpecificRolesString()) {
                allRoles.add(role);
            }
        }

        allRoles.add(localUser.getMainRole().getName().name());

        return allRoles;
    }
}
