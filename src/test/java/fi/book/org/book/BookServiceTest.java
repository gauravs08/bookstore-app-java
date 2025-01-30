package fi.book.org.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import fi.book.org.api.ApiResponsePage;
import fi.book.org.dto.BookDto;
import fi.book.org.exception.BookCreateException;
import fi.book.org.exception.BookNotFoundException;
import fi.book.org.model.BookModel;
import fi.book.org.model.Bookstore;
import fi.book.org.model.Inventory;
import fi.book.org.repository.BookRepository;
import fi.book.org.repository.InventoryRepository;
import fi.book.org.services.BookService;
import fi.book.org.services.InventoryService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private BookService bookService;
    @Mock
    private InventoryService inventoryService;

    private UUID bookIsbn;
    private BookModel bookModel;
    private BookDto bookDto;
    private Bookstore bookStore;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        bookIsbn = UUID.randomUUID();
        inventory = new Inventory();
        inventory.setId(bookIsbn);
        inventory.setCopies(10);

        bookModel = new BookModel(bookIsbn, "Spring Reactive", "Josh Long", BigDecimal.valueOf(39.99), 100L, true);
        bookStore = new Bookstore(100L, "Address1", "123456789", "bookstore1@example.com", List.of(bookModel), inventory);

        bookDto = BookDto.builder()
                .id(bookIsbn)
                .title("Spring Reactive")
                .author("Josh Long")
                .price(BigDecimal.valueOf(39.99))
                .build();
    }

    @Test
    void shouldGetBooks() {
        when(bookRepository.findAllBy(any())).thenReturn(Flux.just(bookModel));

        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(null, null, null, PageRequest.of(1, 1, Sort.unsorted()));

        assertNotNull(result);
        result.block();
        verify(bookRepository, times(1)).findAllBy(any());
    }

    @Test
    public void testGetBooks_withValidAuthorAndTitle() {
        String author = "Some Author";
        String title = "Some Title";
        Pageable pageable = PageRequest.of(0, 10);

        BookModel bookModel = new BookModel();
        bookModel.setAuthor(author);
        bookModel.setTitle(title);

        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(bookModels);

        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(author, title, 1000L, pageable);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(author, title, pageable);
    }

    @Test
    public void testGetBooks_withValidAuthor() {
        String author = "Some Author";
        String title = "Some Title";
        Pageable pageable = PageRequest.of(0, 10);

        BookModel bookModel = new BookModel();
        bookModel.setAuthor(author);
        bookModel.setTitle(title);

        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByAuthorContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(bookModels);

        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(author, null, 1000L, pageable);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCase(author, pageable);
    }

    @Test
    public void testGetBooks_withValidTitle() {
        String author = "Some Author";
        String title = "Some Title";
        Pageable pageable = PageRequest.of(0, 10);

        BookModel bookModel = new BookModel();
        bookModel.setAuthor(author);
        bookModel.setTitle(title);

        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(bookModels);

        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(null, title, null, pageable);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(title, pageable);
    }

    @Test
    public void testGetBooks_withValidBookStoreId() {
        Pageable pageable = PageRequest.of(0, 10);

        BookModel bookModel = new BookModel();
        bookModel.setAuthor("Some Author");
        bookModel.setTitle("Some Title");
        bookModel.setBookstoreId(1000L);

        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByBookstoreId(anyLong(), any(Pageable.class)))
                .thenReturn(bookModels);

        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(null, null, 1000L, pageable);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        verify(bookRepository, times(1)).findByBookstoreId(1000L, pageable);
    }

    @Test
    void shouldCreateBookWithInventoryPresent() {
        when(bookRepository.save(any(BookModel.class))).thenReturn(Mono.just(bookModel));
        when(inventoryService.updateOrCreateInventory(any(BookModel.class))).thenReturn(Mono.empty());
        Mono<UUID> result = bookService.createBook(bookDto);

        assertEquals(bookIsbn, result.block());
        verify(bookRepository, times(1)).save(any(BookModel.class));
    }

    @Test
    void shouldHandleExceptionWhenCreatingBook() {
        UUID bookIsbn = UUID.randomUUID();
        BookDto bookDto = new BookDto(bookIsbn, "Reactive Spring", "Josh Long", BigDecimal.valueOf(49.99), 1001L);

        when(bookRepository.save(any(BookModel.class)))
                .thenReturn(Mono.error(new RuntimeException("Simulated database error")));

        StepVerifier.create(bookService.createBook(bookDto))
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(BookCreateException.class, throwable);
                    assertThat(throwable.getMessage().contains("Exception to create or update book with ISBN:"));
                    assertEquals(HttpStatus.BAD_REQUEST, ((BookCreateException) throwable).getStatusCode());
                })
                .verify();
    }

    @Test
    void shouldUpdateBook() {
        when(bookRepository.findById(bookIsbn)).thenReturn(Mono.just(bookModel));
        when(bookRepository.save(any(BookModel.class))).thenReturn(Mono.just(bookModel));

        Mono<UUID> result = bookService.updateBook(bookDto);

        assertEquals(bookIsbn, result.block());
        verify(bookRepository, times(1)).save(any(BookModel.class));
    }

    @Test
    void shouldGetBookByIsbn() {
        when(bookRepository.findById(bookIsbn)).thenReturn(Mono.just(bookModel));

        Mono<BookDto> result = bookService.getBookByIsbn(bookIsbn);

        assertNotNull(result.block());
        assertEquals("Spring Reactive", result.block().getTitle());
        verify(bookRepository, times(1)).findById(bookIsbn);
    }

    @Test
    void shouldDeleteBookByIsbn() {
        when(bookRepository.deleteById(bookIsbn)).thenReturn(Mono.empty());

        Mono<Void> result = bookService.deleteBookWithIsbn(bookIsbn);

        assertNull(result.block());
        verify(bookRepository, times(1)).deleteById(bookIsbn);
    }

    @Test
    void shouldHandleExceptionWhenDeletingBook() {
        UUID bookIsbn = UUID.randomUUID();

        when(bookRepository.deleteById(bookIsbn))
                .thenReturn(Mono.error(new RuntimeException("Simulated delete error")));

        StepVerifier.create(bookService.deleteBookWithIsbn(bookIsbn))
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(BookNotFoundException.class, throwable);
                    assertThat(throwable.getMessage().contains("Error deleting book with ISBN:"));
                })
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(bookIsbn)).thenThrow(BookNotFoundException.class);

        assertThrows(RuntimeException.class, () -> bookService.getBookByIsbn(bookIsbn).block());
        verify(bookRepository, times(1)).findById(bookIsbn);
    }

    @Test
    void shouldGetBookByIsbnUsingStepVerifier() {
        when(bookRepository.findById(bookIsbn)).thenReturn(Mono.just(bookModel));

        StepVerifier.create(bookService.getBookByIsbn(bookIsbn))
                .expectNextMatches(dto -> dto.getTitle().equals("Spring Reactive"))
                .verifyComplete();
    }

}
