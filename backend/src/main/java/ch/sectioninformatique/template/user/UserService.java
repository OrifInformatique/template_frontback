package ch.sectioninformatique.template.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.jspecify.annotations.NonNull;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.auth.RegisterDto;
import ch.sectioninformatique.template.security.LocalRoleEnum;
import ch.sectioninformatique.template.security.MainRoleEnum;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.user.UserExceptions.DefaultRoleNotFoundException;
import ch.sectioninformatique.template.user.UserExceptions.DuplicateUserException;
import ch.sectioninformatique.template.user.UserExceptions.RoleNotFoundException;
import ch.sectioninformatique.template.user.UserExceptions.UserAlreadyHasRoleException;
import ch.sectioninformatique.template.user.UserExceptions.UserNotFoundByLoginException;
import ch.sectioninformatique.template.user.UserExceptions.UserNotFoundException;
import ch.sectioninformatique.template.user.UserExceptions.UserCreationException;
import ch.sectioninformatique.template.user.UserExceptions.UserPromotionException;
import ch.sectioninformatique.template.user.UserExceptions.UserUpdateException;
import ch.sectioninformatique.template.user.UserExceptions.UserDeletionException;
import ch.sectioninformatique.template.user.UserExceptions.UserValidationException;
import ch.sectioninformatique.template.user.UserExceptions.PermanentUserDeletionException;
import ch.sectioninformatique.template.user.UserExceptions.UserRetrievalException;
import ch.sectioninformatique.template.user.UserExceptions.InactiveUserException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
@SuppressWarnings("null")
public class UserService {

    /** EntityManager for database operations */
    @PersistenceContext
    private EntityManager entityManager;
    private final MessageSource messageSource;

    /** Repository for user data access */
    private final UserRepository userRepository;

    /** Repository for role data access */
    private final RoleRepository roleRepository;

    /** Client for authentication operations */
    private final AuthClient authClient;

    /** Mapper for converting between User entities and DTOs */
    private final UserMapper userMapper;

    /**
     * Promotes a user to the local manager role.
     * This operation:
     * - Verifies the user exists
     * - Checks if the user already has the local manager role
     * - Removes existing roles and assigns the local manager role
     *
     * @param userId The ID of the user to promote
     * @return UserDto containing the updated user's information
     * @throws UserNotFoundException if the user is not found
     * @throws UserAlreadyHasRoleException if the user already has the role
     * @throws RoleNotFoundException if the role is not found
     * @throws UserPromotionException if the promotion operation fails
     */
    public UserDto promoteToLocalManagerRole(@NonNull Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);

            for (Role role : user.getAppSpecificRoles()) {
                if (role.getName().equals(LocalRoleEnum.LOCAL_MANAGER)) {
                    throw new UserAlreadyHasRoleException(LocalRoleEnum.LOCAL_MANAGER.name());
                }
            }

            Role testAdminRole = roleRepository.findByName(LocalRoleEnum.LOCAL_MANAGER)
                    .orElseThrow(() -> new RoleNotFoundException(LocalRoleEnum.LOCAL_MANAGER.name()));

