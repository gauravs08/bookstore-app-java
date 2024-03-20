package fi.epassi.recruitment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@ResponseStatus(BAD_REQUEST)
public class ApplicationException extends ErrorResponseException {

    public ApplicationException(String message) {
        this(INTERNAL_SERVER_ERROR, message);
    }

    public ApplicationException() {
        super(INTERNAL_SERVER_ERROR);
    }

    public ApplicationException(HttpStatus responseStatus) {
        super(responseStatus);
    }

    public ApplicationException(HttpStatus responseStatus, String message) {
        this(responseStatus, message, null);
    }

    public ApplicationException(String message, Throwable e) {
        this(INTERNAL_SERVER_ERROR, message, e);
    }

    public ApplicationException(HttpStatus responseStatus, Throwable e) {
        super(responseStatus, e);
    }

    public ApplicationException(HttpStatus responseStatus, String message, Throwable e) {
        super(responseStatus, e);
        if (StringUtils.hasText(message)) {
            setDetail(message);
        }
    }
}
