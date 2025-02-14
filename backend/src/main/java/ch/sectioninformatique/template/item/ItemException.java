package ch.sectioninformatique.template.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public class ItemException {
     
    /**
     * Exception for the item not found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(Long id) {
            super("Could not find item " + id);
        }
    }

    /**
     * Exception for the unauthorized item update
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedItemUpdateException extends RuntimeException {
        public UnauthorizedItemUpdateException(String message) {
            super(message);
        }
    }

    /**
     * Error response for the item exception
     */
    private static class ErrorResponse {
        @JsonProperty("status")
        private final int status;
        
        @JsonProperty("error")
        private final String error;
        
        @JsonProperty("message")
        private final String message;

        /**
         * Constructor for the ErrorResponse class
         * 
         * @param status The status of the error
         * @param message The message of the error
         */
        public ErrorResponse(HttpStatus status, String message) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
        }
    }

    /**
     * Exception handler for the item exception
     */
    @ControllerAdvice
    public static class ItemExceptionHandler {
        @ExceptionHandler(ItemNotFoundException.class)
        @ResponseBody
        public ResponseEntity<ErrorResponse> itemNotFoundHandler(ItemNotFoundException e) {
            ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(UnauthorizedItemUpdateException.class)
        @ResponseBody
        public ResponseEntity<ErrorResponse> unauthorizedItemUpdateHandler(UnauthorizedItemUpdateException e) {
            ErrorResponse response = new ErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }   
}
