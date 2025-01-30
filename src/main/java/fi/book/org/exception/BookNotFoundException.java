package fi.book.org.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class BookNotFoundException extends ApplicationException {

    public BookNotFoundException(final String attribute, final String value) {
        super(NOT_FOUND, "No book found with %s: %s".formatted(attribute, value));
    }

    public BookNotFoundException() {
        super(NOT_FOUND, "No books in store with the given data");
    }

}
