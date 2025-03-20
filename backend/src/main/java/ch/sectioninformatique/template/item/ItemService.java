package ch.sectioninformatique.template.item;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    /**
     * Extract the current user's email from the authentication principal
     * 
     * @return The current user's email
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Full authentication principal: {}", authentication.getPrincipal());
        
        String currentUserEmail = authentication.getPrincipal().toString();
        // Extract only the login from the principal string
        currentUserEmail = currentUserEmail.substring(currentUserEmail.indexOf("login=") + 6);
        currentUserEmail = currentUserEmail.substring(0, currentUserEmail.indexOf(","));
        logger.debug("Extracted user email: {}", currentUserEmail);
        
        return currentUserEmail;
    }

    /**
     * Create an item
     * 
     * @param newItem The item to create
     * @return The created item
     */
    public Item createItem(Item newItem) {
        String currentUserEmail = getCurrentUserEmail();
        
        if (!userRepository.existsByLogin(currentUserEmail)) {
            User newUser = new User();
            newUser.setLogin(currentUserEmail);
            newUser.setFirstName("Azure User");
            userRepository.save(newUser);
        }
        
        User author = userRepository.findByLogin(currentUserEmail)
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
        String currentUserEmail = getCurrentUserEmail();
        
        User currentUser = userRepository.findByLogin(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isSuperAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        
        if (!isAdmin && !isSuperAdmin) {
            if (item.getAuthor().getId() != currentUser.getId()) {
                throw new UnauthorizedItemException("delete");
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
        String currentUserEmail = getCurrentUserEmail();
        
        User currentUser = userRepository.findByLogin(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        logger.debug("Found user with ID: {}", currentUser.getId());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return itemRepository.findById(id)
            .map(item -> {
                boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
                boolean isSuperAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                logger.debug("User roles - isAdmin: {}, isSuperAdmin: {}", isAdmin, isSuperAdmin);

                if (!isAdmin && !isSuperAdmin) {
                    logger.debug("Checking authorization - Item author ID: {}, Current user ID: {}", 
                        item.getAuthor().getId(), currentUser.getId());
                    if (item.getAuthor().getId() != currentUser.getId()) {
                        logger.debug("Authorization failed - User is not the author of the item");
                        throw new UnauthorizedItemException("update");
                    }
                } 
                
                item.setName(newItem.getName());
                item.setDescription(newItem.getDescription());
                item.setAuthor(currentUser);
                logger.debug("Item updated successfully - ID: {}, New name: {}", id, newItem.getName());
                return itemRepository.save(item);
            })
            .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
