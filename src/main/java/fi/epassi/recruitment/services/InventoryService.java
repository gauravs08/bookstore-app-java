package fi.epassi.recruitment.services;

import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.model.Inventory;
import fi.epassi.recruitment.repository.BookRepository;
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
//    @Autowired
//    public InventoryService(BookRepository bookRepository, InventoryRepository inventoryRepository){
//        this.bookRepository=bookRepository;
//        this.inventoryRepository=inventoryRepository;
//    }
    @Cacheable(key = "{#author}")
    public Mono<InventoryDto> getCopiesByAuthor(String author) {
        Flux<BookModel> books = bookRepository.findByAuthorContainingIgnoreCase(author, Pageable.ofSize(1));

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
        Flux<BookModel> books = bookRepository.findByTitleContainingIgnoreCase(title, Pageable.ofSize(1));
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
                .map(this::toInventoryDto)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", isbn.toString())));
                //.orElseThrow(() -> new InventoryNotFoundException(isbn.toString()));

        //return inventory.getCopies();
    }

    public Mono<UUID> updateInventory(UUID isbn, Integer copies) {
        return inventoryRepository.findByIsbn(isbn)
                .flatMap(inventory -> {
                    int updatedCopies = inventory.getCopies() + copies;
                    inventory.setCopies(updatedCopies);
                    inventory.setNewInventory(false);
                    return inventoryRepository.save(inventory).map(Inventory::getIsbn);
                })
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", isbn.toString())));
    }



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
                    Inventory newInventory = new Inventory(isbn, 1,true);
                    return inventoryRepository.save(newInventory);
                }))
                .then();

    }

    public Mono<InventoryGlobalDto> getTotalCopies() {
        return inventoryRepository.findAll()
                .map(Inventory::getCopies)
                .reduce(0L, Long::sum)
                .map(totalCopies -> InventoryGlobalDto.builder()
                        .total_copies(totalCopies)
                        .build());
    }
    private Inventory toInventoryModel(InventoryDto inventoryDto) {
        return Inventory.builder()
                .isbn(inventoryDto.getIsbn())
                .copies(inventoryDto.getCopies())
                .build();
    }

    private InventoryDto toInventoryDto(Inventory inventory) {
        return InventoryDto.builder()
                .isbn(inventory.getIsbn())
                .copies(inventory.getCopies())
                .build();
    }
}
