package ch.sectioninformatique.template.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;



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
