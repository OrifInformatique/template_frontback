package ch.sectioninformatique.template.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
/* Implementing CommandLineRunner will execute the run method just after application starts */
public class ItemSeeder implements CommandLineRunner {
    
    @Autowired
    ItemRepository itemRepository;

    @Override
    public void run(String... args) throws Exception {
        loadItemData();
    }

    private void loadItemData() {
        if (itemRepository.count() == 0) {
            Item item1 = new Item("Premier objet");
            Item item2 = new Item("Deuxième objet");
            Item item3 = new Item("Troisième objet");

            itemRepository.save(item1);
            itemRepository.save(item2);
            itemRepository.save(item3);
        }
    }
}
