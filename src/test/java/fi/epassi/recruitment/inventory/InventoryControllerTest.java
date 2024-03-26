package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.controller.InventoryController;
import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.model.Inventory;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryControllerTest {

    @Mock
    InventoryService inventoryService;

    @InjectMocks
    InventoryController inventoryController;

    @Test
    void testGetInventoryCopiesByIsbn() {
        UUID isbn = UUID.randomUUID();
        InventoryDto inventoryDto = new InventoryDto(isbn,"Title","Author",10,1001L);
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
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

//    @Test
//    void testGetInventoryTotalCopies() {
//        Map<String, Integer> inventoryMap = new HashMap<>();
//        inventoryMap.put("Book1", 10);
//        inventoryMap.put("Book2", 20);
//        ApiResponse<InventoryGlobalDto> expectedResponse = ApiResponse.ok(new InventoryDto(inventoryMap));
//        when(inventoryService.getTotalCopies())
//                .thenReturn(Mono.just(inventoryMap));
//
//        Mono<ApiResponse<InventoryGlobalDto>> response = inventoryController.getInventoryTotalCopies();
//
//        StepVerifier.create(response)
//                .expectNextMatches(apiResponse -> apiResponse.getStatusCode() == 200 &&
//                        apiResponse.getResponse().equals(expectedResponse.getResponse()))
//                .expectComplete()
//                .verify();
//    }



}
