package ch.sectioninformatique.template.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/")
    public String getHello() {
        return "<strong>Hello World !</strong><br>" +
               "<strong>JAVA_HOME : </strong>" + environment.getProperty("JAVA_HOME") + "<br>" +
               "<strong>Spring active profile : </strong>" + environment.getProperty("spring.profiles.active") + "<br>" +
               "<strong>Database used : </strong>" + environment.getProperty("spring.datasource.url");
    }

    /**
     * Read - Get all items
     * @return - An Iterable object of Items full filled
     */
    @GetMapping("/items")
    public Iterable<Item> getItems() {
        return itemService.getItems();
    }

    /**
     * Read - Get one item by id
     * @return - A single item object
     */
    @GetMapping("/items/{id}")
    public Item getItemById(@PathVariable Long id) {
        /* Try to get the corresponding item, else throw an exception */
        return itemService.getItem(id)
            .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
