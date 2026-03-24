package fi.book.org.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class BookCreateExceptionTest {

    @Test
    void shouldCreateWithAttributeAndValue() {
        var ex = new BookCreateException("ISBN", "123");

        assertThat(ex.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Exception to create or update book with ISBN: 123");
    }

    @Test
    void shouldCreateWithDefaultMessage() {
        var ex = new BookCreateException();

        assertThat(ex.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(ex.getMessage()).contains("Exception to create or update book");
    }
}
