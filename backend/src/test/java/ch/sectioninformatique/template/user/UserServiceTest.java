package ch.sectioninformatique.template.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import ch.sectioninformatique.template.security.LocalRoleEnum;
import ch.sectioninformatique.template.security.MainRoleEnum;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.security.SpringAuthPermissions;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserExceptions.DuplicateUserException;

/**
 * Tests the resolution of the local user matching a user authenticated by
 * spring-auth ({@link UserService#getOrCreateAuthenticatedUser}): the local user
 * is found by its spring-auth id, never by its login.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserServiceTest {

    // Ids far from the seeded users, rolled back after each test
    private static final long MANAGER_ID = 900_001L;
    private static final long OTHER_ID = 900_002L;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Autowired
    private MockMvc mockMvc;

    private User saveLocalUser(long id, String login, boolean localManager) {
        Set<Role> roles = new HashSet<>();
        if (localManager) {
            roles.add(roleRepository.findByName(LocalRoleEnum.LOCAL_MANAGER).orElseThrow());
        }
        return userRepository.save(User.builder()
                .id(id)
                .firstName("Local")
                .lastName("User")
                .login(login)
                .mainRole(MainRoleEnum.USER)
                .appSpecificRoles(roles)
                .build());
    }

    private UserDto authenticatedUser(long id, String login) {
        return UserDto.builder()
                .id(id)
                .firstName("Auth")
                .lastName("User")
                .login(login)
                .mainRole(MainRoleEnum.USER.name())
                .build();
    }

    /**
     * A user whose login is part of another user's login must not get the
     * local user (and local roles) of that other user.
     */
    @Test
    public void getOrCreateAuthenticatedUser_loginContainedInAnotherLogin_shouldNotMatchOtherUser() {
        saveLocalUser(MANAGER_ID, "unit.john.doe@test.com", true);

        User resolved = userService.getOrCreateAuthenticatedUser(
                authenticatedUser(OTHER_ID, "doe@test.com"), OTHER_ID);

        assertEquals(OTHER_ID, resolved.getId());
        assertEquals("doe@test.com", resolved.getLogin());
        assertTrue(resolved.getAppSpecificRoles().isEmpty(), "New user must not inherit local roles");
        assertEquals("unit.john.doe@test.com", userRepository.findById(MANAGER_ID).orElseThrow().getLogin());
    }

    /**
     * The local user is found by id, and its login and names follow spring-auth.
     */
    @Test
    public void getOrCreateAuthenticatedUser_loginChangedInSpringAuth_shouldSyncLocalUser() {
        saveLocalUser(MANAGER_ID, "old.login@test.com", true);

        User resolved = userService.getOrCreateAuthenticatedUser(
                authenticatedUser(MANAGER_ID, "new.login@test.com"), MANAGER_ID);

        assertEquals(MANAGER_ID, resolved.getId());
        assertEquals("new.login@test.com", resolved.getLogin());
        assertEquals("Auth", resolved.getFirstName());
        assertEquals(1, resolved.getAppSpecificRoles().size(), "Local roles are kept");
    }

    /**
     * A local soft delete only hides the user from the lists: the user is
     * resolved (neither refused nor re-created) and stays soft-deleted.
     */
    @Test
    public void getOrCreateAuthenticatedUser_softDeletedLocally_shouldReturnUserStillDeleted() {
        User user = saveLocalUser(MANAGER_ID, "deleted.user@test.com", false);
        user.setDeleted(true);
        userRepository.save(user);

        User resolved = userService.getOrCreateAuthenticatedUser(
                authenticatedUser(MANAGER_ID, "deleted.user@test.com"), MANAGER_ID);

        assertEquals(MANAGER_ID, resolved.getId());
        assertTrue(userRepository.findById(MANAGER_ID).orElseThrow().isDeleted(), "User stays soft-deleted");
    }

    /**
     * A user without last name (e.g. an Azure account without family_name) is
     * created locally on its first login.
     */
    @Test
    public void getOrCreateAuthenticatedUser_newUserWithoutLastName_shouldCreateUser() {
        UserDto azureUser = authenticatedUser(OTHER_ID, "azure.user@test.com");
        azureUser.setLastName(null);

        User resolved = userService.getOrCreateAuthenticatedUser(azureUser, OTHER_ID);

        assertEquals(OTHER_ID, resolved.getId());
        assertEquals(null, userRepository.findById(OTHER_ID).orElseThrow().getLastName());
    }

    /**
     * A new user whose login is already used by another local user is refused
     * rather than merged with it.
     */
    @Test
    public void getOrCreateAuthenticatedUser_newIdWithExistingLogin_shouldThrowDuplicate() {
        saveLocalUser(MANAGER_ID, "same.login@test.com", true);

        assertThrows(DuplicateUserException.class, () -> userService.getOrCreateAuthenticatedUser(
                authenticatedUser(OTHER_ID, "same.login@test.com"), OTHER_ID));
    }

    /**
     * A user soft-deleted locally can still use the application.
     */
    @Test
    public void request_withTokenOfUserSoftDeletedLocally_shouldReturn200() throws Exception {
        User user = saveLocalUser(MANAGER_ID, "deleted.user@test.com", false);
        user.setDeleted(true);
        userRepository.save(user);

        UserDto tokenUser = authenticatedUser(MANAGER_ID, "deleted.user@test.com");
        String token = userAuthenticationProvider.createToken(SpringAuthPermissions.grant(tokenUser));

        mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }
}
