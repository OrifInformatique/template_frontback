package ch.sectioninformatique.template.item;

import ch.sectioninformatique.template.user.User;

/**
 * Builder for the Item class
 */
public class ItemBuilder {
    private String name;
    private String description;
    private User author;

    /**
     * Constructor for the ItemBuilder class
     */
    public ItemBuilder() {
    }

    /**
     * Set the name of the item
     * 
     * @param name The name of the item
     * @return The ItemBuilder object
     */
    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the description of the item
     * 
     * @param description The description of the item
     * @return The ItemBuilder object
     */
    public ItemBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the author of the item
     * 
     * @param author The author of the item
     * @return The ItemBuilder object
     */
    public ItemBuilder setAuthor(User author) {
        this.author = author;
        return this;
    }

    /**
     * Build the item
     * 
     * @return The Item object
     */
    public Item build() {
        return new Item(name, description, author);
    }
}