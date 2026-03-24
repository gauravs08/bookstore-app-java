package fi.book.org.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class BookstoreNotFoundExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        var ex = new BookstoreNotFoundException("Store not found");

        assertThat(ex.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(ex.getMessage()).contains("Store not found");
    }
}
