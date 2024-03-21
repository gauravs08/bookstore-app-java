package fi.epassi.recruitment.book;

import fi.epassi.recruitment.exception.BookNotFoundException;
import java.util.List;
import java.util.UUID;

import fi.epassi.recruitment.inventory.InventoryRepository;
import fi.epassi.recruitment.inventory.InventoryService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final InventoryService inventoryService;

    public UUID createBook(BookDto bookDto) {
        BookModel bookModel = toBookModel(bookDto);
        var savedBook = bookRepository.save(bookModel);
        inventoryService.saveOrUpdateInventory(savedBook.getIsbn(),1);
        return savedBook.getIsbn();
    }

    public void deleteBookWithIsbn(@NonNull UUID isbn) {
        bookRepository.deleteById(isbn);
    }

    public BookDto getBookByIsbn(@NonNull UUID isbn) throws BookNotFoundException {
        return bookRepository.findByIsbn(isbn)
            .map(BookService::toBookDto)
            .orElseThrow(() -> new BookNotFoundException(isbn.toString()));
    }


    public Page<BookDto> getBooks(String author, String title, Pageable pageable) {
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

        return bookRepository.findAll(pageable).map(BookService::toBookDto);
    }

    public UUID updateBook(BookDto bookDto) {
        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            var bookModel = toBookModel(bookDto);
            var savedBook = bookRepository.save(bookModel);
            return savedBook.getIsbn();
        }

        throw new BookNotFoundException(bookDto.getIsbn().toString());
    }

    private static BookModel toBookModel(BookDto bookDto) {
        return BookModel.builder()
            .isbn(bookDto.getIsbn())
            .author(bookDto.getAuthor())
            .title(bookDto.getTitle())
            .price(bookDto.getPrice())
            .build();
    }

    private static BookDto toBookDto(BookModel bookModel) {
        return BookDto.builder()
            .isbn(bookModel.getIsbn())
            .author(bookModel.getAuthor())
            .title(bookModel.getTitle())
            .price(bookModel.getPrice())
            .build();
    }
}
