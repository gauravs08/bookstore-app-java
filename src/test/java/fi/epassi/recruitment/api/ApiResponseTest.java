package fi.epassi.recruitment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void shouldCreateDefaultOkResponse() {
        // When
        var response = ApiResponse.ok();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isNull();
    }

    @Test
    void shouldCreateOkResponseWithBody() {
        // When
        var response = ApiResponse.ok("Body");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isEqualTo("Body");
    }

    @Test
    void shouldCreateOkResponseWithStatusMessageAndBody() {
        // When
        var response = ApiResponse.ok("Status message", "Body");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusMessage()).isEqualTo("Status message");
        assertThat(response.getResponse()).isEqualTo("Body");
    }

    @Test
    void shouldCreateResponseWithBasicBuilder() {
        // When
        var response = ApiResponse.buildResponse(BAD_REQUEST, "Response object");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getResponse()).isEqualTo("Response object");
    }

    @Test
    void shouldCreateResponseWithExtendedBuilder() {
        // When
        var response = ApiResponse.buildResponse(BAD_REQUEST, "Status message", "Response object");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getStatusMessage()).isEqualTo("Status message");
        assertThat(response.getResponse()).isEqualTo("Response object");
    }
}
