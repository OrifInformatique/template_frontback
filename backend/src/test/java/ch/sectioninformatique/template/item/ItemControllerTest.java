package ch.sectioninformatique.template.item;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ch.sectioninformatique.template.RestDocsSensitiveDataMasking.maskSensitiveData;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import ch.sectioninformatique.template.security.SpringAuthPermissions;
import ch.sectioninformatique.template.AuthApplication;
import ch.sectioninformatique.template.RestDocsSnippets;
import ch.sectioninformatique.template.security.UserAuthenticationProvider;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.security.LocalRoleEnum;
import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserRepository;
import ch.sectioninformatique.template.user.UserService;

/**
 * Integration tests for {@link ItemController} REST endpoints.
 * Snippets generated here feed the REST Docs section for item endpoints.
 */
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class ItemControllerTest {

    private static final String USER_LOGIN = "test.user@test.com";
    private static final String MANAGER_LOGIN = "test.manager@test.com";
    private static final String ADMIN_LOGIN = "test.admin@test.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private String tokenFor(String login) {
        UserDto userDto = userService.findByLogin(login);
        if (userDto == null) {
            throw new IllegalStateException("User " + login + " not found. Ensure TestUserSeeder has run.");
        }
        return userAuthenticationProvider.createToken(SpringAuthPermissions.grant(userDto));
    }

    private User userEntity(String login) {
        return userRepository.findByLogin(login).orElseThrow();
    }

    /** Creates a stable item for REST Docs examples. */
    private Item saveDocItem(User author, String name, String description) {
        return itemRepository.save(new Item(name, description, author));
    }

    private void clearItems() {
        itemRepository.deleteAllPermanently();
    }

    private String itemJson(String name, String description) throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of("name", name, "description", description));
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private void performRequest(
            String method,
            String endpoint,
            String body,
            String token,
            int expectedStatus,
            String docsFileName,
            Consumer<ResultActions> script,
            Snippet... snippets) throws Exception {

        var builder = switch (method) {
            case "GET" -> get(endpoint);
            case "POST" -> post(endpoint);
            case "PUT" -> put(endpoint);
            case "DELETE" -> delete(endpoint);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        if (body != null) {
            builder.content(body);
        }
        if (token != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        var request = mockMvc.perform(builder.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus));

        if (script != null) {
            script.accept(request);
        }

        request.andDo(document("items/" + docsFileName,
                preprocessRequest(maskSensitiveData(), prettyPrint()),
                preprocessResponse(maskSensitiveData(), prettyPrint()),
                snippets));
    }

    @Test
    @Transactional
    public void getItems_active_returnsActiveItems() throws Exception {
        clearItems();
        saveDocItem(userEntity(USER_LOGIN), "Doc item", "For REST Docs");

        performRequest(
                "GET",
                "/items",
                null,
                tokenFor(USER_LOGIN),
                200,
                "list-active",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.length()").value(1));
                        response.andExpect(jsonPath("$[0].name").value("Doc item"));
                        response.andExpect(jsonPath("$[0].deleted").value(false));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemsListResponse());
    }

    @Test
    @Transactional
    public void getItems_deleted_returnsSoftDeletedItems() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(MANAGER_LOGIN), "Deleted item", "Soft deleted for REST Docs");
        itemRepository.deleteById(item.getId());

        performRequest(
                "GET",
                "/items?state=deleted",
                null,
                tokenFor(MANAGER_LOGIN),
                200,
                "list-deleted",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.length()").value(1));
                        response.andExpect(jsonPath("$[0].name").value("Deleted item"));
                        response.andExpect(jsonPath("$[0].deleted").value(true));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemsListResponse());
    }

    @Test
    @Transactional
    public void getItems_all_returnsActiveAndDeletedItems() throws Exception {
        clearItems();
        saveDocItem(userEntity(USER_LOGIN), "Active item", "Still visible");
        Item deleted = saveDocItem(userEntity(USER_LOGIN), "Archived item", "Soft deleted");
        itemRepository.deleteById(deleted.getId());

        performRequest(
                "GET",
                "/items?state=all",
                null,
                tokenFor(ADMIN_LOGIN),
                200,
                "list-all",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.length()").value(2));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemsListResponse());
    }

    @Test
    @Transactional
    public void getItems_missingToken_returns401() throws Exception {
        performRequest(
                "GET",
                "/items",
                null,
                null,
                401,
                "list/401/missing-token",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message")
                                .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Transactional
    public void getItemById_found_returnsItem() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "Doc item", "For REST Docs");

        performRequest(
                "GET",
                "/items/" + item.getId(),
                null,
                tokenFor(USER_LOGIN),
                200,
                "get-by-id",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.id").value(item.getId()));
                        response.andExpect(jsonPath("$.name").value("Doc item"));
                        response.andExpect(jsonPath("$.author.login").value(USER_LOGIN));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemDetailResponse());
    }

    @Test
    @Transactional
    public void getItemById_notFound_returns404() throws Exception {
        performRequest(
                "GET",
                "/items/999999",
                null,
                tokenFor(USER_LOGIN),
                404,
                "get-by-id/404/not-found",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("item.notFound")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Transactional
    public void getItemById_missingToken_returns401() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "Doc item", "For REST Docs");

        performRequest(
                "GET",
                "/items/" + item.getId(),
                null,
                null,
                401,
                "get-by-id/401/missing-token",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message")
                                .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Transactional
    public void createItem_withWritePermission_returnsCreatedItem() throws Exception {
        clearItems();

        performRequest(
                "POST",
                "/items/",
                itemJson("New item", "Created via REST Docs test"),
                tokenFor(MANAGER_LOGIN),
                200,
                "create",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.name").value("New item"));
                        response.andExpect(jsonPath("$.author.login").value(MANAGER_LOGIN));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest(),
                RestDocsSnippets.itemDetailResponse());
    }

    @Test
    @Transactional
    public void createItem_withoutWritePermission_returns403() throws Exception {
        performRequest(
                "POST",
                "/items/",
                itemJson("Forbidden item", "Should not be created"),
                tokenFor(USER_LOGIN),
                403,
                "create/403/forbidden",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("error.accessDenied")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Transactional
    public void createItem_missingToken_returns401() throws Exception {
        performRequest(
                "POST",
                "/items/",
                itemJson("Ghost item", "No authentication"),
                null,
                401,
                "create/401/missing-token",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message")
                                .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest());
    }

    @Test
    @Transactional
    public void updateItem_asAuthor_returnsUpdatedItem() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(MANAGER_LOGIN), "Original name", "Original description");

        performRequest(
                "PUT",
                "/items/" + item.getId(),
                itemJson("Updated name", "Updated description"),
                tokenFor(MANAGER_LOGIN),
                200,
                "update-own",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.name").value("Updated name"));
                        response.andExpect(jsonPath("$.description").value("Updated description"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest(),
                RestDocsSnippets.itemDetailResponse());
    }

    @Test
    @Transactional
    public void updateItem_asNonAuthor_returns403() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "User item", "Owned by test user");

        performRequest(
                "PUT",
                "/items/" + item.getId(),
                itemJson("Stolen update", "Not allowed"),
                tokenFor(MANAGER_LOGIN),
                403,
                "update/403/not-author",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("item.unauthorized")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest());
    }

    @Test
    @Transactional
    public void updateItem_withoutUpdatePermission_returns403() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(MANAGER_LOGIN), "Manager item", "USER cannot update");

        performRequest(
                "PUT",
                "/items/" + item.getId(),
                itemJson("Blocked update", "Not allowed"),
                tokenFor(USER_LOGIN),
                403,
                "update/403/forbidden",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("error.accessDenied")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest());
    }

    @Test
    @Transactional
    public void updateItem_notFound_returns404() throws Exception {
        performRequest(
                "PUT",
                "/items/999999",
                itemJson("Missing item", "Does not exist"),
                tokenFor(MANAGER_LOGIN),
                404,
                "update/404/not-found",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("item.notFound")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest());
    }

    @Test
    @Transactional
    public void updateItem_missingToken_returns401() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(MANAGER_LOGIN), "Protected item", "Needs auth");

        performRequest(
                "PUT",
                "/items/" + item.getId(),
                itemJson("Anonymous update", "Not allowed"),
                null,
                401,
                "update/401/missing-token",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message")
                                .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                RestDocsSnippets.itemCreateRequest());
    }

    @Test
    @Transactional
    public void deleteItem_asAdmin_softDeletesItem() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "To delete", "Soft delete via REST Docs");

        performRequest(
                "DELETE",
                "/items/" + item.getId(),
                null,
                tokenFor(ADMIN_LOGIN),
                200,
                "delete-soft",
                null);
    }

    @Test
    @Transactional
    public void deleteItem_asAdmin_permanentlyDeletesItem() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "Permanent delete", "Removed from database");
        Long itemId = item.getId();

        performRequest(
                "DELETE",
                "/items/" + itemId + "?softDelete=false",
                null,
                tokenFor(ADMIN_LOGIN),
                200,
                "delete-permanent",
                null);
    }

    @Test
    @Transactional
    public void deleteItem_notFound_returns404() throws Exception {
        performRequest(
                "DELETE",
                "/items/999999",
                null,
                tokenFor(ADMIN_LOGIN),
                404,
                "delete/404/not-found",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("item.notFound")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Transactional
    public void deleteItem_missingToken_returns401() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "Protected item", "Needs auth");

        performRequest(
                "DELETE",
                "/items/" + item.getId(),
                null,
                null,
                401,
                "delete/401/missing-token",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message")
                                .value(getMessage("security.auth.missingOrInvalidToken")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Transactional
    public void deleteItem_asManager_returns403() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(MANAGER_LOGIN), "Protected item", "Manager cannot delete");

        performRequest(
                "DELETE",
                "/items/" + item.getId(),
                null,
                tokenFor(MANAGER_LOGIN),
                403,
                "delete/403/forbidden",
                response -> {
                    try {
                        response.andExpect(jsonPath("$.message").value(getMessage("error.accessDenied")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * An admin updating someone else's item changes its content only: the
     * author stays the creator.
     */
    @Test
    @Transactional
    public void updateItem_asAdminOnOtherUsersItem_keepsAuthor() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "User item", "Owned by test user");

        mockMvc.perform(put("/items/" + item.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson("Edited by admin", "Moderated")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Edited by admin"))
                .andExpect(jsonPath("$.author.login").value(USER_LOGIN));

        // The stored author is still the creator
        assertEquals(USER_LOGIN, itemRepository.findById(item.getId()).orElseThrow().getAuthor().getLogin());
    }

    // ==================== Request body: client-settable fields only ====================

    /**
     * A create request carrying the id of an existing item creates a new item:
     * the existing item (of another user) is not overwritten.
     */
    @Test
    @Transactional
    public void createItem_withIdOfExistingItem_createsNewItemAndKeepsExistingOne() throws Exception {
        clearItems();
        Item existing = saveDocItem(userEntity(ADMIN_LOGIN), "Admin item", "Owned by admin");

        mockMvc.perform(post("/items/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(MANAGER_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + existing.getId() + ",\"name\":\"Hijacked\",\"description\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.not(existing.getId().intValue())))
                .andExpect(jsonPath("$.author.login").value(MANAGER_LOGIN));

        Item unchanged = itemRepository.findById(existing.getId()).orElseThrow();
        assertEquals("Admin item", unchanged.getName());
        assertEquals(ADMIN_LOGIN, unchanged.getAuthor().getLogin());
    }

    /**
     * The author and deletion state can't be set through an update request.
     */
    @Test
    @Transactional
    public void updateItem_withAuthorAndDeletedFields_ignoresThem() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(MANAGER_LOGIN), "Manager item", "Owned by manager");
        long adminId = userEntity(ADMIN_LOGIN).getId();

        mockMvc.perform(put("/items/" + item.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(MANAGER_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"description\":\"x\",\"deleted\":true,"
                                + "\"author\":{\"id\":" + adminId + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.author.login").value(MANAGER_LOGIN));
    }

    /**
     * An item without name is refused with 400 and the invalid field.
     */
    @Test
    @Transactional
    public void createItem_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/items/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(MANAGER_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== Ownership: permanent delete and restore ====================

    /** Gives test.user the LOCAL_MANAGER role (item:delete, not admin), rolled back with the test */
    private void grantLocalManagerToTestUser() {
        User user = userEntity(USER_LOGIN);
        user.getAppSpecificRoles().add(roleRepository.findByName(LocalRoleEnum.LOCAL_MANAGER).orElseThrow());
        userRepository.save(user);
    }

    /**
     * A non-admin allowed to delete items can't permanently delete another
     * user's item.
     */
    @Test
    @Transactional
    public void deleteItemPermanently_asNonAuthor_returns403AndKeepsItem() throws Exception {
        clearItems();
        grantLocalManagerToTestUser();
        Item item = saveDocItem(userEntity(ADMIN_LOGIN), "Admin item", "Owned by admin");

        mockMvc.perform(delete("/items/" + item.getId() + "?softDelete=false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(USER_LOGIN)))
                .andExpect(status().isForbidden());

        assertTrue(itemRepository.findById(item.getId()).isPresent(), "Item must not be deleted");
    }

    /**
     * The author can permanently delete their own item.
     */
    @Test
    @Transactional
    public void deleteItemPermanently_asAuthor_deletesItem() throws Exception {
        clearItems();
        grantLocalManagerToTestUser();
        Item item = saveDocItem(userEntity(USER_LOGIN), "User item", "Owned by test user");

        mockMvc.perform(delete("/items/" + item.getId() + "?softDelete=false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(USER_LOGIN)))
                .andExpect(status().isOk());

        assertTrue(itemRepository.findById(item.getId()).isEmpty(), "Item must be deleted");
    }

    /**
     * Permanently deleting an unknown item is answered 404.
     */
    @Test
    @Transactional
    public void deleteItemPermanently_unknownItem_returns404() throws Exception {
        mockMvc.perform(delete("/items/999999?softDelete=false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(ADMIN_LOGIN)))
                .andExpect(status().isNotFound());
    }

    /**
     * A non-admin can't restore another user's deleted item; an admin can.
     */
    @Test
    @Transactional
    public void restoreItem_asNonAuthor_returns403_asAdmin_restores() throws Exception {
        clearItems();
        Item item = saveDocItem(userEntity(USER_LOGIN), "User item", "Owned by test user");
        item.setDeleted(true);
        itemRepository.save(item);

        mockMvc.perform(put("/items/" + item.getId() + "/restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(MANAGER_LOGIN)))
                .andExpect(status().isForbidden());
        assertTrue(itemRepository.findById(item.getId()).orElseThrow().isDeleted(), "Item stays deleted");

        mockMvc.perform(put("/items/" + item.getId() + "/restore")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(ADMIN_LOGIN)))
                .andExpect(status().isOk());
        assertFalse(itemRepository.findById(item.getId()).orElseThrow().isDeleted(), "Item restored");
    }

    // ==================== Items without author ====================

    /**
     * An item whose author was permanently deleted (author set to null) does not
     * break the item list.
     */
    @Test
    @Transactional
    public void getItems_withItemWithoutAuthor_returns200() throws Exception {
        clearItems();
        saveDocItem(null, "Orphan item", "Author permanently deleted");

        mockMvc.perform(get("/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(USER_LOGIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Orphan item"))
                .andExpect(jsonPath("$[0].authorFirstName").doesNotExist());
    }

    /**
     * Nobody is the author of an item without author: a non-admin can't update
     * or delete it (403, not a 500).
     */
    @Test
    @Transactional
    public void updateAndDeleteItem_withoutAuthor_asNonAdmin_returns403() throws Exception {
        clearItems();
        Item item = saveDocItem(null, "Orphan item", "Author permanently deleted");
        String managerToken = tokenFor(MANAGER_LOGIN);

        mockMvc.perform(put("/items/" + item.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson("Taken over", "Not allowed")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/items/" + item.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }
}
