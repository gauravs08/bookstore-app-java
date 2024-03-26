package fi.epassi.recruitment.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class BookstoreNotFoundException extends ApplicationException {
    public BookstoreNotFoundException(String s) {
        super(NOT_FOUND, s);
    }
}
