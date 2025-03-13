package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.user.UserDto;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserAuthenticationProvider {

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    private final UserService userService;

    @PostConstruct
    protected void init() {
        // this is to avoid having the raw secret key available in the JVM
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1 hour

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(user.getLogin())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("role", "ROLE_USER")  // Rôle par défaut pour les utilisateurs OAuth2
                .withClaim("permissions", List.of(
                    // OAuth2 scopes
                    "SCOPE_openid", 
                    "SCOPE_profile", 
                    "SCOPE_email", 
                    "SCOPE_User.Read",
                    // Item permissions
                    "item:read",
                    "item:write",
                    "item:update",
                    "item:delete"
                ))
                .sign(algorithm);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(String role, List<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (role != null && !role.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        if (permissions != null) {
            authorities.addAll(permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        }
        log.debug("Built authorities for role {}: {}", role, authorities);
        return authorities;
    }

    public Authentication validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);
        log.debug("Token verified for subject: {}", decoded.getSubject());

        UserDto user = UserDto.builder()
                .login(decoded.getSubject())
                .firstName(decoded.getClaim("firstName").asString())
                .lastName(decoded.getClaim("lastName").asString())
                .role(decoded.getClaim("role").asString())
                .permissions(decoded.getClaim("permissions").asList(String.class))
                .build();

        List<SimpleGrantedAuthority> authorities = buildAuthorities(user.getRole(), user.getPermissions());
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    public Authentication validateTokenStrongly(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);
        log.debug("Token strongly verified for subject: {}", decoded.getSubject());

        UserDto user = userService.findByLogin(decoded.getSubject());
        
        // Add default permissions for OAuth2 users
        List<String> permissions = new ArrayList<>(List.of(
            // OAuth2 scopes
            "SCOPE_openid", 
            "SCOPE_profile", 
            "SCOPE_email", 
            "SCOPE_User.Read",
            // Item permissions
            "item:read",
            "item:write",
            "item:update",
            "item:delete"
        ));

        // Add any existing permissions
        if (user.getPermissions() != null) {
            permissions.addAll(user.getPermissions());
        }

        user.setPermissions(permissions);
        // Set the token in the UserDto
        user.setToken(token);
        
        List<SimpleGrantedAuthority> authorities = buildAuthorities(user.getRole(), user.getPermissions());
        log.debug("Built authorities for user {}: {}", user.getLogin(), authorities);
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
    
}

