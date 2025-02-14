package fi.book.org.exception;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class AuthException extends ApplicationException {

    AuthException(final String attribute, final String value) {
        super(UNAUTHORIZED, "Unauthorised %s: %s".formatted(attribute, value));
    }

    public AuthException(final String message) {
        super(UNAUTHORIZED, message);
    }

    public AuthException() {
        super(UNAUTHORIZED, "Invalid request");
    }
}
