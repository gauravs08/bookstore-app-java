package fi.book.org.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.book.org.dto.BookDto;
import fi.book.org.dto.InventoryDto;
import fi.book.org.dto.InventoryGlobalDto;
import fi.book.org.exception.BookstoreNotFoundException;
import fi.book.org.exception.InventoryNotFoundException;
import fi.book.org.model.BookModel;
import fi.book.org.model.Bookstore;
import fi.book.org.model.Inventory;
import fi.book.org.repository.BookRepository;
import fi.book.org.repository.BookstoreRepository;
import fi.book.org.repository.InventoryRepository;
import fi.book.org.services.InventoryService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private Bookstore bookStore;

    @BeforeEach
    void setUp() {
        isbn = UUID.randomUUID();
        inventory = new Inventory();
        inventory.setId(isbn);
        inventory.setCopies(10);
        inventory.setBookstoreId(100L);

        bookIsbn = UUID.randomUUID();
        bookModel = new BookModel(bookIsbn, "Spring Reactive", "Josh Long", BigDecimal.valueOf(39.99), 100L, true);
        bookStore = new Bookstore(100L, "Address1", "123456789", "bookstore1@example.com", List.of(bookModel), inventory);
        inventory.setBookstoreId(bookStore.getId());
    }

    @Test
    void shouldReturnCopiesByAuthor() {

        when(bookRepository.findByAuthorContainingIgnoreCase(eq("John Doe"), any())).thenReturn(Flux.just(bookModel));
        when(inventoryRepository.findInventoriesById(eq(bookModel.getId()))).thenReturn(Flux.just(inventory));


        Mono<Map<String, Integer>> result = inventoryService.getCopiesByAuthorBookstore("John Doe");

        StepVerifier.create(result)
                .expectNextMatches(map -> map.containsKey(String.valueOf(inventory.getBookstoreId()))
                        && map.containsValue(inventory.getCopies()))
                .verifyComplete();

    }

    @Test
    void shouldReturnCopiesByTitle() {
        when(bookRepository.findByTitleContainingIgnoreCase(eq("The Great Gatsby"), any())).thenReturn(Flux.just(bookModel));
        when(inventoryRepository.findInventoriesById(eq(bookModel.getId()))).thenReturn(Flux.just(inventory));


        Mono<Map<String, Integer>> result = inventoryService.getCopiesByTitleBookstore("The Great Gatsby");

        StepVerifier.create(result)
                .expectNextMatches(map -> map.containsKey(String.valueOf(inventory.getBookstoreId()))
                        && map.containsValue(inventory.getCopies()))
                .verifyComplete();

    }


    @Test
    void shouldReturnInventoryForExistingIsbn() {
        when(inventoryRepository.findInventoriesById(isbn)).thenReturn(Flux.just(inventory));

        Flux<InventoryDto> result = inventoryService.getCopiesByIsbn(isbn);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getId().equals(isbn) && dto.getCopies() == 10)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findInventoriesById(isbn);
    }

    @Test
    void shouldReturnNotFoundForNonExistingInventory() {
        when(inventoryRepository.findInventoriesById(isbn)).thenReturn(Flux.empty());

        Flux<InventoryDto> result = inventoryService.getCopiesByIsbn(isbn);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException)
                .verify();

        verify(inventoryRepository, times(1)).findInventoriesById(isbn);
    }

    @Test
    void shouldUpdateInventorySuccessfully() {
        when(inventoryRepository.findByIdAndBookstoreId(isbn, bookStore.getId())).thenReturn(Mono.just(inventory));
        when(bookstoreRepository.existsById(bookStore.getId())).thenReturn(Mono.just(true));
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(isbn);
        updatedInventory.setCopies(20);
        updatedInventory.setBookstoreId(bookStore.getId());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(Mono.just(updatedInventory));

        Mono<UUID> result = inventoryService.updateInventory(isbn, 20, bookStore.getId());

        StepVerifier.create(result)
                .expectNext(isbn)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByIdAndBookstoreId(isbn, bookStore.getId());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void shouldUpdateInventoryCreateNewSuccessfully() {
        when(inventoryRepository.findByIdAndBookstoreId(isbn, bookStore.getId())).thenReturn(Mono.empty());
        when(bookstoreRepository.existsById(bookStore.getId())).thenReturn(Mono.just(true));
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(isbn);
        updatedInventory.setCopies(20);
        updatedInventory.setBookstoreId(bookStore.getId());

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(Mono.just(updatedInventory));

        Mono<UUID> result = inventoryService.updateInventory(isbn, 20, bookStore.getId());

        StepVerifier.create(result)
                .expectNext(isbn)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findByIdAndBookstoreId(isbn, bookStore.getId());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void shouldReturnErrorWhenUpdatingNonExistingInventory() {
        when(inventoryRepository.findByIdAndBookstoreId(isbn, 1L)).thenReturn(Mono.empty());
        when(bookstoreRepository.existsById(1L)).thenReturn(Mono.just(false));

        Mono<UUID> result = inventoryService.updateInventory(isbn, 20, 1L);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof BookstoreNotFoundException);
                    assertEquals("Bookstore with ID 1 not found.", ((BookstoreNotFoundException) throwable).getBody().getDetail());
                })
                .verify();

        verify(inventoryRepository, times(1)).findByIdAndBookstoreId(isbn, 1L);
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

    @Test
    void shouldUpdateOrCreateInventory() {
        UUID bookId = UUID.randomUUID();
        Long bookstoreId = 1001L;
        BookModel bookModel = new BookModel();
        bookModel.setId(bookId);
        bookModel.setBookstoreId(bookstoreId);

        Inventory existingInventory = Inventory.builder()
                .id(bookId)
                .copies(5)
                .bookstoreId(bookstoreId)
                .isNew(false)
                .build();

        Inventory newInventory = Inventory.builder()
                .id(bookId)
                .copies(1)
                .bookstoreId(bookstoreId)
                .isNew(true)
                .build();

        // Scenario 1: Inventory exists and needs to be updated
        when(inventoryRepository.findInventoriesById(bookId))
                .thenReturn(Flux.just(existingInventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(inventoryService.updateOrCreateInventory(bookModel))
                .expectSubscription()
                .verifyComplete();

        verify(inventoryRepository).findInventoriesById(bookId);
        verify(inventoryRepository).save(argThat(inventory ->
                inventory.getCopies() == 6 && !inventory.isNew()));

        when(inventoryRepository.findInventoriesById(bookId))
                .thenReturn(Flux.empty());
        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(Mono.just(newInventory));


        StepVerifier.create(inventoryService.updateOrCreateInventory(bookModel))
                .expectSubscription()
                .verifyComplete();

        verify(inventoryRepository, times(2)).findInventoriesById(bookId); // Once for update, once for create
        verify(inventoryRepository, times(2)).save(any(Inventory.class)); // Once for each scenario
    }

}
