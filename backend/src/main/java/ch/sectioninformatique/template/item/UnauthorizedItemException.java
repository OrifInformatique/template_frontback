package ch.sectioninformatique.template.item;

public class UnauthorizedItemException extends RuntimeException {
    public UnauthorizedItemException(String message) {
        super("You can only " + message + " your own items");
    }
}
