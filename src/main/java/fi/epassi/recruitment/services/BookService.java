package fi.epassi.recruitment.services;

import fi.epassi.recruitment.model.Books;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.exception.BookNotFoundException;

import java.util.UUID;

import fi.epassi.recruitment.repository.InventoryRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "bookCache")
public class BookService {

    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

//    public BookService(BookRepository bookRepository, InventoryRepository inventoryRepository){
//        this.bookRepository=bookRepository;
//        this.inventoryRepository=inventoryRepository;
//    }
    public UUID createBook(BookDto bookDto) {
        Books books = toBookModel(bookDto);
        var savedBook = bookRepository.save(books).block();
        //inventoryRepository.saveOrUpdateInventory(savedBook.getIsbn(),1);
        return savedBook.getIsbn();
    }

    public void deleteBookWithIsbn(@NonNull UUID isbn) {
        bookRepository.deleteById(isbn);
    }

    @Cacheable(key = "{#isbn}")
    public Mono<BookDto> getBookByIsbn(@NonNull UUID isbn) throws BookNotFoundException {
        return bookRepository.findByIsbn(isbn)
            .map(BookService::toBookDto);
            //.(Mono.just(new BookNotFoundException(isbn.toString()));
    }

    @Cacheable(key = "{#author, #title, #pageable}")
    public Flux<BookDto> getBooks(String author, String title, Pageable pageable) {
        if (StringUtils.isNotBlank(author) && StringUtils.isNotBlank(title)) {
            return bookRepository.findByAuthorAndTitle(author, title, pageable)
                    //.stream()
                    .map(BookService::toBookDto);
                    //.toList());
        } else if (StringUtils.isNotBlank(author) && StringUtils.isBlank(title)) {
            return bookRepository.findByAuthor(author, pageable)
                    //.stream()
                    .map(BookService::toBookDto);
        } else if (StringUtils.isNotBlank(title) && StringUtils.isBlank(author)) {
            return bookRepository.findByTitle(title, pageable)
                    //.stream()
                    .map(BookService::toBookDto);
                    //.toList());
        }

        return bookRepository.findAllBy(pageable).map(BookService::toBookDto);
    }

    public UUID updateBook(BookDto bookDto) {
        if (Boolean.TRUE.equals(bookRepository.findByIsbn(bookDto.getIsbn()).hasElement().block())) {
            var bookModel = toBookModel(bookDto);
            var savedBook = bookRepository.save(bookModel).block();
            return savedBook.getIsbn();
        }

        throw new BookNotFoundException(bookDto.getIsbn().toString());
    }

    private static Books toBookModel(BookDto bookDto) {
        return Books.builder()
            .isbn(bookDto.getIsbn())
            .author(bookDto.getAuthor())
            .title(bookDto.getTitle())
            .price(bookDto.getPrice())
            .build();
    }

    private static BookDto toBookDto(Books books) {
        return BookDto.builder()
            .isbn(books.getIsbn())
            .author(books.getAuthor())
            .title(books.getTitle())
            .price(books.getPrice())
            .build();
    }
}
