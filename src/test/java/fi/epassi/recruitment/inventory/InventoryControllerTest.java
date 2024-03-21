package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.BaseIntegrationTest;
import fi.epassi.recruitment.book.BookModel;
import fi.epassi.recruitment.book.BookRepository;
import fi.epassi.recruitment.inventory.InventoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryControllerTest extends BaseIntegrationTest {

    private static final String BASE_PATH_V1_INVENTORY = "/api/v1/inventory";
    private static final UUID EXAMPLE_ISBN = UUID.randomUUID();
    private static final String EXAMPLE_AUTHOR = "J.R.R. Tolkien";
    private static final String EXAMPLE_TITLE = "The Hobbit";
    private static final UUID NON_EXISTING_ISBN = UUID.randomUUID();
    private static final String NON_EXISTING_AUTHOR = "Non Existing Author";
    private static final String NON_EXISTING_TITLE = "Non Existing Title";
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldGetInventoryCopiesByIsbn() throws Exception {
        // Given
        inventoryRepository.save(new InventoryModel(EXAMPLE_ISBN, 10));

        // When
        ResultActions result = mvc.perform(get(BASE_PATH_V1_INVENTORY + "/isbn/{isbn}/copies", EXAMPLE_ISBN)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.response.isbn").value(EXAMPLE_ISBN.toString()))
                .andExpect(jsonPath("$.response.copies").value(10));
    }

    @Test
    void shouldGetInventoryCopiesByAuthor() throws Exception {
        // Given
        bookRepository.save(new BookModel(EXAMPLE_ISBN,EXAMPLE_TITLE,EXAMPLE_AUTHOR,new BigDecimal(15.0)));
        inventoryRepository.save(new InventoryModel(EXAMPLE_ISBN, 5)); // 5 copies for the example author

        // When
        ResultActions result = mvc.perform(get(BASE_PATH_V1_INVENTORY + "/author/{author}/copies", EXAMPLE_AUTHOR)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.response.author").value(EXAMPLE_AUTHOR))
                .andExpect(jsonPath("$.response.copies").value(5));
    }

    @Test
    void shouldGetInventoryCopiesByTitle() throws Exception {
        // Given
        bookRepository.save(new BookModel(EXAMPLE_ISBN,EXAMPLE_TITLE,EXAMPLE_AUTHOR,new BigDecimal(15.0)));
        inventoryRepository.save(new InventoryModel(EXAMPLE_ISBN, 7)); // 7 copies for the example title

        // When
        ResultActions result = mvc.perform(get(BASE_PATH_V1_INVENTORY + "/title/{title}/copies", EXAMPLE_TITLE)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.response.title").value(EXAMPLE_TITLE))
                .andExpect(jsonPath("$.response.copies").value(7));
    }


    @Test
    void shouldHandleInventoryNotFoundExceptionWhenGettingInventoryCopiesByIsbn() throws Exception {
        // When
        ResultActions result = mvc.perform(get(BASE_PATH_V1_INVENTORY + "/isbn/{isbn}/copies", NON_EXISTING_ISBN)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("No inventory found with ISBN {%s}".formatted(NON_EXISTING_ISBN)));
    }

    @Test
    void shouldHandleInventoryNotFoundExceptionWhenGettingInventoryCopiesByAuthor() throws Exception {
        // When
        ResultActions result = mvc.perform(get(BASE_PATH_V1_INVENTORY + "/author/{author}/copies", NON_EXISTING_AUTHOR)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("No inventory found with ISBN {%s}".formatted(NON_EXISTING_ISBN)));

    }

    @Test
    void shouldHandleInventoryNotFoundExceptionWhenGettingInventoryCopiesByTitle() throws Exception {
        // When
        ResultActions result = mvc.perform(get(BASE_PATH_V1_INVENTORY + "/title/{title}/copies", NON_EXISTING_TITLE)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("No inventory found with ISBN {%s}".formatted(NON_EXISTING_ISBN)));

    }
}
