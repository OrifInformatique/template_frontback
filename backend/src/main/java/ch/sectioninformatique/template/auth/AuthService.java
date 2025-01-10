package ch.sectioninformatique.template.auth;

import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.user.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserRepository;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final RoleRepository roleRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    private final Validator validator;

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository,
            Validator validator
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.validator = validator;
    }

    public User signup(@Valid RegisterUserDto registerUserDto) {
        var violations = validator.validate(registerUserDto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Validation failed");
        }
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return null;
        }
        User user = new UserBuilder()
                .setFirstName(registerUserDto.getFirstName())
                .setLastName(registerUserDto.getLastName())
                .setEmail(registerUserDto.getEmail())
                .setPassword(passwordEncoder.encode(registerUserDto.getPassword()))
                .addRole(optionalRole.get())
                .build();

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            return (User) authentication.getPrincipal();
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email/password combination");
        }
    }
}