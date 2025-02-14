package ch.sectioninformatique.template.item;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.sectioninformatique.template.user.UserRepository;
import ch.sectioninformatique.template.user.User;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/* @Service annotation indicates that the class is a business layer Bean, used as a bridge
 *          between repository and controller.
 */
@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create an item
     * 
     * @param newItem The item to create
     * @return The created item
     */
    public Item createItem(Item newItem) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User author = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        newItem.setAuthor(author);
        
        return itemRepository.save(newItem);
    }

    /**
     * Get an item by its id
     * 
     * @param id The id of the item
     * @return The item
     */
    public Optional<Item> getItem(final Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Get all items
     * 
     * @return The items
     */
    public Iterable<Item> getItems() {
        return itemRepository.findAll();
    }

    /**
     * Delete an item
     * 
     * @param id The id of the item
     */
    public void deleteItem(final Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemException.ItemNotFoundException(id));
        
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isSuperAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        
        if (!isAdmin && !isSuperAdmin) {
            if (item.getAuthor().getId() != currentUser.getId()) {
                throw new ItemException.UnauthorizedItemUpdateException("You can only delete your own items");
            }
        }
        
        itemRepository.deleteById(id);
    }

    /**
     * Update an item
     * 
     * @param id The id of the item
     * @param newItem The new item
     * @return The updated item
     */
    public Item updateItem(Long id, Item newItem) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return itemRepository.findById(id)
            .map(item -> {
                boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
                boolean isSuperAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));

                if (!isAdmin && !isSuperAdmin) {
                    if (item.getAuthor().getId() != currentUser.getId()) {
                        throw new ItemException.UnauthorizedItemUpdateException("You can only update your own items");
                    }
                } 
                
                item.setName(newItem.getName());
                item.setDescription(newItem.getDescription());
                item.setAuthor(currentUser);
                return itemRepository.save(item);
            })
            .orElseThrow(() -> new ItemException.ItemNotFoundException(id));
    }
}
