package fi.book.org.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class ApiResponseTest {

    @Test
    void shouldCreateDefaultOkResponse() {

        var response = ApiResponse.ok();


        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isNull();
    }

    @Test
    void shouldCreateOkResponseWithBody() {

        var response = ApiResponse.ok("Body");


        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isEqualTo("Body");
    }

    @Test
    void shouldCreateOkResponseWithStatusMessageAndBody() {

        var response = ApiResponse.ok("Status message", "Body");


        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusMessage()).isEqualTo("Status message");
        assertThat(response.getResponse()).isEqualTo("Body");
    }

    @Test
    void shouldCreateResponseWithBasicBuilder() {

        var response = ApiResponse.buildResponse(BAD_REQUEST, "Response object");


        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getResponse()).isEqualTo("Response object");
    }

    @Test
    void shouldCreateResponseWithExtendedBuilder() {

        var response = ApiResponse.buildResponse(BAD_REQUEST, "Status message", "Response object");


        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getStatusMessage()).isEqualTo("Status message");
        assertThat(response.getResponse()).isEqualTo("Response object");
    }
}
