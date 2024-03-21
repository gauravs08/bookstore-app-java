package fi.epassi.recruitment.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class InventoryUpdateException extends ApplicationException {

    public InventoryUpdateException(final String isbn) {
        super("Inventory Update failure for ISBN {%s}".formatted(isbn));
    }
}
