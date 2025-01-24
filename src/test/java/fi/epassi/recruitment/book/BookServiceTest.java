package fi.epassi.recruitment.book;

import fi.epassi.recruitment.api.ApiResponsePage;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.repository.BookRepository;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fi.epassi.recruitment.services.BookService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private UUID bookIsbn;
    private BookModel bookModel;
    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        bookIsbn = UUID.randomUUID();
        bookModel = new BookModel(bookIsbn, "Spring Reactive", "Josh Long", BigDecimal.valueOf(39.99));

        bookDto = BookDto.builder()
                .isbn(bookIsbn)
                .title("Spring Reactive")
                .author("Josh Long")
                .price(BigDecimal.valueOf(39.99))
                .build();
    }

    @Test
    void shouldGetBooks() {
        when(bookRepository.findAllBy(any())).thenReturn(Flux.just(bookModel));

        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(null, null, PageRequest.of(1, 1, Sort.unsorted()));

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
        bookModel.setAuthor("Some Author");
        bookModel.setTitle("Some Title");

        // Create a Flux of BookModel to simulate repository response
        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(bookModels);

        // Calling the method under test
        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(author, title, pageable);

        // Verifying the result using StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        // Verifying that the repository method was called with the correct arguments
        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(author, title, pageable);
    }

    @Test
    public void testGetBooks_withValidAuthor() {
        String author = "Some Author";
        String title = "Some Title";
        Pageable pageable = PageRequest.of(0, 10);

        BookModel bookModel = new BookModel();
        bookModel.setAuthor("Some Author");
        bookModel.setTitle("Some Title");

        // Create a Flux of BookModel to simulate repository response
        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByAuthorContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(bookModels);

        // Calling the method under test
        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(author, null, pageable);

        // Verifying the result using StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        // Verifying that the repository method was called with the correct arguments
        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCase(author, pageable);
    }

    @Test
    public void testGetBooks_withValidTitle() {
        String author = "Some Author";
        String title = "Some Title";
        Pageable pageable = PageRequest.of(0, 10);

        BookModel bookModel = new BookModel();
        bookModel.setAuthor("Some Author");
        bookModel.setTitle("Some Title");

        // Create a Flux of BookModel to simulate repository response
        Flux<BookModel> bookModels = Flux.just(bookModel);
        when(bookRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(bookModels);

        // Calling the method under test
        Mono<ApiResponsePage<BookDto>> result = bookService.getBooks(null, title, pageable);

        // Verifying the result using StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getResponse().size() == 1)
                .verifyComplete();

        // Verifying that the repository method was called with the correct arguments
        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(title, pageable);
    }

    @Test
    void shouldCreateBook() {
        when(bookRepository.save(any(BookModel.class))).thenReturn(Mono.just(bookModel));

        Mono<UUID> result = bookService.createBook(bookDto);

        assertEquals(bookIsbn, result.block());
        verify(bookRepository, times(1)).save(any(BookModel.class));
    }

    @Test
    void shouldUpdateBook() {
        when(bookRepository.findByIsbn(bookIsbn)).thenReturn(Mono.just(bookModel));
        when(bookRepository.save(any(BookModel.class))).thenReturn(Mono.just(bookModel));

        Mono<UUID> result = bookService.updateBook(bookDto);

        assertEquals(bookIsbn, result.block());
        verify(bookRepository, times(1)).save(any(BookModel.class));
    }

    @Test
    void shouldGetBookByIsbn() {
        when(bookRepository.findByIsbn(bookIsbn)).thenReturn(Mono.just(bookModel));

        Mono<BookDto> result = bookService.getBookByIsbn(bookIsbn);

        assertNotNull(result.block());
        assertEquals("Spring Reactive", result.block().getTitle());
        verify(bookRepository, times(1)).findByIsbn(bookIsbn);
    }

    @Test
    void shouldDeleteBookByIsbn() {
        when(bookRepository.deleteById(bookIsbn)).thenReturn(Mono.empty());

        Mono<Void> result = bookService.deleteBookWithIsbn(bookIsbn);

        assertNull(result.block());
        verify(bookRepository, times(1)).deleteById(bookIsbn);
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findByIsbn(bookIsbn)).thenThrow(BookNotFoundException.class);

        assertThrows(RuntimeException.class, () -> bookService.getBookByIsbn(bookIsbn).block());
        verify(bookRepository, times(1)).findByIsbn(bookIsbn);
    }
    @Test
    void shouldGetBookByIsbnUsingStepVerifier() {
        when(bookRepository.findByIsbn(bookIsbn)).thenReturn(Mono.just(bookModel));

        StepVerifier.create(bookService.getBookByIsbn(bookIsbn))
                .expectNextMatches(dto -> dto.getTitle().equals("Spring Reactive"))
                .verifyComplete();
    }

}
