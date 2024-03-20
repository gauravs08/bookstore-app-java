package fi.epassi.recruitment.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.Test;

class ApplicationExceptionTest {

    private static final String CUSTOM_MESSAGE = "This is a custom message";
    private static final String INTERNAL_CAUSE_MESSAGE = "Internal cause message";

    @Test
    void shouldCreationApplicationExceptionWithoutParams() {
        // When
        var ex = new ApplicationException();

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomMessage() {
        // When
        var ex = new ApplicationException(CUSTOM_MESSAGE);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(ex.getMessage()).contains(CUSTOM_MESSAGE);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomResponseStatus() {
        // When
        var ex = new ApplicationException(BAD_REQUEST);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomResponseStatusAndMessage() {
        // When
        var ex = new ApplicationException(UNAUTHORIZED, CUSTOM_MESSAGE);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(UNAUTHORIZED);
        assertThat(ex.getMessage()).contains(CUSTOM_MESSAGE);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomMessageAndThrowable() {
        // When
        var throwableMock = mock(Throwable.class);
        when(throwableMock.getMessage()).thenReturn(INTERNAL_CAUSE_MESSAGE);

        var ex = new ApplicationException(CUSTOM_MESSAGE, throwableMock);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(ex.getCause().getMessage()).isEqualTo(INTERNAL_CAUSE_MESSAGE);
        assertThat(ex.getMessage()).contains(CUSTOM_MESSAGE);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomResponseStatusAndThrowable() {
        // When
        var throwableMock = mock(Throwable.class);
        when(throwableMock.getMessage()).thenReturn(INTERNAL_CAUSE_MESSAGE);

        var ex = new ApplicationException(SERVICE_UNAVAILABLE, throwableMock);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(SERVICE_UNAVAILABLE);
        assertThat(ex.getCause().getMessage()).contains(INTERNAL_CAUSE_MESSAGE);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomResponseStatusAndMessageAndThrowable() {
        // When
        var throwableMock = mock(Throwable.class);
        when(throwableMock.getMessage()).thenReturn(INTERNAL_CAUSE_MESSAGE);

        var ex = new ApplicationException(SERVICE_UNAVAILABLE, CUSTOM_MESSAGE, throwableMock);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(SERVICE_UNAVAILABLE);
        assertThat(ex.getMessage()).contains(CUSTOM_MESSAGE);
        assertThat(ex.getCause().getMessage()).contains(INTERNAL_CAUSE_MESSAGE);
    }

    @Test
    void shouldCreationApplicationExceptionWithCustomResponseStatusAndEmptyMessageAndThrowable() {
        // When
        var throwableMock = mock(Throwable.class);
        when(throwableMock.getMessage()).thenReturn(INTERNAL_CAUSE_MESSAGE);

        var ex = new ApplicationException(SERVICE_UNAVAILABLE, null, throwableMock);

        // Then
        assertThat(ex.getStatusCode()).isEqualTo(SERVICE_UNAVAILABLE);
        assertThat(ex.getCause().getMessage()).contains(INTERNAL_CAUSE_MESSAGE);
    }
}
