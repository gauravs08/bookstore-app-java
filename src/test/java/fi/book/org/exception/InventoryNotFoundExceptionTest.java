package fi.book.org.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class InventoryNotFoundExceptionTest {

    @Test
    void shouldCreateWithAttributeAndValue() {
        var ex = new InventoryNotFoundException("ISBN", "abc");

        assertThat(ex.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(ex.getMessage()).contains("No inventory found with ISBN: abc");
    }

    @Test
    void shouldCreateWithDefaultMessage() {
        var ex = new InventoryNotFoundException();

        assertThat(ex.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(ex.getMessage()).contains("No Inventory Found!");
    }
}
