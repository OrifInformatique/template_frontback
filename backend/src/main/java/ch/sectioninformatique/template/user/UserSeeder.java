package ch.sectioninformatique.template.user;

import java.util.HashSet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.auth.CredentialsDto;
import ch.sectioninformatique.template.security.MainRoleEnum;

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

	private final AuthClient authClient;

	/**
	 * Constructs a new UserSeeder with the required dependencies.
	 *
	 * @param userRepository Repository for user data access
	 */
	public UserSeeder(UserRepository userRepository, AuthClient authClient, UserMapper userMappeer) {
		this.userRepository = userRepository;
		this.authClient = authClient;
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
	 */
	private void loadUserData() {
		if (this.userRepository.count() == 0) {
			UserDto adminUserDto = authClient.login(new CredentialsDto(
				"super.admin@test.com",
				 "ReallySecure123@PassWordBecauseIWantToBeSuperSafe".toCharArray()
				)
			).block().getBody();

			User adminUser = User.builder()
				.id(adminUserDto.getId())
				.firstName(adminUserDto.getFirstName())
				.lastName(adminUserDto.getLastName())
				.login(adminUserDto.getLogin())
				.mainRole(MainRoleEnum.ADMIN)
				.appSpecificRoles(new HashSet<>())
				.build();

			userRepository.save(adminUser);

			UserDto userDto = authClient.login(new CredentialsDto(
				"john.doe@test.com",
				"Secure123@Pass".toCharArray()
			)).block().getBody();

			User user = User.builder()
				.id(userDto.getId())
				.firstName(userDto.getFirstName())
				.lastName(userDto.getLastName())
				.login(userDto.getLogin())
				.mainRole(MainRoleEnum.USER)
				.appSpecificRoles(new HashSet<>())
				.build();

			userRepository.save(user);

		} else {
			System.out.println("Users table not empty - Skipping user seeding");
		}
	}
}