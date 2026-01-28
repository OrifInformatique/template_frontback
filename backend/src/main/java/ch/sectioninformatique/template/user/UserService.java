package ch.sectioninformatique.template.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.RegisterDto;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;

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

    /** EntityManager for database operations */
    @Autowired
    private EntityManager entityManager;

    /** Repository for user data access */
    @Autowired
    private final UserRepository userRepository;

    /** Repository for role data access */
    private final RoleRepository roleRepository;

    /** Mapper for converting between User entities and DTOs */
    private final UserMapper userMapper;

    /**
     * Promotes a user to a local app role.
     * This operation:
     * - Verifies the user exists
     * - Checks if the user already has the local app role
     * - Removes existing roles and assigns the local app role
     *
     * @param userId The ID of the user to promote
     * @return UserDto containing the updated user's information
     * @throws RuntimeException if the user is not found, already has the role, or
     *                          the role is not found
     */
    public UserDto promoteToLocalAppRole(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        for (Role role : user.getAppSpecificRoles()) {
            if (role.getName().equals(RoleEnum.LOCAL_APP_ROLE)) {
                throw new AppException("The user is already a test admin", HttpStatus.CONFLICT);
            }
        }

        Role testAdminRole = roleRepository.findByName(RoleEnum.LOCAL_APP_ROLE)
                .orElseThrow(() -> new AppException("LOCAL_APP_ROLE role not found", HttpStatus.NOT_FOUND));

        user.getAppSpecificRoles().add(testAdminRole);
        userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    /**
     * Retrieves all users in the system (not including soft-deleted users).
     *
     * @return List of all User entities
     */
    public List<UserDto> allUsers() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedFilter").setParameter("isDeleted", false);
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(userMapper.toUserDto(user));
        }
        return usersDto;
    }

    /**
     * Retrieves all users including soft-deleted ones.
     *
     * @return List of all User entities including deleted
     */
    public List<UserDto> allWithDeletedUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAllIncludingDeleted().forEach(users::add);
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(userMapper.toUserDto(user));
        }
        return usersDto;
    }

    /**
     * Retrieves only soft-deleted users.
     *
     * @return List of soft-deleted User entities
     */
    public List<UserDto> deletedUsers() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedFilter").setParameter("isDeleted", true);
        List<User> users = new ArrayList<>();
        userRepository.findAllDeleted().forEach(users::add);
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(userMapper.toUserDto(user));
        }
        return usersDto;
    }

    /**
     * Retrieves the authenticated user's information.
     *
     * @return UserDto containing the current user's information
     */
    public UserDto me(UserDto currentUser) {
        User user = userRepository.findByLogin(currentUser.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDto userMapped = userMapper.toUserDto(user);
        return userMapped;
    }

    /**
     * Registers a new user in the system.
     * This method:
     * - Checks if the login is already taken
     * - Assigns the default USER role
     * - Saves the user to the database
     *
     * @param registerDto The user registration data
     * @return User containing the created user's information
     * @throws AppException if the login already exists or the default role is not
     *                      found
     */
    public User register(RegisterDto registerDto) {
        Optional<User> optionalUser = userRepository.findByLogin(registerDto.login());

        if (optionalUser.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(registerDto);

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
     * @param localUser The local user's data
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

    /**
     * Deletes a user from the system.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param userId The ID of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws AppException if the user is not found
     */
    public UserDto deleteUser(@NonNull Long userId) {
        // Get the user to delete
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Delete the user
        userRepository.deleteById(userId);
        return userMapper.toUserDto(userToDelete);
    }

    /**
     * Permanently deletes a user from the system.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param userId The ID of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws AppException if the user is not found
     */
    public UserDto deleteUserPermanent(@NonNull Long userId) {
        // Get the user to delete
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Delete the user
        userRepository.deletePermanentlyById(userId);
        return userMapper.toUserDto(userToDelete);
    }

    /**
     * Deletes a user from the system by login.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param login The login of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws AppException if the user is not found
     */
    public UserDto deleteUserByLogin(String login) {
        // Get the user to delete
        User userToDelete = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Delete the user
        userRepository.deleteById(userToDelete.getId());
        return userMapper.toUserDto(userToDelete);
    }

    /**
     * Permanently deletes a user from the system by login.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param login The login of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws AppException if the user is not found
     */
    public UserDto deleteUserPermanentByLogin(String login) {
        // Get the user to delete
        User userToDelete = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Delete the user
        userRepository.deletePermanentlyById(userToDelete.getId());
        return userMapper.toUserDto(userToDelete);
    }

    /**
     * Finds a user by their login.
     * This method:
     * - Searches the database for a user with the specified login
     * - Throws an exception if the user is not found
     * - Maps the User entity to a UserDto
     *
     * @param login The login of the user to find
     * @return UserDto containing the user's information
     * @throws AppException if the user is not found
     */
    public UserDto findByLogin(String login) {
        log.debug("Searching for user with login: {}", login);

        Optional<User> userOptional = userRepository.findByLogin(login);
        log.debug("User found in database: {}", userOptional.isPresent());

        User user = userOptional
                .orElseThrow(() -> {
                    log.error("User not found with login: {}", login);
                    return new AppException("Unknown user", HttpStatus.NOT_FOUND);
                });

        log.debug("User details - ID: {}, FirstName: {}, LastName: {}, Roles: {}",
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getMainRole());

        UserDto userDto = userMapper.toUserDto(user);
        log.debug("Mapped to UserDto - ID: {}, FirstName: {}, LastName: {}, Role: {}",
                userDto.getId(), userDto.getFirstName(), userDto.getLastName(), userDto.getMainRole());

        return userDto;
    }
}
