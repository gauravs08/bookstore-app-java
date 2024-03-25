package fi.epassi.recruitment.exception;

public class InventoryUpdateException extends ApplicationException {

    public InventoryUpdateException(final String isbn) {
        super("Inventory Update failure for ISBN {%s}".formatted(isbn));
    }
}
