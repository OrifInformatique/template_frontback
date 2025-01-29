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

    public Item createItem(Item newItem) {
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        // Récupérer l'utilisateur depuis la base de données
        User author = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Assigner l'auteur à l'item
        newItem.setAuthor(author);
        
        // Sauvegarder l'item
        return itemRepository.save(newItem);
    }


    public Optional<Item> getItem(final Long id) {
        return itemRepository.findById(id);
    }

    public Iterable<Item> getItems() {
        return itemRepository.findAll();
    }

    public void deleteItem(final Long id) {
        itemRepository.deleteById(id);
    }

    public Item saveItem(Item item) {
        Item savedItem = itemRepository.save(item);
        return savedItem;
    }

    public Item updateItem(Long id, Item newItem) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return itemRepository.findById(id)
            .map(item -> {
                // Check permissions based on authorities
                boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
                boolean isSuperAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                
                System.out.println("isAdmin: " + isAdmin);
                System.out.println("isSuperAdmin: " + isSuperAdmin);

                if (!isAdmin && !isSuperAdmin) {
                    // Pour les utilisateurs normaux, vérifier s'ils sont l'auteur
                    String currentUsername = authentication.getName();
                    // Correction ici : comparer avec l'email de l'auteur
                    if (!item.getAuthor().getEmail().equals(currentUsername)) {
                        throw new ItemException.UnauthorizedItemUpdateException("You can only update your own items");
                    }
                } 
                
                item.setName(newItem.getName());
                item.setDescription(newItem.getDescription());
                return itemRepository.save(item);
            })
            .orElseThrow(() -> new ItemException.ItemNotFoundException(id));
    }
}