            user.getAppSpecificRoles().add(testAdminRole);
            userRepository.save(user);
            return userMapper.toUserDto(user);
        } catch (UserNotFoundException | UserAlreadyHasRoleException | RoleNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserPromotionException(e.getMessage());
        }
    }

    /**
     * Retrieves all users in the system (not including soft-deleted users).
     *
     * @return List of all users as UserDto
     */
    public List<UserDto> allUsers() {
        List<User> users = new ArrayList<>();
        users = userRepository.findAllByDeletedFalse();

        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(userMapper.toUserDto(user));
        }
        return usersDto;
    }

    /**
     * Retrieves all users including soft-deleted ones.
     *
     * @return List of all users as UserDto including deleted ones
     */
    public List<UserDto> allWithDeletedUsers() {
        List<User> users = new ArrayList<>();
        users = userRepository.findAllIncludingDeleted();

        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(userMapper.toUserDto(user));
        }
        return usersDto;
    }

    /**
     * Retrieves only soft-deleted users.
     *
     * @return List of soft-deleted users as UserDto
     */
        public List<UserDto> deletedUsers() {
        List<User> users = new ArrayList<>();
        users = userRepository.findAllDeleted();

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
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException(currentUser.getId()));
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
     * @throws DuplicateUserException if the login already exists
     * @throws DefaultRoleNotFoundException if the default role is not found
     * @throws UserCreationException if user creation fails
     */
    public User register(RegisterDto registerDto, Long userId) {
        try {
            // Basic validation before persisting. The last name is optional, as in
            // spring-auth: some Azure accounts have none.
            if (registerDto.login() == null || registerDto.login().isBlank()
                    || registerDto.firstName() == null || registerDto.firstName().isBlank()) {
                throw new UserValidationException("user.validation.missingFields", true);
            }

            Optional<User> optionalUser = userRepository.findByLogin(registerDto.login());

            if (optionalUser.isPresent()) {
                throw new DuplicateUserException(registerDto.login());
            }

            User user = userMapper.signUpToUser(registerDto, userId);

            // Define the user's main role (owned by spring-auth, transversal for all
            // applications) - stored locally as a plain enum value, no DB lookup.
            if(registerDto.mainRole() == null || registerDto.mainRole().isBlank()){
                // No role is provided, add default USER role
                user.setMainRole(MainRoleEnum.USER);
            }
            else{
                // Add the provided role
                user.setMainRole(MainRoleEnum.valueOf(registerDto.mainRole()));
            }

            // Define the user's specific (local) role(s) for this application
            if(registerDto.appSpecificRoles() != null ){

                Set<Role> appSpecificRoles = new HashSet<>();
                for (String role : registerDto.appSpecificRoles()) {
                    appSpecificRoles.add(roleRepository.findByName(LocalRoleEnum.valueOf(role)).orElseThrow(RoleNotFoundException::new));
                }
                user.setAppSpecificRoles(appSpecificRoles);
            }
            
            User savedUser = userRepository.save(user);
            return savedUser;
        } catch (UserValidationException | DuplicateUserException | DefaultRoleNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserCreationException(e.getMessage());
        }
    }

    /**
     * Gets or creates the local user matching a user authenticated by spring-auth.
     * This method:
     * - Looks the local user up by its id, which is the spring-auth user id
     * - Keeps a user soft-deleted locally as is: the soft delete only hides it
     *   from the user lists, it can still use the application
     * - Keeps the local login and names in sync with spring-auth
     * - Registers the user locally if it has no local row yet
     *
     * @param userDto The user as authenticated by spring-auth
     * @param userId  The spring-auth user id, also the local user id
     * @return Found or created user
     * @throws DuplicateUserException if another local user already has this login
     * @throws UserCreationException if user creation fails
     */
    public User getOrCreateAuthenticatedUser(UserDto userDto, Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isEmpty()) {
                RegisterDto newUser = new RegisterDto(userDto.getFirstName(), userDto.getLastName(),
                        userDto.getLogin(), null, userDto.getMainRole(), null);
                return this.register(newUser, userId);
            }

            User localUser = optionalUser.get();

            // spring-auth owns the login and names: mirror any change made there
            if (!Objects.equals(localUser.getLogin(), userDto.getLogin())
                    || !Objects.equals(localUser.getFirstName(), userDto.getFirstName())
                    || !Objects.equals(localUser.getLastName(), userDto.getLastName())) {
                localUser.setLogin(userDto.getLogin());
                localUser.setFirstName(userDto.getFirstName());
                localUser.setLastName(userDto.getLastName());
                localUser = userRepository.save(localUser);
            }

            return localUser;
        } catch (DuplicateUserException | DefaultRoleNotFoundException
                | UserCreationException | UserValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("ERROR : {} | Message : {}", e.getClass(), e.getMessage(), e);
            throw new UserCreationException(e.getMessage());
        }
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
     * @throws RoleNotFoundException if the new role is not found
     * @throws UserUpdateException if the role update fails
     */
    public void updateMainRole(User localUser, UserDto currentUser) {
        try {
            String localMainRole = localUser.getMainRole().name();

            if (!localMainRole.equals(currentUser.getMainRole())) {
                localUser.setMainRole(MainRoleEnum.valueOf(currentUser.getMainRole()));
                userRepository.save(localUser);
            }
        }
        catch (RoleNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new RoleNotFoundException(currentUser.getMainRole());
        } catch (Exception e) {
            throw new UserUpdateException(e.getMessage());
        }
    }


    /**
     * Deletes a user from the system.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param userId The ID of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws UserNotFoundException if the user is not found
     * @throws UserDeletionException if the deletion fails
     */
    public UserDto deleteUser(@NonNull Long userId) {
        try {
            // Get the user to delete
            User userToDelete = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

            // Delete the user
            userRepository.deleteById(userToDelete.getId());
            return userMapper.toUserDto(userToDelete);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserDeletionException(e.getMessage());
        }
    }

    /**
     * Permanently deletes a user from the system.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param userId The ID of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws UserNotFoundException if the user is not found
     * @throws UserDeletionException if the permanent deletion fails
     */
    public UserDto deleteUserPermanent(@NonNull Long userId) {
        try {
            // Get the user to delete
            User userToDelete = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

            // Delete the user
            userRepository.deletePermanentlyById(userToDelete.getId());
            return userMapper.toUserDto(userToDelete);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PermanentUserDeletionException(e.getMessage());
        }
    }

    /**
     * Deletes a user from the system by ID.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param userId The ID of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws UserNotFoundException if the user is not found
     * @throws UserDeletionException if the deletion fails
     */
    public UserDto deleteUserById(Long userId) {
        try {
            // Get the user to delete
            User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

            // Delete the user
            userRepository.deleteById(userToDelete.getId());
            return userMapper.toUserDto(userToDelete);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserDeletionException(e.getMessage());
        }
    }

    /**
     * Permanently deletes a user from the system by ID.
     * This method:
     * - Verifies the user exists
     * - Deletes the user from the database
     *
     * @param userId The ID of the user to delete
     * @return UserDto containing the deleted user's information
     * @throws UserNotFoundException if the user is not found
     * @throws UserDeletionException if the permanent deletion fails
     */
    public UserDto deletePermanentById(Long userId) {
        try {
            // Get the user to delete
            User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

            // Delete the user
            userRepository.deletePermanentlyById(userToDelete.getId());
            return userMapper.toUserDto(userToDelete);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PermanentUserDeletionException(e.getMessage());
        }
    }

    /**
     * Gets a user by its ID, soft-deleted or not (the "deleted" field tells).
     *
     * @param userId The ID of the user
     * @return The user
     * @throws UserNotFoundException if no local user has this ID
     */
    public UserDto getUser(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Finds a user by their ID.
     * This method:
     * - Searches the database for a user with the specified ID
     * - Throws an exception if the user is not found
     * - Maps the User entity to a UserDto
     *
     * @param userId The ID of the user to find
     * @return UserDto containing the user's information
     * @throws AppException if the user is not found
     */
    public UserDto findById(Long userId) {
        log.debug("Searching for user with ID: {}", userId);
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            log.debug("User found in database: {}", userOptional.isPresent());

            User user = userOptional
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", userId);
                        return new UserNotFoundException(userId);
                    });

            if (user.isDeleted()) {
                throw new InactiveUserException("user.inactive.orDeleted");
            }

            log.debug("User details - ID: {}, FirstName: {}, LastName: {}, Roles: {}",
                    user.getId(), user.getFirstName(), user.getLastName(),
                    user.getMainRole());

            UserDto userDto = userMapper.toUserDto(user);
            log.debug("Mapped to UserDto - ID: {}, FirstName: {}, LastName: {}, Role: {}",
                    userDto.getId(), userDto.getFirstName(), userDto.getLastName(), userDto.getMainRole());

            return userDto;
        } catch (UserNotFoundException | InactiveUserException e) {
            throw e;
        } catch (Exception e) {
            throw new UserRetrievalException(e.getMessage());
        }
    }

    /**
     * Find a user by their login.
     * This method:
     * - Searches the database for a user with the specified login
     * - Throws an exception if the user is not found
     * 
     * @param login The login of the user to find
     * @return a UserDto containing the user's information
     * @throws AppException if the user is not found
     */
    public UserDto findByLogin(String login) {
        log.debug("Searching for user with login: {}", login);
        try {
            Optional<User> userOptional = userRepository.findByLogin(login);
            log.debug("User found in database: {}", userOptional.isPresent());

            User user = userOptional
                    .orElseThrow(() -> {
                        log.error("User not found with login: {}", login);
                        return new UserNotFoundByLoginException(login);
                    });

            if (user.isDeleted()) {
                throw new InactiveUserException("user.inactive.orDeleted");
            }

            log.debug("User details - ID: {}, FirstName: {}, LastName: {}, Roles: {}",
                    user.getId(), user.getFirstName(), user.getLastName(),
                    user.getMainRole());

            UserDto userDto = userMapper.toUserDto(user);
            log.debug("Mapped to UserDto - ID: {}, FirstName: {}, LastName: {}, Role: {}",
                    userDto.getId(), userDto.getFirstName(), userDto.getLastName(), userDto.getMainRole());

            return userDto;
        } catch (UserNotFoundByLoginException | InactiveUserException e) {
            throw e;
        } catch (Exception e) {
            throw new UserRetrievalException(e.getMessage());
        }
    }

    /**
     * Deletes a user globally (via AuthClient) and locally.
     * This method:
     * - Calls the AuthClient to delete user from the global auth service
     * - Once the auth service confirmed the deletion (204 No Content), deletes
     *   the user locally by id (the local id is the spring-auth id), permanently
     *   if {@code hardDelete} is true, softly otherwise
     *
     * @param token      The authorization token
     * @param userId     The ID of the user to delete
     * @param hardDelete Whether the global deletion is permanent
     * @return A Mono completing once the user is deleted globally and locally
     * @throws UserDeletionException if the global deletion fails
     */
    public reactor.core.publisher.Mono<Void> deleteGlobalAndLocal(String token, Long userId, boolean hardDelete) {

        // spring-auth answers 204 No Content on success and errors are mapped by AuthClient
        return authClient.deleteGlobalUser(token, userId, hardDelete)
                .then(reactor.core.publisher.Mono.fromRunnable(() -> {
                    // Local id is the spring-auth id: delete locally by the same id,
                    // with the same scope (soft or permanent) as the global deletion
                    if (hardDelete) {
                        deletePermanentById(userId);
                    } else {
                        deleteUserById(userId);
                    }
                }));
    }

    /**
     * Updates a user, globally in spring-auth and locally.
     * This method:
     * - Validates the requested main role and app-specific roles first, so that
     *   an unknown role fails before anything is changed
     * - Updates the profile (first name, last name, login) in spring-auth
     * - Changes the main role in spring-auth, if given and different from the
     *   current one (spring-auth owns the main roles)
     * - Mirrors the resulting global user locally, and replaces the local
     *   app-specific roles if given
     *
     * @param userId  The ID of the user to update
     * @param newUser The new user data
     * @param token   The authorization token for the request
     * @return The updated user
     * @throws UserNotFoundException if no local user has this id
     * @throws RoleNotFoundException if a requested role does not exist
     * @throws UserUpdateException if spring-auth refuses the profile update
     */
    public UserDto updateUser(Long userId, UserUpdateDto newUser, String token) {

        // Retrieve the existing user from the database
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // Resolve the requested roles before changing anything
        MainRoleEnum requestedMainRole = null;
        if (newUser.mainRole() != null && !newUser.mainRole().isBlank()) {
            try {
                requestedMainRole = MainRoleEnum.valueOf(newUser.mainRole());
            } catch (IllegalArgumentException e) {
                throw new RoleNotFoundException(newUser.mainRole());
            }
        }

        Set<Role> newAppSpecificRoles = null;
        if (newUser.appSpecificRoles() != null) {
            newAppSpecificRoles = new HashSet<>();
            for (String role : newUser.appSpecificRoles()) {
                Role newRole;
                try {
                    newRole = roleRepository.findByName(LocalRoleEnum.valueOf(role))
                        .orElseThrow(() -> new RoleNotFoundException(role));
                } catch (IllegalArgumentException | NullPointerException e) {
                    throw new RoleNotFoundException(role);
                }
                newAppSpecificRoles.add(newRole);
            }
        }

        // Update the profile in spring-auth (errors are mapped by AuthClient)
        UserDto globalUser = authClient.updateUser(token, userId, newUser.profile()).block().getBody();

        // Change the main role in spring-auth if needed: it refuses a change to
        // the current role, and checks that the actor may manage both roles
        if (requestedMainRole != null && !requestedMainRole.name().equals(globalUser.getMainRole())) {
            globalUser = authClient.changeMainRole(token, userId, requestedMainRole).block().getBody();
        }

        // Mirror the global user locally
        existingUser.setFirstName(globalUser.getFirstName());
        existingUser.setLastName(globalUser.getLastName());
        existingUser.setLogin(globalUser.getLogin());
        existingUser.setMainRole(MainRoleEnum.valueOf(globalUser.getMainRole()));
        if (newAppSpecificRoles != null) {
            existingUser.setAppSpecificRoles(newAppSpecificRoles);
        }

        // Save modified Entity
        return userMapper.toUserDto(userRepository.save(existingUser));
    }

    /**
     * Restores a soft-deleted user globally (via AuthClient) and locally.
     *
     * @param userId The ID of the user to restore
     * @param token The authorization token for the request
     * @return The restored user
     * @throws UserNotFoundException if the local user is not found
     * @throws ch.sectioninformatique.template.auth.AuthExceptions.UserNotFoundException
     *         if the global user is not found
     * @throws UserUpdateException if the global restoration fails
     */
    public UserDto restoreUser(Long userId, String token) {
        User userToRestore = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Restore the user in the global auth service
        authClient.restoreGlobalUser(token, userId).block();

        // Change deleted value in the Entity
        userToRestore.setDeleted(false);

        // Save modified Entity
        return userMapper.toUserDto(userRepository.save(userToRestore));
    }

    /**
     * Adds the local data to a user authenticated by spring-auth.
     * This method:
     * - Gets or creates the matching local user and syncs its main role
     * - Sets the user's app-specific roles, which spring-auth does not know
     * - Merges the global permissions granted by spring-auth (in the given DTO)
     *   with the local permissions of the user's main role and app-specific roles
     *
     * @param authUser The user as returned by spring-auth, updated in place
     * @return The same user, with its local roles and all its permissions
     */
    public UserDto addLocalRolesAndPermissions(UserDto authUser, Long userId) {
        User localUser = getOrCreateAuthenticatedUser(authUser, userId);
        updateMainRole(localUser, authUser);

        Set<String> permissions = new LinkedHashSet<>();
        if (authUser.getPermissions() != null) {
            permissions.addAll(authUser.getPermissions());
        }
        localUser.getAuthorities().forEach(authority -> permissions.add(authority.getAuthority()));

        authUser.setAppSpecificRoles(localUser.getAppSpecificRolesString().stream().sorted().toList());
        authUser.setPermissions(new ArrayList<>(permissions));
        return authUser;
    }
}
