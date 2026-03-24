package fi.book.org.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponsePageTest {

    @Test
    void shouldCreatePagedResponse() {
        var response = ApiResponsePage.okWithPagination(
                List.of("item1", "item2"), 10, 5, 0, 2);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(5);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(2);
    }

    @Test
    void shouldCreatePagedResponseWithEmptyList() {
        var response = ApiResponsePage.okWithPagination(
                List.of(), 0, 0, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldCreateWithConstructor() {
        var response = new ApiResponsePage<>(200, "OK",
                List.of("a", "b", "c"), 100, 10, 2, 10);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusMessage()).isEqualTo("OK");
        assertThat(response.getResponse()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(100);
        assertThat(response.getTotalPages()).isEqualTo(10);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getPageSize()).isEqualTo(10);
    }
}
