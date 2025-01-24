package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.controller.InventoryController;
import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.exception.BookstoreNotFoundException;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.services.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
public class InventoryControllerTest {

    @Mock
    InventoryService inventoryService;

    @InjectMocks
    InventoryController inventoryController;

    @Test
    void testGetInventoryCopiesByIsbn() {
        UUID isbn = UUID.randomUUID();
        InventoryDto inventoryDto = new InventoryDto(isbn, "Title", "Author", 10, 1001L);
        when(inventoryService.getCopiesByIsbn(isbn))
                .thenReturn(Flux.just(inventoryDto));

        Flux<ApiResponse<InventoryDto>> response = inventoryController.getInventoryCopiesByIsbn(isbn);


        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == 200 &&
                        apiResponse.getResponse().equals(inventoryDto))
                .expectComplete()
                .verify();
    }

    @Test
    void testGetInventoryCopiesByAuthor() {

        String author = "Author";
        Map<String, Integer> inventoryMap = new HashMap<>();
        inventoryMap.put("Book1", 10);
        inventoryMap.put("Book2", 20);
        when(inventoryService.getCopiesByAuthorBookstore(author))
                .thenReturn(Mono.just(inventoryMap));


        Mono<ApiResponse<Map<String, Integer>>> response = inventoryController.getInventoryCopiesByAuthor(author);


        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == 200 &&
                        apiResponse.getResponse().equals(inventoryMap))
                .expectComplete()
                .verify();
    }


    @Test
    void testGetInventoryCopiesByTitle() {
        String title = "Title";
        Map<String, Integer> inventoryMap = new HashMap<>();
        inventoryMap.put("Book1", 10);
        inventoryMap.put("Book2", 20);
        when(inventoryService.getCopiesByTitleBookstore(title))
                .thenReturn(Mono.just(inventoryMap));

        Mono<ApiResponse<Map<String, Integer>>> response = inventoryController.getInventoryCopiesByTitle(title);

        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == 200 &&
                        apiResponse.getResponse().equals(inventoryMap))
                .expectComplete()
                .verify();
    }

    @Test
    void testUpdateInventoriesCopiesByIsbn() {
        UUID isbn = UUID.randomUUID();
        int copies = 10;
        Long bookstoreId = 1001L;
        ApiResponse<UUID> expectedResponse = ApiResponse.ok(isbn);
        when(inventoryService.updateInventory(isbn, copies, bookstoreId))
                .thenReturn(Mono.just(isbn));

        Mono<ApiResponse<UUID>> response = inventoryController.updateInventoriesCopiesByIsbn(isbn, copies, bookstoreId);

        StepVerifier.create(response)
                //.expectNext(expectedResponse)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == 200
                        && apiResponse.getStatusMessage().equals("OK")
                && apiResponse.getResponse().equals(isbn))
                .expectComplete()
                .verify();
    }

    @Test
    void testGetInventoryTotalCopies() {

        InventoryGlobalDto inventoryGlobalDto = new InventoryGlobalDto(30L);
        when(inventoryService.getTotalCopies())
                .thenReturn(Mono.just(inventoryGlobalDto));

        Mono<ApiResponse<InventoryGlobalDto>> response = inventoryController.getInventoryTotalCopies();

        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == 200 &&
                        apiResponse.getResponse().equals(inventoryGlobalDto))
                .expectComplete()
                .verify();
    }

    @Test
    void testGetInventoryCopiesByIsbn_NotFound() {
        UUID isbn = UUID.randomUUID();
        when(inventoryService.getCopiesByIsbn(isbn))
                .thenReturn(Flux.error(new InventoryNotFoundException("ISBN", isbn.toString())));

        Flux<ApiResponse<InventoryDto>> response = inventoryController.getInventoryCopiesByIsbn(isbn);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof InventoryNotFoundException &&
                                throwable.getMessage().contains("No inventory found with ISBN: " + isbn))
                .verify();
    }

    @Test
    void testGetInventoryCopiesByAuthor_NotFound() {
        String author = "UnknownAuthor";
        when(inventoryService.getCopiesByAuthorBookstore(author))
                .thenReturn(Mono.error(new InventoryNotFoundException("Author", author)));


        Mono<ApiResponse<Map<String, Integer>>> response = inventoryController.getInventoryCopiesByAuthor(author);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof InventoryNotFoundException &&
                                throwable.getMessage().contains("No inventory found with Author: " + author))
                .verify();
    }

    @Test
    void testGetInventoryCopiesByTitle_NotFound() {
        String title = "UnknownTitle";
        when(inventoryService.getCopiesByTitleBookstore(title))
                .thenReturn(Mono.error(new InventoryNotFoundException("Title", title)));


        Mono<ApiResponse<Map<String, Integer>>> response = inventoryController.getInventoryCopiesByTitle(title);

        // Assert
        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof InventoryNotFoundException &&
                                throwable.getMessage().contains("No inventory found with Title: " + title))
                .verify();
    }

    @Test
    void testUpdateInventoriesCopiesByIsbn_BookstoreNotFound() {
        UUID isbn = UUID.randomUUID();
        int copies = 10;
        Long bookstoreId = 1001L;
        when(inventoryService.updateInventory(isbn, copies, bookstoreId))
                .thenReturn(Mono.error(new BookstoreNotFoundException("Bookstore not found")));

        Mono<ApiResponse<UUID>> response = inventoryController.updateInventoriesCopiesByIsbn(isbn, copies, bookstoreId);

        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == NOT_FOUND.value())
                .expectComplete()
                .verify();
    }

    @Test
    void testUpdateInventoriesCopiesByIsbn_InventoryNotFound() {
        UUID isbn = UUID.randomUUID();
        int copies = 10;
        Long bookstoreId = 1001L;
        when(inventoryService.updateInventory(isbn, copies, bookstoreId))
                .thenReturn(Mono.error(new InventoryNotFoundException("ISBN", isbn.toString())));

        Mono<ApiResponse<UUID>> response = inventoryController.updateInventoriesCopiesByIsbn(isbn, copies, bookstoreId);

        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == NOT_FOUND.value())
                .expectComplete()
                .verify();
    }

    @Test
    void testUpdateInventoriesCopiesByIsbn_InternalServerError() {
        UUID isbn = UUID.randomUUID();
        int copies = 10;
        Long bookstoreId = 1001L;
        when(inventoryService.updateInventory(isbn, copies, bookstoreId))
                .thenReturn(Mono.error(new RuntimeException("Internal server error")));

        Mono<ApiResponse<UUID>> response = inventoryController.updateInventoriesCopiesByIsbn(isbn, copies, bookstoreId);

        StepVerifier.create(response)
                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == INTERNAL_SERVER_ERROR.value())
                .expectComplete()
                .verify();
    }


}
