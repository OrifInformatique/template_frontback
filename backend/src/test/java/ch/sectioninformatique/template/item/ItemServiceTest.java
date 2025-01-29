/* package ch.sectioninformatique.template.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ch.sectioninformatique.template.user.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private User mockUser;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private Item newItemData;

    @BeforeEach
    void setUp() {
        // Setup test items
        testItem = new Item();
        testItem.setId(1);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        
        // Setup mock user as author
        when(mockUser.getUsername()).thenReturn("user2");
        testItem.setAuthor(mockUser);

        newItemData = new Item();
        newItemData.setName("Updated Item");
        newItemData.setDescription("Updated Description");

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

     @Test
    void updateItem_AsAdmin_ShouldSucceed() {
        // Setup admin role
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        when(authentication.getAuthorities())
            .thenReturn(Collections.singletonList(authority));
        when(itemRepository.findById((long) 1)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Execute update
        Item updatedItem = itemService.updateItem((long) 1, newItemData);

        // Verify
        assertNotNull(updatedItem);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_AsUserUpdatingOwnItem_ShouldSucceed() {
        // Setup user role and authentication
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());
        when(authentication.getName()).thenReturn("user2");
        when(itemRepository.findById((long) 1)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Execute update
        Item updatedItem = itemService.updateItem((long)1, newItemData);

        // Verify
        assertNotNull(updatedItem);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_AsUserUpdatingOthersItem_ShouldThrowException() {
        // Setup user role and authentication
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());
        when(authentication.getName()).thenReturn("user1");
        when(itemRepository.findById((long) 1)).thenReturn(Optional.of(testItem));

        // Execute and verify
        assertThrows(UnauthorizedItemUpdateException.class, () -> {
            itemService.updateItem((long)1, newItemData);
        });
    }

    @Test
    void updateItem_ItemNotFound_ShouldThrowException() {
        when(itemRepository.findById((long) 1)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> {
            itemService.updateItem((long)1, newItemData);
        });
    }
}  */