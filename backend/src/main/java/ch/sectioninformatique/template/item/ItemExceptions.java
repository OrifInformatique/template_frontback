package ch.sectioninformatique.template.item;
import org.springframework.http.HttpStatus;

import ch.sectioninformatique.template.app.exceptions.AppException;;

public class ItemExceptions {

    public static class ItemNotFoundException extends AppException {
  
        /**
        * Constructs a new ItemNotFoundException with a message indicating
        * that the item with the specified ID could not be found.
        *
        * @param id The ID of the item that was not found
        */
        public ItemNotFoundException(Long id) {
            super("Could not find item " + id,HttpStatus.NOT_FOUND);
        }
    }


    public static class UnauthorizedItemException extends AppException {
    
        /**
        * Constructs a new UnauthorizedItemException with a message indicating
        * that the user can only perform the specified operation on their own items.
        *
        * @param message The operation that was attempted (e.g., "update", "delete")
        */
        public UnauthorizedItemException(String message) {
            super("You can only " + message + " your own items",HttpStatus.UNAUTHORIZED);
        }
    }

}
