package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.auth.credentials.CredentialsDto;
import ch.sectioninformatique.template.auth.signup.SignUpDto;
import ch.sectioninformatique.template.item.ItemRepository;
import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserService class is the service for the User entity.
 */
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Autowired
    private ItemRepository itemRepository;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDto login(CredentialsDto credentialsDto) {
        User user = userRepository.findByLogin(credentialsDto.login())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()), user.getPassword())) {
            return userMapper.toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public UserDto register(SignUpDto userDto) {
        Optional<User> optionalUser = userRepository.findByLogin(userDto.login());

        if (optionalUser.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(userDto);
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.password())));

        User savedUser = userRepository.save(user);

        return userMapper.toUserDto(savedUser);
    }

    public UserDto findByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        return userMapper.toUserDto(user);
    }

    /**
     * Get all users.
     * 
     * @return The list of all users
     */
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }

    /**
     * Promote a user to admin role
     * 
     * @param userId The ID of the user to promote
     * @throws RuntimeException if the user is not found
     * @throws RuntimeException if the user is already an admin
     * @throws RuntimeException if the user is a super admin
     * @throws RuntimeException if the admin role is not found
     */
    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (user.getRole().getName().equals(RoleEnum.ADMIN)) {
            throw new RuntimeException("The user is already an admin");
        }
        if (user.getRole().getName().equals(RoleEnum.SUPER_ADMIN)) {
            throw new RuntimeException("The user is already a super admin");
        }
        
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
            .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
        user.getRoles().clear();
        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    /**
     * Revoke an admin role from a user
     * 
     * @param userId The ID of the user to revoke the admin role from
     * @throws RuntimeException if the user is not found
     * @throws RuntimeException if the user is already a user
     * @throws RuntimeException if the user role is not found
     */
    public void revokeAdminRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole().getName().equals(RoleEnum.USER)) {
            throw new RuntimeException("The user is already a user");
        }
        if (user.getRole().getName().equals(RoleEnum.SUPER_ADMIN)) {
            throw new RuntimeException("You don't have the necessary rights to delete a super admin");
        }

        Role userRole = roleRepository.findByName(RoleEnum.USER)
            .orElseThrow(() -> new RuntimeException("User role not found"));

        user.getRoles().clear();
        user.getRoles().add(userRole);
        userRepository.save(user);
    }

    /**
     * Promote an user to super admin role
     * 
     * @param userId The ID of the user to promote
     * @throws RuntimeException if the user is not found
     * @throws RuntimeException if the user is already a super admin
     * @throws RuntimeException if the super admin role is not found
     */
    public void promoteToSuperAdmin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getName().equals(RoleEnum.SUPER_ADMIN)) {
            throw new RuntimeException("The user is already a super admin");
        }

        Role superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN)
            .orElseThrow(() -> new RuntimeException("Super admin role not found"));

        user.getRoles().clear();
        user.getRoles().add(superAdminRole);
        userRepository.save(user);
    }

    /**
     * Downgrade a super admin to an admin role
     * 
     * @param userId The ID of the user to downgrade
     * @throws RuntimeException if the user is not found
     * @throws RuntimeException if the user is already an admin
     * @throws RuntimeException if the user role is not found
     */
    public void downgradeSuperAdminRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole().getName().equals(RoleEnum.USER)) {
            throw new RuntimeException("The user has lower rights than desired");
        }
        if (user.getRole().getName().equals(RoleEnum.ADMIN)) {
            throw new RuntimeException("The user is already an admin");
        }

        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
            .orElseThrow(() -> new RuntimeException("Admin role not found"));

        user.getRoles().clear();
        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    /**
     * Revoke the super admin role from a user
     * 
     * @param userId The ID of the user to revoke the super admin role from
     * @throws RuntimeException if the user is not found
     * @throws RuntimeException if the user is already a user
     * @throws RuntimeException if the user role is not found
     */
    public void revokeSuperAdminRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getName().equals(RoleEnum.USER)) {
            throw new RuntimeException("The user is already a user");
        }

        Role userRole = roleRepository.findByName(RoleEnum.USER)
            .orElseThrow(() -> new RuntimeException("User role not found"));

        user.getRoles().clear();
        user.getRoles().add(userRole);
        userRepository.save(user);
    }

    /**
     * Check if the actor can perform the action on the target  
     * 
     * @param actorRole The role of the actor
     * @param targetRole The role of the target
     * @return true if the actor can perform the action on the target, false otherwise
     */
    private boolean canPerformAction(RoleEnum actorRole, RoleEnum targetRole) {
        switch (actorRole) {
            case SUPER_ADMIN:
                return true;
            case ADMIN:
                if (targetRole == RoleEnum.SUPER_ADMIN) {
                    return false;
                }
                return true;
            case USER:
                return false;
            default:
                return false;
        }
    }

    /**
     * Delete a user and transfer their items to the deleted user account (ID 1)
     * 
     * @param userId The ID of the user to delete
     * @throws RuntimeException if the user is not found
     * @throws RuntimeException if the authenticated user doesn't have sufficient permissions
     * @throws RuntimeException if the deleted user account (ID 1) is not found
     */
    public void deleteUser(Long userId) {
        // Get the user to delete
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the authenticated user (the actor)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();

        // Check if the action is authorized
        if (!canPerformAction(authenticatedUser.getRole().getName(), userToDelete.getRole().getName())) {
            throw new RuntimeException("You don't have the necessary rights to perform this action");
        }

        // Get the "deleted user" (id=1)
        User deletedUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Deleted user not found"));

        // Transfer all items to the "deleted user"
        itemRepository.findAll().forEach(item -> {
            if (item.getAuthor().equals(userToDelete)) {
                item.setAuthor(deletedUser);
                itemRepository.save(item);
            }
        });

        // Delete the user
        userRepository.deleteById(userId);
    }
}
