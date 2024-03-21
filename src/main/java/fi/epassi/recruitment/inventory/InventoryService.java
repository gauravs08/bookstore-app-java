package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.book.BookDto;
import fi.epassi.recruitment.book.BookModel;
import fi.epassi.recruitment.book.BookRepository;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.exception.InventoryUpdateException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final BookRepository bookRepository;

    private final InventoryRepository inventoryRepository;
    // Get copies by author
    public InventoryDto getCopiesByAuthor(String author) {
        List<BookModel> books = bookRepository.findByAuthor(author);
        int totalCopies = 0;
        for (BookModel book : books) {
            InventoryModel inventory = inventoryRepository.findByIsbn(book.getIsbn())
                    .orElseThrow(() -> new InventoryNotFoundException(book.getIsbn().toString()));
            totalCopies += inventory.getCopies();
        }
        return InventoryDto.builder().author(author)
                .copies(totalCopies)
                .build();
    }

    // Get copies by title
    public InventoryDto getCopiesByTitle(String title) {
        List<BookModel> books = bookRepository.findByTitle(title);
        int totalCopies = 0;
        for (BookModel book : books) {
            InventoryModel inventory = inventoryRepository.findByIsbn(book.getIsbn())
                    .orElseThrow(() -> new InventoryNotFoundException(book.getIsbn().toString()));
            totalCopies += inventory.getCopies();
        }
        return InventoryDto.builder().title(title)
                .copies(totalCopies)
                .build();
    }

    // Get copies by ISBN
    public InventoryDto getCopiesByIsbn(UUID isbn) {
        return inventoryRepository.findByIsbn(isbn)
                .map(InventoryService::toInventoryDto)
                .orElseThrow(() -> new InventoryNotFoundException(isbn.toString()));

        //return inventory.getCopies();
    }

    public UUID updateInventory(InventoryDto inventoryDto) {
        if (bookRepository.findByIsbn(inventoryDto.getIsbn()).isPresent()) {
            var inventoryModel = toInventoryModel(inventoryDto);
            var savedBook = inventoryRepository.save(inventoryModel);
            return savedBook.getIsbn();
        }
        throw new InventoryNotFoundException(inventoryDto.getIsbn().toString());
    }

    public UUID updateInventory(BookModel bookModel) {
        if (inventoryRepository.findByIsbn(bookModel.getIsbn()).isEmpty()) {
            InventoryModel inventoryModel = InventoryModel.builder()
                    .isbn(bookModel.getIsbn())
                    .copies(1)
                    .build();
            var savedBook = inventoryRepository.save(inventoryModel);
            return savedBook.getIsbn();
        }

        throw new InventoryNotFoundException(bookModel.getIsbn().toString());
    }

    // Save or update inventory copies
    public void saveOrUpdateInventory(UUID isbn, int copies) {
        try {
            // Check if inventory entry exists for the given ISBN
            Optional<InventoryModel> existingInventory = inventoryRepository.findByIsbn(isbn);
            if (existingInventory.isPresent()) {
                InventoryModel updatedinventoryModel = existingInventory.get();
                // If inventory entry exists, update the copies by 1
                updatedinventoryModel.setCopies(updatedinventoryModel.getCopies() + 1);
                inventoryRepository.save(updatedinventoryModel);
            } else {
                // If inventory entry does not exist, create a new entry
                InventoryModel newInventory = InventoryModel.builder()
                        .isbn(isbn)
                        .copies(copies).build();
                inventoryRepository.save(newInventory);
            }
        } catch(Exception e) {
          throw new InventoryUpdateException(isbn.toString());
        }
    }

    public InventoryGlobalDto getTotalCopies() {
        Long globalCopies = inventoryRepository.findAll().stream()
                .mapToLong(InventoryModel::getCopies)
                .sum();
        return InventoryGlobalDto.builder()
                .total_copies(globalCopies).build();
    }
    private static InventoryModel toInventoryModel(InventoryDto inventoryDto) {
        return InventoryModel.builder()
                .isbn(inventoryDto.getIsbn())
                .copies(inventoryDto.getCopies())
                .build();
    }

        private static InventoryDto toInventoryDto(InventoryModel inventoryModel) {
        return InventoryDto.builder()
                .isbn(inventoryModel.getIsbn())
                .copies(inventoryModel.getCopies())
                .build();
    }
}
