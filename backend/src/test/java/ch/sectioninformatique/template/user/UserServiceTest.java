package ch.sectioninformatique.template.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.security.LocalRoleEnum;
import ch.sectioninformatique.template.security.MainRoleEnum;
import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.security.SpringAuthPermissions;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.user.UserExceptions.DuplicateUserException;
import ch.sectioninformatique.template.user.UserExceptions.RoleNotFoundException;
import reactor.core.publisher.Mono;

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

    @MockitoBean
    private AuthClient authClient;

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

    // ==================== updateUser ====================

    private static final String TOKEN = "Bearer token";

    /** User as returned by spring-auth after an update */
    private UserDto globalUser(long id, String login, MainRoleEnum mainRole) {
        return UserDto.builder()
                .id(id)
                .firstName("New")
                .lastName("Name")
                .login(login)
                .mainRole(mainRole.name())
                .build();
    }

    /**
     * A main role different from the global one is changed in spring-auth, and
     * the resulting user is mirrored locally with its new local roles.
     */
    @Test
    public void updateUser_withNewMainRole_shouldChangeRoleInSpringAuthAndLocally() {
        saveLocalUser(MANAGER_ID, "update.user@test.com", false);
        when(authClient.updateUser(eq(TOKEN), eq(MANAGER_ID), anyMap()))
                .thenReturn(Mono.just(ResponseEntity.ok(globalUser(MANAGER_ID, "renamed@test.com", MainRoleEnum.USER))));
        when(authClient.changeMainRole(TOKEN, MANAGER_ID, MainRoleEnum.MANAGER))
                .thenReturn(Mono.just(ResponseEntity.ok(globalUser(MANAGER_ID, "renamed@test.com", MainRoleEnum.MANAGER))));

        userService.updateUser(MANAGER_ID, new UserUpdateDto("New", "Name", "renamed@test.com",
                "MANAGER", List.of(LocalRoleEnum.LOCAL_MANAGER.name())), TOKEN);

        verify(authClient).changeMainRole(TOKEN, MANAGER_ID, MainRoleEnum.MANAGER);
        User updated = userRepository.findById(MANAGER_ID).orElseThrow();
        assertEquals(MainRoleEnum.MANAGER, updated.getMainRole());
        assertEquals("renamed@test.com", updated.getLogin());
        assertEquals("New", updated.getFirstName());
        assertEquals(List.of(LocalRoleEnum.LOCAL_MANAGER.name()), updated.getAppSpecificRolesString());
    }

    /**
     * The main role is not changed in spring-auth when it is the current one
     * (spring-auth would refuse it with 409), nor when it is not given.
     */
    @Test
    public void updateUser_withSameOrNoMainRole_shouldNotChangeRole() {
        saveLocalUser(MANAGER_ID, "update.user@test.com", true);
        when(authClient.updateUser(eq(TOKEN), eq(MANAGER_ID), anyMap()))
                .thenReturn(Mono.just(ResponseEntity.ok(globalUser(MANAGER_ID, "update.user@test.com", MainRoleEnum.USER))));

        userService.updateUser(MANAGER_ID, new UserUpdateDto("New", "Name", "update.user@test.com",
                "USER", null), TOKEN);
        userService.updateUser(MANAGER_ID, new UserUpdateDto("New", "Name", "update.user@test.com",
                null, null), TOKEN);

        verify(authClient, never()).changeMainRole(anyString(), anyLong(), any());
        User updated = userRepository.findById(MANAGER_ID).orElseThrow();
        assertEquals(MainRoleEnum.USER, updated.getMainRole());
        assertEquals(1, updated.getAppSpecificRoles().size(), "Local roles not given: kept");
    }

    /**
     * An unknown role fails before anything is sent to spring-auth.
     */
    @Test
    public void updateUser_withUnknownRole_shouldThrowBeforeCallingSpringAuth() {
        saveLocalUser(MANAGER_ID, "update.user@test.com", false);

        assertThrows(RoleNotFoundException.class, () -> userService.updateUser(MANAGER_ID,
                new UserUpdateDto("New", "Name", "update.user@test.com", "BOSS", null), TOKEN));
        assertThrows(RoleNotFoundException.class, () -> userService.updateUser(MANAGER_ID,
                new UserUpdateDto("New", "Name", "update.user@test.com", null, List.of("BOSS")), TOKEN));

        verifyNoInteractions(authClient);
    }

    /**
     * The body sent by the frontend user form (no "deleted" or "id" field) is
     * accepted.
     */
    @Test
    public void putUser_withFrontendFormBody_shouldReturn200() throws Exception {
        saveLocalUser(MANAGER_ID, "update.user@test.com", false);
        when(authClient.updateUser(anyString(), eq(MANAGER_ID), anyMap()))
                .thenReturn(Mono.just(ResponseEntity.ok(globalUser(MANAGER_ID, "update.user@test.com", MainRoleEnum.USER))));
        UserDto admin = userService.findByLogin("test.admin@test.com");
        String token = userAuthenticationProvider.createToken(SpringAuthPermissions.grant(admin));

        mockMvc.perform(put("/users/" + MANAGER_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"New","lastName":"Name","login":"update.user@test.com",
                                 "mainRole":"USER","appSpecificRoles":[]}
                                """))
                .andExpect(status().isOk());
    }
}
