package fi.book.org.services;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fi.book.org.dto.InventoryDto;
import fi.book.org.dto.InventoryGlobalDto;
import fi.book.org.exception.BookstoreNotFoundException;
import fi.book.org.exception.InventoryNotFoundException;
import fi.book.org.model.BookModel;
import fi.book.org.model.Inventory;
import fi.book.org.repository.BookRepository;
import fi.book.org.repository.BookstoreRepository;
import fi.book.org.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "inventoryCache")
public class InventoryService {
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final BookstoreRepository bookstoreRepository;

    @Cacheable(cacheNames = "copiesByAuthor", key = "{#author}")
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
        return books.flatMap(book -> inventoryRepository.findInventoriesById(book.getId())
                        .flatMap(inventory -> {
                            Long bookstoreId = inventory.getBookstoreId();
                            int copies = inventory.getCopies();
                            Map<Long, Integer> map = new HashMap<>();
                            map.put(bookstoreId, copies);
                            return Mono.just(map);
                        }))
                .reduce(new HashMap<>(), (map1, map2) -> {
                    map2.forEach((bookstoreId, copies) -> map1.merge(String.valueOf(bookstoreId), copies, Integer::sum));
                    return map1;
                });
    }

    @Cacheable(cacheNames = "copiesByIsbn", key = "{#id}")
    public Flux<InventoryDto> getCopiesByIsbn(UUID id) {
        return inventoryRepository.findInventoriesById(id)
                .map(this::toInventoryDto)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", id.toString())));
    }

    @CacheEvict(cacheNames = {"copiesByIsbn", "copiesByTitle", "copiesByAuthor"}, allEntries = true)
    public Mono<UUID> updateInventory(UUID id, Integer copies, Long bookstore_id) {
        return inventoryRepository.findByIdAndBookstoreId(id, bookstore_id)
                .flatMap(inventory -> {
                    inventory.setCopies(copies);
                    inventory.setNew(false);
                    inventory.setBookstoreId(bookstore_id);
                    return inventoryRepository.save(inventory).map(Inventory::getId);
                })
                .switchIfEmpty(createNewInventory(id, copies, bookstore_id));
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
                .id(inventory.getId())
                .copies(inventory.getCopies())
                .bookstoreId(inventory.getBookstoreId())
                .build();
    }

    private Mono<UUID> createNewInventory(UUID id, Integer copies, Long bookstoreId) {
        return bookstoreRepository.existsById(bookstoreId)
                .flatMap(exists -> {
                    if (exists) {
                        Inventory newInventory = new Inventory();
                        newInventory.setId(id);
                        newInventory.setCopies(copies);
                        newInventory.setNew(true);
                        newInventory.setBookstoreId(bookstoreId);
                        return inventoryRepository.save(newInventory).map(Inventory::getId);
                    } else {
                        return Mono.error(new BookstoreNotFoundException("Bookstore with ID " + bookstoreId + " not found."));
                    }
                });
    }

    public Mono<Void> updateOrCreateInventory(BookModel bookModel) {
        return inventoryRepository.findInventoriesById(bookModel.getId())
                .flatMap(existingInventory -> {
                    existingInventory.setCopies(existingInventory.getCopies() + 1);
                    existingInventory.setNew(false);
                    return inventoryRepository.save(existingInventory);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Inventory newInventory = Inventory.builder()
                            .id(bookModel.getId())
                            .copies(1)
                            .bookstoreId(bookModel.getBookstoreId())
                            .isNew(true).build();
                    return inventoryRepository.save(newInventory);
                }))
                .then();
    }
}
