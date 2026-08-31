package ch.sectioninformatique.template.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.sectioninformatique.template.security.Role;
import ch.sectioninformatique.template.security.RoleEnum;
import ch.sectioninformatique.template.security.RoleRepository;
import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserRepository;

@SpringBootTest
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void getItemsTest() {
        // Clear items table
        itemRepository.deleteAllPermanently();
        
        Optional<Role> role = roleRepository.findByName(RoleEnum.USER);
        User author = User.builder()
            .firstName("author")
            .lastName("test")
            .login("test.author@test.com")
            .mainRole(role.get())
            .build();
        author = userRepository.save(author);

        Item item = new Item();
        item.setName("Test Item 1");
        item.setDescription("This is a test item.");
        item.setAuthor(author);
        itemRepository.save(item);

        item = new Item();
        item.setName("Test Item 2");
        item.setDescription("This is a test item.");
        item.setAuthor(author);
        itemRepository.save(item);

        item = new Item();
        item.setName("Test Item 3");
        item.setDescription("This is soft deleted test item.");
        item.setAuthor(author);
        item.setDeleted(true);
        item = itemRepository.save(item);

        // Check that default getItems returns only non-deleted items
        List<Item> items = itemService.getItems();
        assertEquals(2, items.size());

        // Check that getItems with includeDeleted = false returns only non-deleted items
        items = itemService.getItems(false);
        assertEquals(2, items.size());

        // Check that getItems with includeDeleted = true returns all items, uncluding deleted ones
        items = itemService.getItems(true);
        assertEquals(3, items.size());

        // Clear items table
        itemRepository.deleteAllPermanently();
        // Clear author from database
        userRepository.deletePermanentlyById(author.getId());
    }

    @Test
    public void deleteAuthorTest() {

        Optional<Role> role = roleRepository.findByName(RoleEnum.USER);
        User author = User.builder()
            .firstName("author")
            .lastName("test")
            .login("test.author@test.com")
            .mainRole(role.get())
            .build();
        userRepository.save(author);

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("This is a test item.");
        item.setAuthor(author);

        itemRepository.save(item);

        userRepository.deletePermanentlyById(author.getId());

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();

        //Then
        assertEquals(null, updatedItem.getAuthor());
    }
}
