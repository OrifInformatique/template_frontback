package ch.sectioninformatique.template.item;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserRepository;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(3)
public class ItemSeeder implements CommandLineRunner {
    
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemSeeder(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    /**
     * Run the item seeder
     * 
     * @param args The arguments
     * @throws Exception The exception
     */
    @Override
    public void run(String... args) throws Exception {
        loadItemData();
    }

    /**
     * Load the item data
     * Get all users except the "deleted user" (ID 1)
     * Create items with random authors (except the "deleted user")
     */
    private void loadItemData() {
        if (itemRepository.count() == 0) {

            var users = new ArrayList<User>();
            userRepository.findAll().forEach(user -> {
                if (user.getId() != 1L) {  
                    users.add(user);
                }
            });

            if (users.isEmpty()) {
                throw new RuntimeException("Aucun utilisateur trouvé pour créer les items");
            }

            var itemsData = List.of(
                new String[]{"Premier objet", "Description du premier objet"},
                new String[]{"Deuxième objet", "Description du deuxième objet"},
                new String[]{"Troisième objet", "Description du troisième objet"},
                new String[]{"Quatrième objet", "Description du quatrième objet"},
                new String[]{"Cinquième objet", "Description du cinquième objet"},
                new String[]{"Sixième objet", "Description du sixième objet"},
                new String[]{"Septième objet", "Description du septième objet"},
                new String[]{"Huitième objet", "Description du huitième objet"},
                new String[]{"Neuvième objet", "Description du neuvième objet"},
                new String[]{"Dixième objet", "Description du dixième objet"},
                new String[]{"Onzième objet", "Description du onzième objet"},
                new String[]{"Douzième objet", "Description du douzième objet"},
                new String[]{"Treizième objet", "Description du treizième objet"},
                new String[]{"Quatorzième objet", "Description du quatorzième objet"},
                new String[]{"Quinzième objet", "Description du quinzième objet"},
                new String[]{"Seizième objet", "Description du seizième objet"},
                new String[]{"Dix-septième objet", "Description du dix-septième objet"},
                new String[]{"Dix-huitième objet", "Description du dix-huitième objet"},
                new String[]{"Dix-neuvième objet", "Description du dix-neuvième objet"},
                new String[]{"Vingtième objet", "Description du vingtième objet"}
            );

            itemsData.forEach(data -> {
                User randomAuthor = users.get(new Random().nextInt(users.size()));
                
                Item item = new ItemBuilder()
                    .setName(data[0])
                    .setDescription(data[1])
                    .setAuthor(randomAuthor)
                    .build();
                
                itemRepository.save(item);
            });
        }
    }
}