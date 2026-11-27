package ch.sectioninformatique.template.item;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import ch.sectioninformatique.template.app.DeletionFilter;
import ch.sectioninformatique.template.item.ItemExceptions.ItemNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for items, the CRUD example module of the template.
 *
 * Endpoints follow the conventions of spring-auth:
 * - GET    /items?status=        list items (ACTIVE by default, DELETED, ALL)
 * - GET    /items/{id}           read an item (soft-deleted or not)
 * - POST   /items                create an item, 201 + Location + item
 * - PUT    /items/{id}           update an item, 200 + item
 * - DELETE /items/{id}           delete an item (soft by default), 204
 * - POST   /items/{id}/restore   restore a soft-deleted item, 200 + item
 *
 * Modifying an item (update, delete, restore) is allowed to its author and to
 * administrators only (checked by ItemService).
 */
@RequestMapping("/items")
@RequiredArgsConstructor
@RestController
public class ItemController {

    private final ItemService itemService;

    /**
     * Lists items, filtered by deletion state.
     *
     * @param status ACTIVE (default), DELETED or ALL, case-insensitive
     * @return 200 with the items
     */
    @GetMapping
    @PreAuthorize("hasAuthority('item:read')")
    public ResponseEntity<List<ItemDto>> getItems(@RequestParam(defaultValue = "ACTIVE") DeletionFilter status) {
        return ResponseEntity.ok(itemService.getItems(status).stream().map(ItemDto::from).toList());
    }

    /**
     * Reads an item, soft-deleted or not (its "deleted" field tells).
     *
     * @param id the item id
     * @return 200 with the item, 404 if unknown
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('item:read')")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long id) {
        return itemService.getItem(id)
                .map(item -> ResponseEntity.ok(ItemDto.from(item)))
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    /**
     * Creates an item, authored by the authenticated user.
     *
     * @param request the name and description of the item
     * @return 201 with the created item and its URL in the Location header
     */
    @PostMapping
    @PreAuthorize("hasAuthority('item:write')")
    public ResponseEntity<ItemDto> createItem(@RequestBody @Valid ItemRequestDto request) {
        // A new entity built from the allowed fields only: a client can't choose
        // the id (which would overwrite an existing item), author or deletion state
        ItemDto created = ItemDto.from(itemService.createItem(request.toItem()));
        URI location = UriComponentsBuilder.fromPath("/items/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Updates the name and description of an item.
     *
     * @param id      the item id
     * @param request the new name and description
     * @return 200 with the updated item
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('item:update')")
    public ResponseEntity<ItemDto> updateItem(@PathVariable Long id, @RequestBody @Valid ItemRequestDto request) {
        return ResponseEntity.ok(ItemDto.from(itemService.updateItem(id, request.toItem())));
    }

    /**
     * Deletes an item.
     *
     * @param id        the item id
     * @param permanent true to remove the item from the database, false
     *                  (default) for a soft, restorable deletion
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('item:delete') || ((hasRole('ROLE_USER') || hasRole('ROLE_ADMIN')) && hasAuthority('item:write'))")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean permanent) {
        if (permanent) {
            itemService.deletePermanentById(id);
        } else {
            itemService.deleteItem(id);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Restores a soft-deleted item.
     *
     * @param id the item id
     * @return 200 with the restored item
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('item:update')")
    public ResponseEntity<ItemDto> restoreItem(@PathVariable Long id) {
        return ResponseEntity.ok(ItemDto.from(itemService.restoreItem(id)));
    }
}
