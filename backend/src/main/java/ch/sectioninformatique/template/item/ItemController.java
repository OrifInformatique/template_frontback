package ch.sectioninformatique.template.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.item.ItemException.UnauthorizedItemUpdateException;

/* @RestController annotation indicates that the class is a Bean.
 *                 Indicates that returned datas have to be in JSON format in the http response's body
 */
@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private Environment environment;

    /**
     * Return some text informations to show that the application is running
     * and to see the value of some environment variables.
     * 
     * @return - A String
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String getHello() {
        return "<strong>Hello World !</strong><br>" +
               "<strong>JAreq_0891d0baeaa243dba3ac7683936cb32dVA_HOME : </strong>" + environment.getProperty("JAVA_HOME") + "<br>" +
               "<strong>Spring active profile : </strong>" + environment.getProperty("spring.profiles.active") + "<br>" +
               "<strong>Database used : </strong>" + environment.getProperty("spring.datasource.url");
    }

    /**
     * Read - Get all items
     * @return - An Iterable object of Items full filled
     */
    @PreAuthorize("hasAuthority('item:read')")
    @GetMapping("/items")
    public Iterable<Item> getItems() {
        return itemService.getItems();
    }

    /**
     * Read - Get one item by id
     * @return - A single item object
     */
    @PreAuthorize("hasAuthority('item:read')")
    @GetMapping("/items/{id}")
    public Item getItemById(@PathVariable Long id) {
        return itemService.getItem(id)
            .orElseThrow(() -> new ItemException.ItemNotFoundException(id));
    }
    
    /**
     * Create - Add a new item
     * @param item - The item to create
     * @return - The created item
     */
    @PreAuthorize("hasAuthority('item:write')")
    @PostMapping("/items")
    public Item createItem(@RequestBody Item item) {
        return itemService.createItem(item);
    }

    /**
     * Update - Update an item
     * @param id - The id of the item to update
     * @param item - The item to update
     * @return - The updated item with actual author and
     * @throws UnauthorizedItemUpdateException if the user doesn't have permission to update this item
     */
    @PreAuthorize("hasAuthority('item:update')")
    @PutMapping("/items/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemService.updateItem(id, item);
    }

    /**
     * Delete - Delete an item
     * @param id - The id of the item to delete
     */
    @PreAuthorize("hasAuthority('item:delete') || ((hasRole('ROLE_USER') || hasRole('ROLE_ADMIN')) && hasAuthority('item:write'))")
    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }
}
