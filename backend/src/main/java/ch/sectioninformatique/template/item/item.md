# ITEM PACKAGE

## Table of Contents
- [Item.java](#itemjava)
- [ItemBuilder.java](#itembuilderjava)
- [ItemController.java](#itemcontrollerjava)
    - [Endpoints](#endpoints)
    - [Security](#security)
- [ItemExceptions.java](#itemexceptionsjava)
    - [ItemNotFoundException](#itemnotfoundexception)
    - [UnauthorizedItemException](#unauthorizeditemexception)
- [ItemRepository.java](#itemrepositoryjava)
- [ItemDto.java](#itemdtojava)
- [ItemRequestDto.java](#itemrequestdtojava)
- [ItemSeeder.java](#itemseederjava)
- [ItemService.java](#itemservicejava)
    - [getCurrentUser()](#getcurrentuser)
    - [createItem()](#createitem)
    - [getItem()](#getitem)
    - [getItems()](#getitems)
    - [deleteItem()](#deleteitem)
    - [deletePermanentById()](#deletepermanentbyid)
    - [updateItem()](#updateitem)

## *Item.java*

`Item` is a JPA Entity class that represents an item in the database.

It stores information like the item's name, description, author, timestamps, and deletion status.

It contains :

* `id`,
* `name`,
* `description`,
* `author`,
* `createdAt`,
* `updatedAt`,
* `deleted`

Each item is linked to a `User` through the `author` field, which represents the user who created the item.

The class also uses soft deletion, meaning deleted items are marked as `deleted` instead of being permanently removed from the database.

`createdAt` and `updatedAt` are automatically managed to keep track of when the item was created and last modified.

---
## *ItemBuilder.java*

`ItemBuilder` is a Builder class used to create `Item` objects.

It allows the properties of an item to be set step by step before creating the object.

It contains :

* `name`,
* `description`,
* `author`

The `setName()`, `setDescription()`, and `setAuthor()` methods set the corresponding values and return the same `ItemBuilder` instance, allowing the methods to be chained together.

The `build()` method creates a new `Item` using the configured values.

For example, the builder can be used to create an item by setting its name, description, and author before calling `build()`.

This class is useful when creating an `Item` because it separates the **configuration of the object** from its **creation**.

---

## *ItemController.java*

`ItemController` is a REST Controller responsible for handling HTTP requests related to items.

It uses `ItemService` to perform the actual item operations instead of directly accessing the database.

All endpoints use the `/items` path and return data in JSON format.

It contains :

* `getItems()`,
* `getItem()`,
* `createItem()`,
* `updateItem()`,
* `deleteItem()`,
* `restoreItem()`

### Endpoints

Every endpoint returns items as `ItemDto` and errors as the standard `ErrorResponse`.

| Endpoint | Authority | Response |
|---|---|---|
| `GET /items?status=ACTIVE\|DELETED\|ALL` | `item:read` | `200`, the items (`ACTIVE` by default, case-insensitive) |
| `GET /items/{id}` | `item:read` | `200`, the item, soft-deleted or not; `404` if unknown |
| `POST /items` | `item:write` | `201`, the created item, with its URL in the `Location` header |
| `PUT /items/{id}` | `item:update` | `200`, the updated item |
| `DELETE /items/{id}?permanent=false` | `item:delete`, or `item:write` for users and admins | `204`; `permanent=true` removes the item from the database |
| `POST /items/{id}/restore` | `item:update` | `200`, the restored item |

`POST` and `PUT` accept an `ItemRequestDto` (`name`, required, and `description`,
at most 1000 characters): the id, author and deletion state can't be sent.
Updating, deleting and restoring an item is allowed to its author and to
administrators only (`ItemService.findModifiableItem()`).

### Security

The controller uses `@PreAuthorize` to check whether the current user has the required permissions before accessing an endpoint.

The permissions used are :

* `item:read` → reading items,
* `item:write` → creating items,
* `item:update` → updating items,
* `item:delete` → deleting items.

The controller passes the requests to `ItemService`, where additional authorization checks are performed, such as checking whether the user is the item's author.


---

## *ItemExceptions.java*

`ItemExceptions` is a container class for item-related exceptions.

This class groups all item-specific exceptions as static inner classes.

It contains :

* `ItemNotFoundException`,
* `UnauthorizedItemException`

### `ItemNotFoundException`

Thrown when an item with the specified ID cannot be found.

It uses the HTTP status `404 NOT_FOUND` and the message key `item.notFound`.

### `UnauthorizedItemException`

Thrown when a user tries to perform an operation on an item they are not authorized to access.

It uses the HTTP status `403 FORBIDDEN` (the user is authenticated, but not allowed on this item) and the message key `item.unauthorized`.

Both exceptions implement `MessageKeyProvider` so that their error messages can be handled by the application's internationalization system.


---
## *ItemRepository.java*

`ItemRepository` is a repository interface used to access and manage `Item` entities in the database.

It extends `CrudRepository<Item, Long>`, which provides basic CRUD operations such as creating, finding, updating, and deleting items.

It contains :

* `findAllIncludingDeleted()`,
* `existsById()`,
* `deletePermanentlyById()`

`findAllIncludingDeleted()` retrieves all items from the database, including soft-deleted items.

`existsById()` checks whether an item with a specific ID exists in the database.

`deletePermanentlyById()` permanently deletes an item from the database using its ID, unlike the normal soft deletion.

`@Modifying` indicates that the query modifies the database, while `@Transactional` ensures that the operation is executed within a transaction.

Spring Data JPA automatically provides the implementation of this repository.


---
## *ItemDto.java*

`ItemDto` is the item returned by every item endpoint (list, detail, create,
update, restore).

It contains :

* `id`,
* `name`,
* `description`,
* `author` : `id`, `firstName` and `lastName` of the author (`AuthorDto`), or `null` once the author was permanently deleted,
* `createdAt`,
* `updatedAt`,
* `deleted`

The author is reduced to its id and names: the API never exposes the whole
`User` entity, with its roles and authorities.

`ItemDto.from(Item item)` converts an `Item` entity into an `ItemDto`.

## *ItemRequestDto.java*

`ItemRequestDto` is the body of the create and update requests. It only
contains the fields a client may set: `name` (required) and `description` (at
most 1000 characters). `toItem()` builds a new `Item` from them, with no id,
author or deletion state.


---
## *ItemSeeder.java*

`ItemSeeder` is a seeder class used to create sample `Item` data when the application starts.

It implements `CommandLineRunner`, so its `run()` method is automatically executed by Spring Boot at startup.

It contains :

* `itemRepository`,
* `userRepository`

`@Order(3)` makes the seeder run after seeders with a lower order number, such as `UserSeeder`.

The seeder only creates items if the `items` table is empty.

It retrieves the existing users and excludes the user with ID `1`, which represents the deleted user.

It then creates **20 sample items** with French names and descriptions. Each item is assigned a random existing user as its author.

`ItemBuilder` is used to create each item before it is saved using `itemRepository`.

If no valid users are found, the seeder throws a `RuntimeException` because an item cannot be created without an author.


---
## *ItemService.java*

`ItemService` is a service class that contains the main business logic for managing items.

It acts as a bridge between the `ItemController`, the repositories, and the database.

It handles :

* creating items,
* retrieving items,
* updating items,
* deleting items,
* checking user permissions,
* identifying the currently logged-in user.

It uses :

* `ItemRepository`,
* `UserRepository`,
* `EntityManager`

`@Service` marks the class as a Spring service.

### `getCurrentUser()`

Gets the local user matching the currently authenticated user.

The authentication principal is the user read from the JWT: its `id` is the
spring-auth id, which is also the local user id. The user is found with
`findById`, never by login. The local user always exists at this point, because
`UserAuthenticationProvider` gets or creates it when it validates the token.

This user is used to identify who performs an operation on an item.

### `createItem()`

Creates a new item.

The current user (`getCurrentUser()`) is assigned as the item's `author` before
the item is saved.

### `getItem()`

Finds an item using its ID.

It returns an `Optional<Item>`, which means the item may or may not exist.

### `getItems()`

Retrieves the items matching a `DeletionFilter` (the `status` parameter of `GET /items`):

* `ACTIVE` → items where `deleted = false` (`findAllByDeletedFalse()`),
* `DELETED` → soft-deleted items only (`findAllByDeletedTrue()`),
* `ALL` → every item (`findAllIncludingDeleted()`).

### `deleteItem()`

Deletes an item using its ID.

Before deleting, it checks the user's permissions.

An item can be deleted by :

* its author,
* an administrator (`ROLE_ADMIN`),
* a super administrator (`ROLE_SUPER_ADMIN`).

If the user is not authorized, an `UnauthorizedItemException` is thrown.

The normal `deleteById()` operation uses the `Item` entity's soft-delete configuration.

### `deletePermanentById()`

Permanently deletes an item from the database.

Unlike `deleteItem()`, this does not use soft deletion.

Like `updateItem()`, `deleteItem()` and `restoreItem()`, it goes through
`findModifiableItem()`: only the item's author or an administrator can do it.
If the item does not exist, an `ItemNotFoundException` (404) is thrown; if the
user is not allowed, an `UnauthorizedItemException` (403).

### `updateItem()`

Updates an existing item.

The method first checks the current user and the item.

Only the item's author, an administrator, or a super administrator can update it.

It updates :

* `name`,
* `description`

and then saves the updated item. The `author` is not changed: it stays the
item's creator, even when an administrator updates someone else's item.

If the item does not exist, an `ItemNotFoundException` is thrown.

If the user is not allowed to modify it, an `UnauthorizedItemException` is thrown.

### Authorization

The service uses Spring Security to check the current user's roles.

```text
Current user
     ↓
Spring Security
     ↓
Is ADMIN / SUPER_ADMIN?
     ↓
Yes → allowed
No  → check if user is the item's author
     ↓
   allowed / unauthorized
```

This prevents regular users from modifying or deleting items belonging to other users.
