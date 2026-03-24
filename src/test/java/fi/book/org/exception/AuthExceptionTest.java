package fi.book.org.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class AuthExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        var ex = new AuthException("Invalid credentials");

        assertThat(ex.getStatusCode()).isEqualTo(UNAUTHORIZED);
        assertThat(ex.getMessage()).contains("Invalid credentials");
    }

    @Test
    void shouldCreateWithDefaultMessage() {
        var ex = new AuthException();

        assertThat(ex.getStatusCode()).isEqualTo(UNAUTHORIZED);
        assertThat(ex.getMessage()).contains("Invalid request");
    }
}
