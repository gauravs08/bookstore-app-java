package fi.epassi.recruitment.services;

import fi.epassi.recruitment.api.ApiResponsePage;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.repository.InventoryRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "bookCache")
public class BookService {

    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    //    @Autowired
//    public BookService(BookRepository bookRepository, InventoryRepository inventoryRepository){
//        this.bookRepository=bookRepository;
//        this.inventoryRepository=inventoryRepository;
//    }
    public Mono<UUID> createBook(BookDto bookDto) {
        BookModel bookModel = toBookModel(bookDto);
        //bookModel.setAsNew();
        return bookRepository.save(bookModel)
                .map(BookModel::getIsbn);
    }

    public Mono<Void> deleteBookWithIsbn(@NonNull UUID isbn) {
        return bookRepository.deleteById(isbn)
                .onErrorResume(Exception.class, e -> Mono.error(new BookNotFoundException("ISBN", isbn.toString())));

    }

    @Cacheable(key = "{#isbn}")
    public Mono<BookDto> getBookByIsbn(@NonNull UUID isbn) throws BookNotFoundException {
        return bookRepository.findByIsbn(isbn)
                .map(this::toBookDto);
        //.(Mono.just(new BookNotFoundException(isbn.toString()));
    }

    @Cacheable(key = "{#author, #title, #pageable}")
    public Mono<ApiResponsePage<BookDto>> getBooks(String author, String title, Pageable pageable) {
        Flux<BookModel> booksFlux;

        if (StringUtils.isNotBlank(author) && StringUtils.isNotBlank(title)) {
            booksFlux = bookRepository.findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(author, title, pageable);
        } else if (StringUtils.isNotBlank(author) && StringUtils.isBlank(title)) {
            booksFlux = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        } else if (StringUtils.isNotBlank(title) && StringUtils.isBlank(author)) {
            booksFlux = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            booksFlux = bookRepository.findAllBy(pageable);
        }
        return booksFlux.map(this::toBookDto)
                .collectList()
                .map(books -> {
                    long totalBooks = books.size(); // Assuming no separate count query
                    int totalPages = (int) Math.ceil((double) totalBooks / pageable.getPageSize());
                    return new ApiResponsePage<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), books, totalBooks, totalPages, pageable.getPageNumber(), pageable.getPageSize());
                });
    }

    public Mono<UUID> updateBook(BookDto bookDto) {
        return bookRepository.findByIsbn(bookDto.getIsbn())
                .flatMap(existingBook -> {
                    var bookModel = toBookModel(bookDto);
                    return bookRepository.save(bookModel).map(BookModel::getIsbn);
                })
                .switchIfEmpty(Mono.error(new BookNotFoundException("ISBN", bookDto.getIsbn().toString())));
    }

    public BookModel toBookModel(BookDto bookDto) {
        return BookModel.builder()
                .isbn(bookDto.getIsbn())
                .author(bookDto.getAuthor())
                .title(bookDto.getTitle())
                .price(bookDto.getPrice())
                .build();
    }

    public BookDto toBookDto(BookModel bookModel) {
        return BookDto.builder()
                .isbn(bookModel.getIsbn())
                .author(bookModel.getAuthor())
                .title(bookModel.getTitle())
                .price(bookModel.getPrice())
                .build();
    }
}
