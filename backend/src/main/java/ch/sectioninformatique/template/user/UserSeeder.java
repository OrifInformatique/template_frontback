package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.Optional;

@Component
@Order(2)
public class UserSeeder implements CommandLineRunner {
        
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserSeeder(UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        RoleRepository roleRepository)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting User Seeding...");
        loadUserData();
        System.out.println("User Seeding completed.");
    }

    private void loadUserData() {
        if (this.userRepository.count() == 0) {
            Optional<Role> optionalRole = this.roleRepository.findByName(RoleEnum.USER);
            
            if (optionalRole.isEmpty()) {
                System.out.println("Role USER not found - Skipping user seeding");
                return;
            }

            Role userRole = optionalRole.get();
            
            User user1 = new UserBuilder()
                    .setFirstName("John")
                    .setLastName("DOE")
                    .setEmail("john.doe@test.com")
                    .setPassword(this.passwordEncoder.encode("Secure123@Pass"))
                    .addRole(userRole)
                    .build();
            
            User user2 = new UserBuilder()
                    .setFirstName("Jane")
                    .setLastName("SMITH")
                    .setEmail("jane.smith@test.com")
                    .setPassword(this.passwordEncoder
                            .encode("Complex#789Pwd"))
                    .addRole(userRole)
                    .build();

            User user3 = new UserBuilder()
                    .setFirstName("Alice")
                    .setLastName("JOHNSON")
                    .setEmail("alice.johnson@test.com")
                    .setPassword(this.passwordEncoder
                            .encode("Test$4321Now"))
                    .addRole(userRole)
                    .build();

            User user4 = new UserBuilder()
                    .setFirstName("Dan")
                    .setLastName("SERGEANT")
                    .setEmail("dan.sergeant@test.com")
                    .setPassword(this.passwordEncoder
                            .encode("Spring2024@Dev"))
                    .addRole(userRole)
                    .build();

            User user5 = new UserBuilder()
                    .setFirstName("Bobby")
                    .setLastName("BALLOONZI")
                    .setEmail("bobby.balloonzi@test.com")
                    .setPassword(this.passwordEncoder
                            .encode("P@ssw0rd2024"))
                    .addRole(userRole)
                    .build();
                
            User user6 = new UserBuilder()
                    .setFirstName("Rob")
                    .setLastName("JAKE")
                    .setEmail("rob.jake@test.com")
                    .setPassword(this.passwordEncoder
                            .encode("Inf0#Security24"))
                    .addRole(userRole)
                    .build();

            this.userRepository.save(user1);
            this.userRepository.save(user2);
            this.userRepository.save(user3);
            this.userRepository.save(user4);
            this.userRepository.save(user5);
            this.userRepository.save(user6);
        } else {
            System.out.println("Users table not empty - Skipping user seeding");
        }
    }
}