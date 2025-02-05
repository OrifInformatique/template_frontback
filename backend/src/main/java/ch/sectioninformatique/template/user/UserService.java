package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.auth.RegisterUserDto;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }
    
    public User createAdministrator(RegisterUserDto input) {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (optionalRole.isEmpty()) return null;
        var user = new UserBuilder()
                .setFirstName(input.getFirstName())
                .setEmail(input.getEmail())
                .setPassword(passwordEncoder.encode(input.getPassword()))
                .addRole(optionalRole.get())
                .build();

        return userRepository.save(user);
    }

    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
        // Vérifier que l'utilisateur n'est pas déjà admin
        if (user.getRole().getName().equals(RoleEnum.ADMIN)) {
            throw new RuntimeException("L'utilisateur est déjà admin");
        }
        
        // Récupérer le rôle ADMIN
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
            .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé"));
            
        // Mettre à jour le rôle
        user.getRoles().clear();
        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    public void revokeAdminRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (user.getRole().getName().equals(RoleEnum.USER)) {
            throw new RuntimeException("L'utilisateur est déjà un utilisateur");
        }

        Role userRole = roleRepository.findByName(RoleEnum.USER)
            .orElseThrow(() -> new RuntimeException("Rôle USER non trouvé"));

        user.getRoles().clear();
        user.getRoles().add(userRole);
        userRepository.save(user);
    }
}