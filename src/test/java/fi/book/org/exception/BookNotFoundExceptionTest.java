package fi.book.org.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class BookNotFoundExceptionTest {

    @Test
    void shouldCreateWithAttributeAndValue() {
        var ex = new BookNotFoundException("ISBN", "123");

        assertThat(ex.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(ex.getMessage()).contains("No book found with ISBN: 123");
    }

    @Test
    void shouldCreateWithDefaultMessage() {
        var ex = new BookNotFoundException();

        assertThat(ex.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(ex.getMessage()).contains("No books in store with the given data");
    }
}
