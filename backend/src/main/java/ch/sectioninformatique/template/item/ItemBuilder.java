package ch.sectioninformatique.template.item;

import ch.sectioninformatique.template.user.User;

public class ItemBuilder {
    private Integer id;
    private String name;
    private String description;
    private User author;

    public ItemBuilder() {
    }

    public ItemBuilder setId(Integer id) {
        this.id = id;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemBuilder setAuthor(User author) {
        this.author = author;
        return this;
    }

    public Item build() {
        return new Item(name, description, author);
    }
}