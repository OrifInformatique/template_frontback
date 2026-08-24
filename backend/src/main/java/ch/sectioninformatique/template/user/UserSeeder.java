package ch.sectioninformatique.template.user;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeder class for initializing the database with default user data.
 * This class implements CommandLineRunner to execute the seeding process
 * when the application starts. It creates a set of predefined users with
 * different roles (USER, MANAGER, ADMIN) for testing and development purposes.
 * The seeder runs after the RoleSeeder (Order(2)) to ensure roles exist before
 * creating users.
 */
@Component
@Order(2)
@Profile({ "dev" })
public class UserSeeder implements CommandLineRunner {

	/** Repository for user data access */
	private final UserRepository userRepository;

	/**
	 * Constructs a new UserSeeder with the required dependencies.
	 *
	 * @param userRepository Repository for user data access
	 * @param roleRepository Repository for role data access
	 */
	public UserSeeder(UserRepository userRepository) {
		this.userRepository = userRepository;

	}

	/**
	 * Executes the seeding process when the application starts.
	 * This method is called by Spring Boot after the application context is loaded.
	 * It:
	 * 1. Prints a start message
	 * 2. Calls loadUserData() to create default users
	 * 3. Prints a completion message
	 *
	 * @param args Command line arguments passed to the application
	 * @throws Exception if an error occurs during the seeding process
	 */
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Starting User Seeding...");
		loadUserData();
		System.out.println("User Seeding completed.");
	}

	/**
	 * Loads initial user data into the database.
	 * Creates a set of predefined users with different roles if the database is
	 * empty.
	 * The users include:
	 * - A deleted user (ID 1)
	 * - Regular users with USER role (John Doe, Alice Johnson, Dan Sergeant, etc.)
	 * - An admin user with MANAGER role (Jane Smith)
	 * - A super admin user with ADMIN role (Super Admin)
	 * 
	 * Each user is created with:
	 * - Unique login (email format)
	 * - First and last name
	 * - Appropriate role(s)
	 *
	 * @throws RuntimeException if any required role (USER, MANAGER, ADMIN) is not
	 *                          found in the database
	 */
	private void loadUserData() {
		if (this.userRepository.count() == 0) {

			// Create users with User.builder()
			User user0 = User.builder()
					.login("deleted.user@test.com")
					.build();

			User user1 = User.builder()
					.login("john.doe@test.com")
					.build();

			User user2 = User.builder()
					.login("jane.smith@test.com")
					.build();

			User user3 = User.builder()
					.login("alice.johnson@test.com")
					.build();

			User user4 = User.builder()
					.login("dan.sergeant@test.com")
					.build();

			User user5 = User.builder()
					.login("bobby.balloonzi@test.com")
					.build();

			User user6 = User.builder()
					.login("rob.jake@test.com")
					.build();

			User user7 = User.builder()
					.login("super.admin@test.com")
					.build();

			User user8 = User.builder()
					.login("not.an.ia@vedal.ia")
					.build();

			userRepository.saveAll(Arrays.asList(user0, user1, user2, user3, user4, user5, user6, user7, user8));
		} else {
			System.out.println("Users table not empty - Skipping user seeding");
		}
	}
}