package fi.epassi.recruitment.services;

import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.exception.BookstoreNotFoundException;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.model.Inventory;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.repository.BookstoreRepository;
import fi.epassi.recruitment.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "inventoryCache")
public class InventoryService {


    private final BookRepository bookRepository;

    private final InventoryRepository inventoryRepository;
    private final BookstoreRepository bookstoreRepository;

    @Cacheable(cacheNames = "copiesByAuthor",key = "{#author}")
    public Mono<Map<String, Integer>> getCopiesByAuthorBookstore(String author) {
        Flux<BookModel> books = bookRepository.findByAuthorContainingIgnoreCase(author, Pageable.ofSize(20));
        return getCopiesMapByBookStoreId(books);
    }

    @Cacheable(cacheNames = "copiesByTitle", key = "{#title}")
    public Mono<Map<String, Integer>> getCopiesByTitleBookstore(String title) {
        Flux<BookModel> books = bookRepository.findByTitleContainingIgnoreCase(title, Pageable.ofSize(20));
        return getCopiesMapByBookStoreId(books);
    }

    private Mono<Map<String, Integer>> getCopiesMapByBookStoreId(Flux<BookModel> books) {
        return books.flatMap(book -> {
                    return inventoryRepository.findByIsbn(book.getIsbn())
                            .flatMap(inventory -> {
                                Long bookstoreId = inventory.getBookstoreId();
                                int copies = inventory.getCopies();
                                Map<Long, Integer> map = new HashMap<>();
                                map.put(bookstoreId, copies);
                                return Mono.just(map);
                            });
                })
                .reduce(new HashMap<>(), (map1, map2) -> {
                    map2.forEach((bookstoreId, copies) -> map1.merge(String.valueOf(bookstoreId), copies, Integer::sum));
                    return map1;
                });
    }

    @Cacheable(cacheNames = "copiesByIsbn",key = "{#isbn}")
    public Flux<InventoryDto> getCopiesByIsbn(UUID isbn) {
        return inventoryRepository.findByIsbn(isbn)
                .map(this::toInventoryDto)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", isbn.toString())));
    }

    @CacheEvict(cacheNames = {"copiesByIsbn", "copiesByTitle","copiesByAuthor"}, allEntries = true)
    public Mono<UUID> updateInventory(UUID isbn, Integer copies, Long bookstore_id) {
        return inventoryRepository.findByIsbnAndBookstoreId(isbn, bookstore_id)
                .flatMap(inventory -> {
                    //int updatedCopies = inventory.getCopies() + copies;
                    inventory.setCopies(copies);
                    inventory.setBookstoreId(bookstore_id);
                    //inventory.setNewInventory(false);
                    return inventoryRepository.save(inventory).map(Inventory::getIsbn);
                })
                .switchIfEmpty(createNewInventory(isbn, copies, bookstore_id));
    }


    public Mono<InventoryGlobalDto> getTotalCopies() {
        return inventoryRepository.findAll()
                .map(Inventory::getCopies)
                .reduce(0L, Long::sum)
                .map(totalCopies -> InventoryGlobalDto.builder()
                        .total_copies(totalCopies)
                        .build());
    }

    private InventoryDto toInventoryDto(Inventory inventory) {
        return InventoryDto.builder()
                .isbn(inventory.getIsbn())
                .copies(inventory.getCopies())
                .bookstore_id(inventory.getBookstoreId())
                .build();
    }

    private Mono<UUID> createNewInventory(UUID isbn, Integer copies, Long bookstoreId) {
        return bookstoreRepository.existsById(bookstoreId)
                .flatMap(exists -> {
                    if (exists) {
                        Inventory newInventory = new Inventory();
                        newInventory.setIsbn(isbn);
                        newInventory.setCopies(copies);
                        newInventory.setBookstoreId(bookstoreId);
                        //newInventory.setNewInventory(true);
                        return inventoryRepository.save(newInventory).map(Inventory::getIsbn);
                    } else {
                        return Mono.error(new BookstoreNotFoundException("Bookstore with ID " + bookstoreId + " not found."));
                    }
                });
    }
}
