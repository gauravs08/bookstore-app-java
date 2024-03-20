package fi.epassi.recruitment.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class InventoryNotFoundException extends ApplicationException {

    public InventoryNotFoundException(final String isbn) {
        super(NOT_FOUND, "No inventory found with ISBN {%s}".formatted(isbn));
    }
}
