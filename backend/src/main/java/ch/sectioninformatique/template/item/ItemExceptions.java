package ch.sectioninformatique.template.item;

public class ItemExceptions {

    public static class ItemNotFoundException extends RuntimeException {
  
        /**
        * Constructs a new ItemNotFoundException with a message indicating
        * that the item with the specified ID could not be found.
        *
        * @param id The ID of the item that was not found
        */
        public ItemNotFoundException(Long id) {
            super("Could not find item " + id);
        }
    }


    public static class UnauthorizedItemException extends RuntimeException {
    
        /**
        * Constructs a new UnauthorizedItemException with a message indicating
        * that the user can only perform the specified operation on their own items.
        *
        * @param message The operation that was attempted (e.g., "update", "delete")
        */
        public UnauthorizedItemException(String message) {
            super("You can only " + message + " your own items");
        }
    }

}
