package fi.book.org.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

import fi.book.org.api.ApiResponsePage;
import fi.book.org.dto.BookDto;
import fi.book.org.exception.BookCreateException;
import fi.book.org.exception.BookNotFoundException;
import fi.book.org.model.BookModel;
import fi.book.org.repository.BookRepository;
import fi.book.org.repository.BookstoreRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "bookCache")
public class BookService {

    private final BookRepository bookRepository;
    private final BookstoreRepository bookstoreRepository;
    private final InventoryService inventoryService;


    public Mono<UUID> createBook(BookDto bookDto) {
        BookModel bookModel = toBookModel(bookDto);
        bookModel.setNew(true);
        return bookRepository.save(bookModel)
                .flatMap(savedBook -> inventoryService.updateOrCreateInventory(savedBook)
                        .thenReturn(savedBook.getId()))
                .onErrorResume(e -> {
                    log.error("Error creating book with ISBN {}: {}", bookDto.getId(), e.getMessage(), e);
                    return Mono.error(new BookCreateException("ISBN", bookDto.getId().toString()));
                });
    }


    public Mono<Void> deleteBookWithIsbn(@NonNull UUID id) {
        return bookRepository.deleteById(id)
                .onErrorResume(e -> {
                    log.error("Error deleting book with ISBN {}: {}", id, e.getMessage(), e);
                    return Mono.error(new BookNotFoundException("ISBN", id.toString()));
                });

    }

    @Cacheable(key = "{#id}")
    public Mono<BookDto> getBookByIsbn(@NonNull UUID id) throws BookNotFoundException {
        return bookRepository.findById(id)
                .map(this::toBookDto)
                .switchIfEmpty(Mono.error(new BookNotFoundException("ISBN", id.toString())));
    }

    @Cacheable(key = "{#author, #title, #bookstoreId, #pageable}")
    public Mono<ApiResponsePage<BookDto>> getBooks(String author, String title, Long bookstoreId, Pageable pageable) {
        Flux<BookModel> booksFlux;

        if (StringUtils.isNotBlank(author) && StringUtils.isNotBlank(title)) {
            booksFlux = bookRepository.findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(author, title, pageable);
        } else if (StringUtils.isNotBlank(author) && StringUtils.isBlank(title)) {
            booksFlux = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        } else if (StringUtils.isNotBlank(title) && StringUtils.isBlank(author)) {
            booksFlux = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (bookstoreId != null) {
            booksFlux = bookRepository.findByBookstoreId(bookstoreId, pageable);
        } else {
            booksFlux = bookRepository.findAllBy(pageable);
        }
        return booksFlux.map(this::toBookDto)
                .collectList()
                .map(books -> {
                    long totalBooks = books.size();
                    int totalPages = (int) Math.ceil((double) totalBooks / pageable.getPageSize());
                    return new ApiResponsePage<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), books, totalBooks, totalPages, pageable.getPageNumber(), pageable.getPageSize());
                });
    }

    public Mono<UUID> updateBook(BookDto bookDto) {
        return bookRepository.findById(bookDto.getId())
                .flatMap(existingBook -> {
                    var bookModel = toBookModel(bookDto);
                    bookModel.setNew(false);
                    return bookRepository.save(bookModel).map(BookModel::getId);
                })
                .switchIfEmpty(Mono.error(new BookNotFoundException("ISBN", bookDto.getId().toString())));
    }

    public BookModel toBookModel(BookDto bookDto) {
        return BookModel.builder()
                .id(bookDto.getId())
                .author(bookDto.getAuthor())
                .title(bookDto.getTitle())
                .price(bookDto.getPrice())
                .bookstoreId(bookDto.getBookstoreId())
                .build();
    }

    public BookDto toBookDto(BookModel bookModel) {
        return BookDto.builder()
                .id(bookModel.getId())
                .author(bookModel.getAuthor())
                .title(bookModel.getTitle())
                .price(bookModel.getPrice())
                .bookstoreId(bookModel.getBookstoreId())
                .build();
    }
}
