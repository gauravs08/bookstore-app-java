package fi.book.org.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class InventoryNotFoundException extends ApplicationException {

    public InventoryNotFoundException(final String attribute, final String value) {
        super(NOT_FOUND, "No inventory found with %s: %s".formatted(attribute, value));
    }

    public InventoryNotFoundException() {
        super(NOT_FOUND, "No Inventory Found!");
    }
}
