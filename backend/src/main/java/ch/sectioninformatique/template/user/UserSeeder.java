package ch.sectioninformatique.template.user;
    
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {
        
    @Autowired
    UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        loadUserData();
    }

    private void loadUserData() {
        if (userRepository.count() == 0) {
            User user1 = new User("john DOE", "john.doe@test.com", "xswqay");
            User user2 = new User("jane SMITH", "jane.smith@test.com", "qwertz");
            User user3 = new User("alice JOHNSON", "alice.johnson@test.com", "yaqwsx");

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
        }
    }
}