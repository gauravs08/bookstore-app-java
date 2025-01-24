package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.exception.BookstoreNotFoundException;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.model.Inventory;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.repository.BookstoreRepository;
import fi.epassi.recruitment.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import fi.epassi.recruitment.services.InventoryService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookstoreRepository bookstoreRepository;
    private UUID isbn;
    private Inventory inventory;
    private UUID bookIsbn;
    private BookModel bookModel;
    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        isbn = UUID.randomUUID();
        inventory = new Inventory();
        inventory.setIsbn(isbn);
        inventory.setCopies(10);
        inventory.setBookstoreId(1L);

        bookIsbn = UUID.randomUUID();
        bookModel = new BookModel(bookIsbn, "Spring Reactive", "Josh Long", BigDecimal.valueOf(39.99));

    }
    @Test
    void shouldReturnCopiesByAuthor() {
//        when(bookRepository.findByAuthorContainingIgnoreCase(anyString(), any())).thenReturn(Flux.just(bookModel));
//        when(inventoryRepository.findByIsbn(any())).thenReturn(Flux.just(inventory));

        when(bookRepository.findByAuthorContainingIgnoreCase(eq("John Doe"), any())).thenReturn(Flux.just(bookModel));
        when(inventoryRepository.findByIsbn(eq(bookModel.getIsbn()))).thenReturn(Flux.just(inventory));


        Mono<Map<String, Integer>> result = inventoryService.getCopiesByAuthorBookstore("John Doe");

        StepVerifier.create(result)
                .expectNextMatches(map -> map.containsKey(String.valueOf(inventory.getBookstoreId()))
                        && map.containsValue(inventory.getCopies()))
                .verifyComplete();

    }

    @Test
    void shouldReturnCopiesByTitle() {
//        when(bookRepository.findByAuthorContainingIgnoreCase(anyString(), any())).thenReturn(Flux.just(bookModel));
//        when(inventoryRepository.findByIsbn(any())).thenReturn(Flux.just(inventory));

        when(bookRepository.findByTitleContainingIgnoreCase(eq("The Great Gatsby"), any())).thenReturn(Flux.just(bookModel));
        when(inventoryRepository.findByIsbn(eq(bookModel.getIsbn()))).thenReturn(Flux.just(inventory));


        Mono<Map<String, Integer>> result = inventoryService.getCopiesByTitleBookstore("The Great Gatsby");

        StepVerifier.create(result)
                .expectNextMatches(map -> map.containsKey(String.valueOf(inventory.getBookstoreId()))
                        && map.containsValue(inventory.getCopies()))
                .verifyComplete();

    }


    @Test
    void shouldReturnInventoryForExistingIsbn() {
        when(inventoryRepository.findByIsbn(isbn)).thenReturn(Flux.just(inventory));

        Flux<InventoryDto> result = inventoryService.getCopiesByIsbn(isbn);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getIsbn().equals(isbn) && dto.getCopies() == 10)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByIsbn(isbn);
    }

    @Test
    void shouldReturnNotFoundForNonExistingInventory() {
        when(inventoryRepository.findByIsbn(isbn)).thenReturn(Flux.empty());

        Flux<InventoryDto> result = inventoryService.getCopiesByIsbn(isbn);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException)
                .verify();

        verify(inventoryRepository, times(1)).findByIsbn(isbn);
    }

    @Test
    void shouldUpdateInventorySuccessfully() {
        when(inventoryRepository.findByIsbnAndBookstoreId(isbn, 1L)).thenReturn(Mono.just(inventory));
        when(bookstoreRepository.existsById(inventory.getBookstoreId())).thenReturn(Mono.just(true));
        // Simulate updating the inventory copies
        Inventory updatedInventory = new Inventory();
        updatedInventory.setIsbn(isbn);
        updatedInventory.setCopies(20);
        updatedInventory.setBookstoreId(1L);

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(Mono.just(updatedInventory));

        Mono<UUID> result = inventoryService.updateInventory(isbn, 20, 1L);

        StepVerifier.create(result)
                .expectNext(isbn)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByIsbnAndBookstoreId(isbn, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void shouldUpdateInventoryCreateNewSuccessfully() {
        when(inventoryRepository.findByIsbnAndBookstoreId(isbn, 1L)).thenReturn(Mono.empty());
        when(bookstoreRepository.existsById(inventory.getBookstoreId())).thenReturn(Mono.just(true));
        // Simulate updating the inventory copies
        Inventory updatedInventory = new Inventory();
        updatedInventory.setIsbn(isbn);
        updatedInventory.setCopies(20);
        updatedInventory.setBookstoreId(1L);

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(Mono.just(updatedInventory));

        Mono<UUID> result = inventoryService.updateInventory(isbn, 20, 1L);

        StepVerifier.create(result)
                .expectNext(isbn)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByIsbnAndBookstoreId(isbn, 1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void shouldReturnErrorWhenUpdatingNonExistingInventory() {
        when(inventoryRepository.findByIsbnAndBookstoreId(isbn, 1L)).thenReturn(Mono.empty());
        when(bookstoreRepository.existsById(1L)).thenReturn(Mono.just(false));

        Mono<UUID> result = inventoryService.updateInventory(isbn, 20, 1L);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof BookstoreNotFoundException);
                    assertEquals("Bookstore with ID 1 not found.", ((BookstoreNotFoundException) throwable).getBody().getDetail());
                })
                .verify();

        // Ensure correct method invocations
        verify(inventoryRepository, times(1)).findByIsbnAndBookstoreId(isbn, 1L);
        verify(bookstoreRepository, times(1)).existsById(1L);
    }



    @Test
    void shouldReturnTotalCopies() {
        when(inventoryRepository.findAll()).thenReturn(Flux.just(inventory));

        Mono<InventoryGlobalDto> result = inventoryService.getTotalCopies();

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getTotal_copies().equals(10L))
                .verifyComplete();

        verify(inventoryRepository, times(1)).findAll();
    }


}
