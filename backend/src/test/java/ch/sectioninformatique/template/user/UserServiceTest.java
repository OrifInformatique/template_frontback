package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.auth.credentials.CredentialsDto;
import ch.sectioninformatique.template.auth.signup.SignUpDto;
import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.http.HttpStatus;
import ch.sectioninformatique.template.item.Item;

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "logs/test_logs.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final AtomicInteger totalTests = new AtomicInteger(0);
    private static final AtomicInteger successfulTests = new AtomicInteger(0);
    private static final AtomicInteger failedTests = new AtomicInteger(0);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    /**
     * Sets up the test environment before each test execution.
     * This method:
     * 1. Configures the security context for authentication testing
     * 2. Creates the logs directory if it doesn't exist
     * 3. Initializes the test environment with necessary mocks
     * 
     * @throws IOException if there's an error creating the logs directory
     */
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        try {
            // Create logs directory if it doesn't exist
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
        } catch (IOException e) {
            System.err.println("Error creating logs directory: " + e.getMessage());
        }
    }

    /**
     * Logs the result of a test execution to a file.
     * This method:
     * 1. Records the test execution details including:
     *    - Timestamp of execution
     *    - Test name
     *    - Success/failure status
     *    - Execution duration
     *    - Test message
     * 2. Captures error details if the test failed
     * 3. Updates test statistics
     * 4. Generates a summary when all tests are complete
     * 
     * @param testName The name of the test being executed
     * @param success Whether the test passed or failed
     * @param message A descriptive message about the test result
     * @param duration The time taken to execute the test in milliseconds
     * @param error The exception thrown if the test failed, null if successful
     */
    private void logTestResult(String testName, boolean success, String message, long duration, Throwable error) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now().format(formatter);
            String status = success ? "SUCCESS" : "FAILURE";
            
            writer.write(String.format("=== Test Result ===%n"));
            writer.write(String.format("Timestamp: %s%n", timestamp));
            writer.write(String.format("Test Name: %s%n", testName));
            writer.write(String.format("Status: %s%n", status));
            writer.write(String.format("Duration: %d ms%n", duration));
            writer.write(String.format("Message: %s%n", message));
            
            if (error != null) {
                writer.write(String.format("Error Details:%n"));
                writer.write(String.format("Message: %s%n", error.getMessage()));
                writer.write(String.format("Stack Trace:%n"));
                for (StackTraceElement element : error.getStackTrace()) {
                    writer.write(String.format("  %s%n", element.toString()));
                }
            }
            
            writer.write(String.format("==================%n%n"));
            
            // Update statistics
            totalTests.incrementAndGet();
            if (success) {
                successfulTests.incrementAndGet();
            } else {
                failedTests.incrementAndGet();
            }
            
            // Log summary if this is the last test
            if (totalTests.get() == 8) { // Update this number based on total tests
                logTestSummary();
            }
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Generates a summary of all test executions.
     * This method:
     * 1. Records the final timestamp
     * 2. Calculates the success rate
     * 3. Logs the total number of tests executed
     * 4. Logs the number of successful and failed tests
     * 5. Provides a percentage-based success rate
     */
    private void logTestSummary() {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now().format(formatter);
            double successRate = (successfulTests.get() * 100.0) / totalTests.get();
            
            writer.write(String.format("=== Test Summary ===%n"));
            writer.write(String.format("Timestamp: %s%n", timestamp));
            writer.write(String.format("Total Tests: %d%n", totalTests.get()));
            writer.write(String.format("Successful Tests: %d%n", successfulTests.get()));
            writer.write(String.format("Failed Tests: %d%n", failedTests.get()));
            writer.write(String.format("Success Rate: %.2f%%%n", successRate));
            writer.write(String.format("===================%n%n"));
        } catch (IOException e) {
            System.err.println("Error writing test summary: " + e.getMessage());
        }
    }

    /**
     * Tests the successful login scenario.
     * Verifies that:
     * 1. A user can log in with correct credentials
     * 2. The returned UserDto matches the expected data
     * 3. The password verification is performed correctly
     * 4. The user repository is queried for the login
     */
    @Test
    void login_Successful_ReturnsUserDto() {
        String testName = "login_Successful_ReturnsUserDto";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            String login = "john@test.com";
            String password = "password123";
            User user = new User(1L, "John", "Doe", login, "hashedPassword", null, null, null);
            UserDto expectedDto = new UserDto(1L, "John", "Doe", login, null, "ROLE_USER", null);
            
            when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(CharBuffer.wrap(password), user.getPassword())).thenReturn(true);
            when(userMapper.toUserDto(user)).thenReturn(expectedDto);

            // Act
            UserDto result = userService.login(new CredentialsDto(login, password.toCharArray()));

            // Assert
            assertEquals(expectedDto, result);
            verify(userRepository).findByLogin(login);
            verify(passwordEncoder).matches(CharBuffer.wrap(password), user.getPassword());
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Login successful for user: " + login, duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    /**
     * Tests the login failure scenario when user doesn't exist.
     * Verifies that:
     * 1. An AppException is thrown when user is not found
     * 2. The exception has the correct message
     * 3. The exception has the correct HTTP status (NOT_FOUND)
     */
    @Test
    void login_UserNotFound_ThrowsAppException() {
        String testName = "login_UserNotFound_ThrowsAppException";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            String login = "nonexistent@test.com";
            String password = "password123";
            
            when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

            // Act & Assert
            AppException exception = assertThrows(AppException.class, 
                () -> userService.login(new CredentialsDto(login, password.toCharArray())));
            assertEquals("Unknown user", exception.getMessage());
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Correctly handled non-existent user: " + login, duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    /**
     * Tests the login failure scenario with invalid password.
     * Verifies that:
     * 1. An AppException is thrown when password is incorrect
     * 2. The exception has the correct message
     * 3. The exception has the correct HTTP status (BAD_REQUEST)
     * 4. The password verification is performed correctly
     */
    @Test
    void login_InvalidPassword_ThrowsAppException() {
        String testName = "login_InvalidPassword_ThrowsAppException";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            String login = "john@test.com";
            String password = "wrongpassword";
            User user = new User(1L, "John", "Doe", login, "hashedPassword", null, null, null);
            
            when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(CharBuffer.wrap(password), user.getPassword())).thenReturn(false);

            // Act & Assert
            AppException exception = assertThrows(AppException.class, 
                () -> userService.login(new CredentialsDto(login, password.toCharArray())));
            assertEquals("Invalid password", exception.getMessage());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Correctly handled invalid password for user: " + login, duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    /**
     * Tests the successful user registration scenario.
     * Verifies that:
     * 1. A new user can be registered with valid data
     * 2. The password is properly encoded
     * 3. The default USER role is assigned
     * 4. The user is saved in the repository
     * 5. The returned UserDto matches the expected data
     */
    @Test
    void register_Successful_ReturnsUserDto() {
        String testName = "register_Successful_ReturnsUserDto";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            String login = "newuser@test.com";
            String password = "password123";
            SignUpDto signUpDto = new SignUpDto("New", "User", login, password.toCharArray());
            
            User user = new User();
            user.setId(1L);
            user.setFirstName("New");
            user.setLastName("User");
            user.setLogin(login);
            user.setPassword("hashedPassword");
            user.setRoles(new HashSet<>());
            
            UserDto expectedDto = new UserDto(1L, "New", "User", login, null, "ROLE_USER", null);
            Role userRole = new Role();
            userRole.setId(1L);
            userRole.setName(RoleEnum.USER);
            
            when(userRepository.findByLogin(login)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(CharBuffer.wrap(password))).thenReturn("hashedPassword");
            when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(userRole));
            when(userMapper.signUpToUser(signUpDto)).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserDto(user)).thenReturn(expectedDto);

            // Act
            UserDto result = userService.register(signUpDto);

            // Assert
            assertEquals(expectedDto, result);
            verify(userRepository).findByLogin(login);
            verify(passwordEncoder).encode(CharBuffer.wrap(password));
            verify(roleRepository).findByName(RoleEnum.USER);
            verify(userRepository).save(user);
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Successfully registered new user: " + login, duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    /**
     * Tests the registration failure scenario when login already exists.
     * Verifies that:
     * 1. An AppException is thrown when login is already taken
     * 2. The exception has the correct message
     * 3. The exception has the correct HTTP status (BAD_REQUEST)
     */
    @Test
    void register_LoginExists_ThrowsAppException() {
        String testName = "register_LoginExists_ThrowsAppException";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            String login = "existing@test.com";
            String password = "password123";
            SignUpDto signUpDto = new SignUpDto("Existing", "User", login, password.toCharArray());
            
            User existingUser = new User(1L, "Existing", "User", login, "hashedPassword", null, null, null);
            
            when(userRepository.findByLogin(login)).thenReturn(Optional.of(existingUser));

            // Act & Assert
            AppException exception = assertThrows(AppException.class, 
                () -> userService.register(signUpDto));
            assertEquals("Login already exists", exception.getMessage());
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Correctly handled existing login: " + login, duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    /**
     * Tests the successful admin promotion scenario.
     * Verifies that:
     * 1. A regular user can be promoted to admin
     * 2. The user's roles are updated correctly
     * 3. The admin role is assigned
     * 4. The user is saved in the repository
     * 5. The returned UserDto reflects the admin role
     */
    @Test
    void promoteToAdmin_Successful_ReturnsUserDto() {
        String testName = "promoteToAdmin_Successful_ReturnsUserDto";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            Long userId = 1L;
            User user = new User();
            user.setId(userId);
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setLogin("john@test.com");
            user.setPassword("pass");
            user.setRoles(new HashSet<>());
            
            Role userRole = new Role();
            userRole.setId(1L);
            userRole.setName(RoleEnum.USER);
            Role adminRole = new Role();
            adminRole.setId(2L);
            adminRole.setName(RoleEnum.ADMIN);
            user.getRoles().add(userRole);
            
            UserDto expectedDto = new UserDto(userId, "John", "Doe", "john@test.com", null, "ROLE_ADMIN", null);
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserDto(user)).thenReturn(expectedDto);

            // Act
            UserDto result = userService.promoteToAdmin(userId);

            // Assert
            assertEquals(expectedDto, result);
            verify(userRepository).findById(userId);
            verify(roleRepository).findByName(RoleEnum.ADMIN);
            verify(userRepository).save(user);
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Successfully promoted user to admin: " + user.getLogin(), duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    @Test
    void promoteToAdmin_UserNotFound_ThrowsRuntimeException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.promoteToAdmin(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void promoteToAdmin_AlreadyAdmin_ThrowsRuntimeException() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setLogin("john@test.com");
        user.setPassword("pass");
        user.setRoles(new HashSet<>());
        
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(RoleEnum.ADMIN);
        user.getRoles().add(adminRole);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.promoteToAdmin(userId));
        assertEquals("The user is already an admin", exception.getMessage());
    }

    /**
     * Tests the successful user deletion scenario.
     * Verifies that:
     * 1. An admin can delete a regular user
     * 2. The user's items are properly handled
     * 3. The user is removed from the repository
     * 4. The authentication context is properly checked
     */
    @Test
    void deleteUser_Successful_DeletesUserAndTransfersItems() {
        String testName = "deleteUser_Successful_DeletesUserAndTransfersItems";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            Long userId = 2L;
            User userToDelete = new User();
            userToDelete.setId(userId);
            userToDelete.setFirstName("John");
            userToDelete.setLastName("Doe");
            userToDelete.setLogin("john@test.com");
            userToDelete.setPassword("pass");
            userToDelete.setRoles(new HashSet<>());
            Role userRole = new Role();
            userRole.setId(1L);
            userRole.setName(RoleEnum.USER);
            userToDelete.getRoles().add(userRole);
            
            User authenticatedUser = new User();
            authenticatedUser.setId(3L);
            authenticatedUser.setFirstName("Admin");
            authenticatedUser.setLastName("User");
            authenticatedUser.setLogin("admin@test.com");
            authenticatedUser.setPassword("pass");
            authenticatedUser.setRoles(new HashSet<>());
            Role adminRole = new Role();
            adminRole.setId(2L);
            adminRole.setName(RoleEnum.ADMIN);
            authenticatedUser.getRoles().add(adminRole);
            
            User deletedUser = new User();
            deletedUser.setId(1L);
            deletedUser.setFirstName("Deleted");
            deletedUser.setLastName("User");
            deletedUser.setLogin("deleted@test.com");
            deletedUser.setPassword("pass");
            deletedUser.setRoles(new HashSet<>());
            
            UserDto authenticatedUserDto = new UserDto(3L, "Admin", "User", "admin@test.com", null, "ROLE_ADMIN", null);
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authenticatedUserDto);
            when(userRepository.findByLogin("admin@test.com")).thenReturn(Optional.of(authenticatedUser));
            when(userRepository.findById(1L)).thenReturn(Optional.of(deletedUser));
            List<Item> userItems = new ArrayList<>();
            when(itemRepository.findAll()).thenReturn(userItems);

            // Act
            userService.deleteUser(userId);

            // Assert
            verify(userRepository).findById(userId);
            verify(userRepository).findByLogin("admin@test.com");
            verify(userRepository).findById(1L);
            verify(userRepository).deleteById(userId);
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Successfully deleted user and transferred items: " + userToDelete.getLogin(), duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }

    /**
     * Tests the unauthorized user deletion scenario.
     * Verifies that:
     * 1. A regular user cannot delete another user
     * 2. A RuntimeException is thrown with the correct message
     * 3. The authentication context is properly checked
     * 4. The user repository is not modified
     */
    @Test
    void deleteUser_Unauthorized_ThrowsRuntimeException() {
        String testName = "deleteUser_Unauthorized_ThrowsRuntimeException";
        long startTime = System.currentTimeMillis();
        try {
            // Arrange
            Long userId = 2L;
            User userToDelete = new User();
            userToDelete.setId(userId);
            userToDelete.setFirstName("John");
            userToDelete.setLastName("Doe");
            userToDelete.setLogin("john@test.com");
            userToDelete.setPassword("pass");
            userToDelete.setRoles(new HashSet<>());
            Role adminRole = new Role();
            adminRole.setId(2L);
            adminRole.setName(RoleEnum.ADMIN);
            userToDelete.getRoles().add(adminRole);
            
            User authenticatedUser = new User();
            authenticatedUser.setId(3L);
            authenticatedUser.setFirstName("Regular");
            authenticatedUser.setLastName("User");
            authenticatedUser.setLogin("user@test.com");
            authenticatedUser.setPassword("pass");
            authenticatedUser.setRoles(new HashSet<>());
            Role userRole = new Role();
            userRole.setId(1L);
            userRole.setName(RoleEnum.USER);
            authenticatedUser.getRoles().add(userRole);
            
            UserDto authenticatedUserDto = new UserDto(3L, "Regular", "User", "user@test.com", null, "ROLE_USER", null);
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authenticatedUserDto);
            when(userRepository.findByLogin("user@test.com")).thenReturn(Optional.of(authenticatedUser));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.deleteUser(userId));
            assertEquals("You don't have the necessary rights to perform this action", exception.getMessage());
            
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, true, "Correctly handled unauthorized deletion attempt for user: " + userToDelete.getLogin(), duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logTestResult(testName, false, "Error: " + e.getMessage(), duration, e);
            throw e;
        }
    }
}