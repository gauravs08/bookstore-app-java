package fi.book.org.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BookCreateException extends ApplicationException {

    public BookCreateException(final String attribute, final String value) {
        super(BAD_REQUEST, "Exception to create or update book with %s: %s".formatted(attribute, value));
    }

    public BookCreateException() {
        super(BAD_REQUEST, "Exception to create or update book");
    }
}
