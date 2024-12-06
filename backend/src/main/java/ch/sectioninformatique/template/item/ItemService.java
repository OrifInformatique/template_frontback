package ch.sectioninformatique.template.item;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* @Service annotation indicates that the class is a business layer Bean, used as a bridge
 *          between repository and controller.
 */
@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

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
}
