/* package ch.sectioninformatique.template.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ch.sectioninformatique.template.user.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private User mockUser1;

    private Item testItem;
    private Item updatedItem;

    @BeforeEach
    void setUp() {
        // Setup mock user
        when(mockUser1.getUsername()).thenReturn("user1");

        testItem = new Item();
        testItem.setId(1);  // Changed from 1L to 1
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setAuthor(mockUser1);

        updatedItem = new Item();
        updatedItem.setId(1);  // Changed from 1L to 1
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("Updated Description");
        updatedItem.setAuthor(mockUser1);
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"item:update"})
    void updateItem_AsAuthorizedUser_ShouldSucceed() throws Exception {
        when(itemService.updateItem(eq(1L), any(Item.class))).thenReturn(updatedItem);

        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @WithMockUser(username = "user2", authorities = {"item:update"})
    void updateItem_AsUnauthorizedUser_ShouldFail() throws Exception {
        when(itemService.updateItem(eq(1L), any(Item.class)))
                .thenThrow(new UnauthorizedItemUpdateException("You can only update your own items"));

        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"}, authorities = {"item:update"})
    void updateItem_AsAdmin_ShouldSucceed() throws Exception {
        when(itemService.updateItem(eq(1L), any(Item.class))).thenReturn(updatedItem);

        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    @WithMockUser(username = "user1")
    void updateItem_WithoutProperAuthority_ShouldFail() throws Exception {
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"item:update"})
    void updateItem_ItemNotFound_ShouldReturn404() throws Exception {
        when(itemService.updateItem(eq(1L), any(Item.class)))
                .thenThrow(new ItemNotFoundException(1L));

        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isNotFound());
    }
}  */