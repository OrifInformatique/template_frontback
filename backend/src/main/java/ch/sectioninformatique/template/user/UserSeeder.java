package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.Optional;

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
			Optional<Role> optionalRole = this.roleRepository.findByName(RoleEnum.USER);

			if (optionalRole.isEmpty()) {
				System.out.println("Role USER not found - Skipping user seeding");
				return;
			}

			/**
			 * Get the user role, admin role and super admin role.
			 */
			Role userRole = optionalRole.get();
			Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
					.orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
			Role superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN)
					.orElseThrow(() -> new RuntimeException("Role SUPER_ADMIN not found"));

			/**
			 * Create different profiles (users).
			 */
			User user0 = new UserBuilder()
					.setFirstName("deleted")
					.setLastName("user")
					.setLogin("deleted.user@test.com")
					.setPassword(this.passwordEncoder.encode("NoN33dPassword@nymore!"))
					.addRole(userRole)
					.build();

			User user1 = new UserBuilder()
					.setFirstName("John")
					.setLastName("DOE")
					.setLogin("john.doe@test.com")
					.setPassword(this.passwordEncoder.encode("Secure123@Pass"))
					.addRole(userRole)
					.build();

			User user2 = new UserBuilder()
					.setFirstName("Jane")
					.setLastName("SMITH")
					.setLogin("jane.smith@test.com")
					.setPassword(this.passwordEncoder
							.encode("Complex#789Pwd"))
					.addRole(adminRole)
					.build();

			User user3 = new UserBuilder()
					.setFirstName("Alice")
					.setLastName("JOHNSON")
					.setLogin("alice.johnson@test.com")
					.setPassword(this.passwordEncoder
							.encode("Test$4321Now"))
					.addRole(userRole)
					.build();

			User user4 = new UserBuilder()
					.setFirstName("Dan")
					.setLastName("SERGEANT")
					.setLogin("dan.sergeant@test.com")
					.setPassword(this.passwordEncoder
							.encode("Spring2024@Dev"))
					.addRole(userRole)
					.build();

			User user5 = new UserBuilder()
					.setFirstName("Bobby")
					.setLastName("BALLOONZI")
					.setLogin("bobby.balloonzi@test.com")
					.setPassword(this.passwordEncoder
							.encode("P@ssw0rd2024"))
					.addRole(userRole)
					.build();

			User user6 = new UserBuilder()
					.setFirstName("Rob")
					.setLastName("JAKE")
					.setLogin("rob.jake@test.com")
					.setPassword(this.passwordEncoder
							.encode("Inf0#Security24"))
					.addRole(userRole)
					.build();

			User user7 = new UserBuilder()
					.setFirstName("Super")
					.setLastName("Admin")
					.setLogin("super.admin@test.com")
					.setPassword(this.passwordEncoder.encode("ReallySecure123@PassWordBecauseIWantToBeSuperSafe"))
					.addRole(superAdminRole)
					.build();

			this.userRepository.save(user0);
			this.userRepository.save(user1);
			this.userRepository.save(user2);
			this.userRepository.save(user3);
			this.userRepository.save(user4);
			this.userRepository.save(user5);
			this.userRepository.save(user6);
			this.userRepository.save(user7);
		} else {
			System.out.println("Users table not empty - Skipping user seeding");
		}
	}
}