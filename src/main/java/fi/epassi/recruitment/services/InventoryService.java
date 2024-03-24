package fi.epassi.recruitment.services;

import fi.epassi.recruitment.model.Books;
import fi.epassi.recruitment.model.Inventory;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "inventoryCache")
public class InventoryService {

    private final BookRepository bookRepository;

    private final InventoryRepository inventoryRepository;

    @Cacheable(key = "{#author}")
    public Mono<InventoryDto> getCopiesByAuthor(String author) {
        Flux<Books> books = bookRepository.findByAuthor(author, Pageable.ofSize(1));

        return books.flatMap(book -> inventoryRepository.findByIsbn(book.getIsbn())
                        .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", book.getIsbn().toString()))))
                .reduce(0, (totalCopies, inventory) -> totalCopies + inventory.getCopies())
                .map(totalCopies -> InventoryDto.builder().author(author).copies(totalCopies).build());

//        int totalCopies = 0;
//        for (BookModel book : books) {
//            InventoryModel inventory = inventoryRepository.findByIsbn(book.getIsbn())
//                    .orElseThrow(() -> new InventoryNotFoundException(book.getIsbn().toString()));
//            totalCopies += inventory.getCopies();
//        }
//        return InventoryDto.builder().author(author)
//                .copies(totalCopies)
//                .build();
    }

    // Get copies by title
    @Cacheable(key = "{#title}")
    public Mono<InventoryDto> getCopiesByTitle(String title) {
        Flux<Books> books = bookRepository.findByTitle(title, Pageable.ofSize(1));
        return books.flatMap(book -> inventoryRepository.findByIsbn(book.getIsbn())
                        .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", book.getIsbn().toString()))))
                .reduce(0, (totalCopies, inventory) -> totalCopies + inventory.getCopies())
                .map(totalCopies -> InventoryDto.builder().author(title).copies(totalCopies).build());
//        int totalCopies = 0;
//        for (BookModel book : books) {
//            InventoryModel inventory = inventoryRepository.findByIsbn(book.getIsbn())
//                    .orElseThrow(() -> new InventoryNotFoundException(book.getIsbn().toString()));
//            totalCopies += inventory.getCopies();
//        }
//        return InventoryDto.builder().title(title)
//                .copies(totalCopies)
//                .build();
    }

    // Get copies by ISBN
    @Cacheable(key = "{#isbn}")
    public Mono<InventoryDto> getCopiesByIsbn(UUID isbn) {
        return inventoryRepository.findByIsbn(isbn)
                .map(InventoryService::toInventoryDto)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", isbn.toString())));
                //.orElseThrow(() -> new InventoryNotFoundException(isbn.toString()));

        //return inventory.getCopies();
    }

    public Mono<UUID> updateInventory(InventoryDto inventoryDto) {
        return bookRepository.findByIsbn(inventoryDto.getIsbn())
                .hasElement()
                .flatMap(found -> {
                    if (found) {
                        var inventoryModel = toInventoryModel(inventoryDto);
                        return inventoryRepository.save(inventoryModel)
                                .map(Inventory::getIsbn);
                    } else {
                        return Mono.error(new InventoryNotFoundException("ISBN", inventoryDto.getIsbn().toString()));
                    }
                });
//        if (Boolean.TRUE.equals(bookRepository.findByIsbn(inventoryDto.getIsbn()).hasElement().block())) {
//            var inventoryModel = toInventoryModel(inventoryDto);
//            var savedBook = inventoryRepository.save(inventoryModel).block();
//            return savedBook.getIsbn();
//        }
//        throw new InventoryNotFoundException(inventoryDto.getIsbn().toString());
    }

//    public UUID updateInventory(BookModel bookModel) {
//        if (Boolean.TRUE.equals(inventoryRepository.findByIsbn(bookModel.getIsbn()).hasElement().block())) {
//            InventoryModel inventoryModel = InventoryModel.builder()
//                    .isbn(bookModel.getIsbn())
//                    .copies(1)
//                    .build();
//            var savedBook = inventoryRepository.save(inventoryModel).block();
//            return savedBook.getIsbn();
//        }
//
//        throw new InventoryNotFoundException(bookModel.getIsbn().toString());
//    }

    // Save or update inventory copies
    public Mono<Void> saveOrUpdateInventory(UUID isbn, int copies) {
        return inventoryRepository.findByIsbn(isbn)
                .flatMap(inventory -> {
                    // Increment copies if inventory exists
                    inventory.setCopies(inventory.getCopies() + 1);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Create new inventory if not found
                    Inventory newInventory = new Inventory(isbn, 1);
                    return inventoryRepository.save(newInventory);
                }))
                .then();
//        try {
//            // Check if inventory entry exists for the given ISBN
//            Mono<InventoryModel> existingInventory = inventoryRepository.findByIsbn(isbn);
//            if (Boolean.TRUE.equals(existingInventory.hasElement().block())) {
//                InventoryModel updatedinventoryModel = existingInventory.block();
//                // If inventory entry exists, update the copies by 1
//                updatedinventoryModel.setCopies(updatedinventoryModel.getCopies() + 1);
//                inventoryRepository.save(updatedinventoryModel);
//            } else {
//                // If inventory entry does not exist, create a new entry
//                InventoryModel newInventory = InventoryModel.builder()
//                        .isbn(isbn)
//                        .copies(copies).build();
//                inventoryRepository.save(newInventory);
//            }
//        } catch(Exception e) {
//          throw new InventoryUpdateException(isbn.toString());
//        }
    }

    public Mono<InventoryGlobalDto> getTotalCopies() {
        return inventoryRepository.findAll()
                .map(Inventory::getCopies)
                .reduce(0L, Long::sum)
                .map(totalCopies -> InventoryGlobalDto.builder()
                        .total_copies(totalCopies)
                        .build());
    }
    private static Inventory toInventoryModel(InventoryDto inventoryDto) {
        return Inventory.builder()
                .isbn(inventoryDto.getIsbn())
                .copies(inventoryDto.getCopies())
                .build();
    }

        private static InventoryDto toInventoryDto(Inventory inventory) {
        return InventoryDto.builder()
                .isbn(inventory.getIsbn())
                .copies(inventory.getCopies())
                .build();
    }
}
