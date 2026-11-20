package ch.sectioninformatique.template.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body to create or update an item ({@code POST /items},
 * {@code PUT /items/{id}}).
 *
 * Only the fields a client may set: the id, author and deletion state of an
 * item are managed by the server and can't be sent.
 *
 * @param name        name of the item
 * @param description description of the item, at most 1000 characters
 */
public record ItemRequestDto(
        @NotBlank String name,
        @Size(max = 1000) String description) {

    /**
     * @return a new item (no id, no author yet) holding this request's fields
     */
    public Item toItem() {
        return new Item(name, description, null);
    }
}
