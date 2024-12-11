package ch.sectioninformatique.template.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {
        
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        loadUserData();
    }

    private void loadUserData() {
        if (userRepository.count() == 0) {
            User user1 = new User();
            user1.setFullName("john DOE");
            user1.setEmail("john.doe@test.com");
            user1.setPassword(passwordEncoder.encode("xswqay"));

            User user2 = new User();
            user2.setFullName("jane SMITH");
            user2.setEmail("jane.smith@test.com");
            user2.setPassword(passwordEncoder.encode("qwertz"));

            User user3 = new User();
            user3.setFullName("alice JOHNSON");
            user3.setEmail("alice.johnson@test.com");
            user3.setPassword(passwordEncoder.encode("yaqwsx"));

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
        }
    }
}