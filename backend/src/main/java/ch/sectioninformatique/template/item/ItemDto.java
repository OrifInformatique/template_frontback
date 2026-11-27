package ch.sectioninformatique.template.item;

import java.util.Date;

import ch.sectioninformatique.template.user.User;

/**
 * Item returned by the item endpoints (list, detail, create, update, restore).
 *
 * Only exposes what clients need: the author is reduced to its id and names
 * (never the whole user with its roles and authorities).
 *
 * @param id          item id
 * @param name        item name
 * @param description item description
 * @param author      author of the item, null once the author was permanently deleted
 * @param createdAt   creation date
 * @param updatedAt   last update date
 * @param deleted     true if the item is soft-deleted
 */
public record ItemDto(
        Long id,
        String name,
        String description,
        AuthorDto author,
        Date createdAt,
        Date updatedAt,
        boolean deleted) {

    /**
     * Author of an item.
     *
     * @param id        user id (the spring-auth user id)
     * @param firstName first name
     * @param lastName  last name
     */
    public record AuthorDto(long id, String firstName, String lastName) {
    }

    /**
     * @param item the item entity
     * @return the item as returned by the API
     */
    public static ItemDto from(Item item) {
        User author = item.getAuthor();
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                // The author is null once permanently deleted (@OnDelete SET_NULL)
                author == null ? null : new AuthorDto(author.getId(), author.getFirstName(), author.getLastName()),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.isDeleted());
    }
}
