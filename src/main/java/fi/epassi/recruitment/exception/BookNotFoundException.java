package fi.epassi.recruitment.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class BookNotFoundException extends ApplicationException {

    public BookNotFoundException(final String isbn) {
        super(NOT_FOUND, "No book found with ISBN {%s}".formatted(isbn));
    }
}
