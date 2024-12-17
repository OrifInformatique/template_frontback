package ch.sectioninformatique.template.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import ch.sectioninformatique.template.user.LoginUserDto;
import ch.sectioninformatique.template.user.RegisterUserDto;
import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
        User user = new User(input.getFirstName(),
                             input.getLastName(),
                             input.getEmail(),
                             passwordEncoder.encode(input.getPassword()));

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