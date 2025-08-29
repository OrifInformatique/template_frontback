package ch.sectioninformatique.template.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link UserAuthenticationProvider}.
 * This class tests the JWT token creation, validation, and authentication
 * functionality of the UserAuthenticationProvider.
 */
@ExtendWith(MockitoExtension.class)
class UserAuthenticationProviderTest {

    @Mock
    private UserService userService;

    private UserAuthenticationProvider authenticationProvider;

    private static final String TEST_LOGIN = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";

    /**
     * Tests the token creation functionality.
     * Verifies that:
     * - Token is created successfully
     * - Token contains correct user claims
     * - Token is properly formatted
     */
    @Test
    void testCreateToken() {
        // Given
        UserDto user = UserDto.builder()
                .login(TEST_LOGIN)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .role("USER")
                .permissions(Arrays.asList("read", "write"))
                .build();

        // When
        String token = authenticationProvider.createToken(user);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    /**
     * Tests the basic token validation.
     * Verifies that:
     * - Valid token is accepted
     * - Authentication object is created with correct user details
     * - Authorities are properly set
     */
    @Test
    void testValidateToken() {
        // Given
        UserDto user = UserDto.builder()
                .login(TEST_LOGIN)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .role("USER")
                .permissions(Arrays.asList("read", "write"))
                .build();

        String token = authenticationProvider.createToken(user);

        // When
        Authentication authentication = authenticationProvider.validateToken(token);

        // Then
        assertNotNull(authentication);
        assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
        assertEquals(TEST_LOGIN, ((UserDto) authentication.getPrincipal()).getLogin());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().startsWith("ROLE_")));
    }

    /**
     * Tests the authority building functionality.
     * Verifies that:
     * - Role is properly prefixed with "ROLE_"
     * - Permissions are correctly converted to authorities
     * - All authorities are included in the result
     */
    @Test
    void testBuildAuthorities() throws Exception {
        // Given
        String role = "USER";
        List<String> permissions = Arrays.asList("read", "write");

        // When
        Method method = UserAuthenticationProvider.class.getDeclaredMethod("buildAuthorities", String.class, List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) method.invoke(authenticationProvider, role, permissions);

        // Then
        assertNotNull(authorities);
        assertEquals(3, authorities.size()); // ROLE_USER + 2 permissions
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("read")));
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("write")));
    }
} 