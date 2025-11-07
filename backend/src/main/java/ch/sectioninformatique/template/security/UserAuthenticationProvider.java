package ch.sectioninformatique.template.security;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserRepository;
import ch.sectioninformatique.template.user.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider class for handling user authentication using JWT tokens.
 * This class is responsible for:
 * - Creating and validating JWT tokens
 * - Managing user authentication
 * - Handling OAuth2 integration
 * - Converting user roles and permissions into Spring Security authorities
 * - Managing Azure user creation for OAuth2 users
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UserAuthenticationProvider {

    /**
     * Service for user-related operations, including user creation and retrieval
     */
    private final UserService userService;

    private final UserRepository userRepository;

    /**
     * Secret key for JWT token signing and verification, configured via application
     * properties
     */
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    /**
     * Initializes the authentication provider by encoding the secret key.
     * This method is called after dependency injection to ensure the secret key
     * is properly encoded before use. The encoding helps prevent the raw secret key
     * from being available in the JVM memory.
     */
    @PostConstruct
    protected void init() {
        // this is to avoid having the raw secret key available in the JVM
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * Creates a JWT token for a user with their information and permissions.
     * The token includes:
     * - User login as subject
     * - First name and last name as claims
     * - Roles
     * - Issue time and expiration time (1 hour validity)
     *
     * @param user The user to create a token for
     * @return A JWT token string containing the user's information and permissions
     */
    public String createToken(UserDto user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1 hour

        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        List<String> authorities = this.getLocalPermissions(user);

        return JWT.create()
                .withSubject(user.getLogin())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("mainRole", user.getMainRole())
                .withClaim("appSpecificRoles", user.getAppSpecificRoles())
                .withClaim("permissions", authorities)
                .sign(algorithm);
    }

    /**
     * Return a list of authorities as Strings from a UserDto.
     *
     * @param user The UserDto
     * 
     * @return List of String of authorities
     */
    public List<String> getLocalPermissions(UserDto user) {
        Optional<User> localUser = userRepository.findByLogin(user.getLogin());

        List<String> roles = new ArrayList<>();

        List<String> authorities = new ArrayList<>();

        if (localUser.isPresent()) {
            roles.add(localUser.get().getMainRole().getName().name());

            for (String role : localUser.get().getAppSpecificRolesString()) {
                roles.add(role);
            }

            authorities = this.buildAuthorities(roles).stream()
                    .map(SimpleGrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else {
            authorities = user.getPermissions();
        }

        return authorities;
    }

    /**
     * Builds a list of authorities from a role and permissions.
     * This method converts:
     * - Role into a "ROLE_" prefixed authority
     * - Permissions into individual authorities
     * The resulting authorities are used by Spring Security for authorization
     * checks.
     *
     * @param role The user's role (e.g., "USER", "MANAGER")
     * 
     * @return List of SimpleGrantedAuthority objects for Spring Security
     */
    private List<SimpleGrantedAuthority> buildAuthorities(List<String> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            if (role != null && !role.isEmpty()) {
                Set<SimpleGrantedAuthority> authoritySet = RoleEnum.valueOf(role).getGrantedAuthorities();
                authorities.addAll(authoritySet);
            }
        }

        log.debug("Built authorities for role {}: {}", roles, authorities);
        return authorities;
    }

    /**
     * Validates a JWT token and creates an Authentication object.
     * This method performs basic token validation without checking the database.
     * It verifies:
     * - Token signature using the secret key
     * - Token expiration
     * - Token claims (user information)
     * 
     * It also modify the local informations based on the the validated token
     * informations
     * - It add new validated user
     * - it update main Roles for users
     *
     * @param token The JWT token to validate
     * @return Authentication object containing the user's information and
     *         authorities
     */
    public Authentication validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);
        log.debug("Token verified for subject: {}", decoded.getSubject());

        UserDto currentUser = UserDto.builder()
                .login(decoded.getSubject())
                .firstName(decoded.getClaim("firstName").asString())
                .lastName(decoded.getClaim("lastName").asString())
                .mainRole(decoded.getClaim("mainRole").asString())
                .appSpecificRoles(decoded.getClaim("appSpecificRoles").asList(String.class))
                .permissions(decoded.getClaim("permissions").asList(String.class))
                .build();

        User localUser = userService.getOrCreateAuthenticatedUser(currentUser);

        userService.updateMainRole(localUser, currentUser);

        List<String> allRoles = userService.getRolesList(localUser);

        List<SimpleGrantedAuthority> authorities = buildAuthorities(allRoles);

        return new UsernamePasswordAuthenticationToken(currentUser, null, authorities);
    }

}
