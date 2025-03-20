package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * UserSeeder class is used to seed the database with users.
 */
@Component
@Order(2)
public class UserSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;

	/**
	 * Constructor for UserSeeder.
	 * 
	 * @param userRepository  The user repository
	 * @param passwordEncoder The password encoder
	 * @param roleRepository  The role repository
	 */
	public UserSeeder(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
	}

	/**
	 * Run the UserSeeder.
	 * 
	 * @param args The command line arguments
	 * @throws Exception If an error occurs
	 */
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Starting User Seeding...");
		loadUserData();
		System.out.println("User Seeding completed.");
	}

	/**
	 * Load user data into the database.
	 */
	private void loadUserData() {
		if (this.userRepository.count() == 0) {
			Role userRole = roleRepository.findByName(RoleEnum.USER)
					.orElseThrow(() -> new RuntimeException("Role USER not found"));
			Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
					.orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
			Role superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN)
					.orElseThrow(() -> new RuntimeException("Role SUPER_ADMIN not found"));

			// Create users with User.builder()
			User user0 = User.builder()
					.firstName("deleted")
					.lastName("user")
					.login("deleted.user@test.com")
					.password(passwordEncoder.encode("NoN33dPassword@nymore!"))
					.roles(Set.of(userRole))
					.build();

			User user1 = User.builder()
					.firstName("John")
					.lastName("DOE")
					.login("john.doe@test.com")
					.password(passwordEncoder.encode("Secure123@Pass"))
					.roles(Set.of(userRole))
					.build();

			User user2 = User.builder()
					.firstName("Jane")
					.lastName("SMITH")
					.login("jane.smith@test.com")
					.password(passwordEncoder.encode("Complex#789Pwd"))
					.roles(Set.of(adminRole))
					.build();

			User user3 = User.builder()
					.firstName("Alice")
					.lastName("JOHNSON")
					.login("alice.johnson@test.com")
					.password(passwordEncoder.encode("Test$4321Now"))
					.roles(Set.of(userRole))
					.build();

			User user4 = User.builder()
					.firstName("Dan")
					.lastName("SERGEANT")
					.login("dan.sergeant@test.com")
					.password(passwordEncoder.encode("Spring2024@Dev"))
					.roles(Set.of(userRole))
					.build();

			User user5 = User.builder()
					.firstName("Bobby")
					.lastName("BALLOONZI")
					.login("bobby.balloonzi@test.com")
					.password(passwordEncoder.encode("P@ssw0rd2024"))
					.roles(Set.of(userRole))
					.build();

			User user6 = User.builder()
					.firstName("Rob")
					.lastName("JAKE")
					.login("rob.jake@test.com")
					.password(passwordEncoder.encode("Inf0#Security24"))
					.roles(Set.of(userRole))
					.build();

			User user7 = User.builder()
					.firstName("Super")
					.lastName("Admin")
					.login("super.admin@test.com")
					.password(passwordEncoder.encode("ReallySecure123@PassWordBecauseIWantToBeSuperSafe"))
					.roles(Set.of(superAdminRole))
					.build();

			userRepository.saveAll(Arrays.asList(user0, user1, user2, user3, user4, user5, user6, user7));
		} else {
			System.out.println("Users table not empty - Skipping user seeding");
		}
	}
}