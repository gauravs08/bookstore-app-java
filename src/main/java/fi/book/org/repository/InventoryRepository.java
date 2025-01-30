package fi.book.org.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import fi.book.org.model.Inventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryRepository extends ReactiveCrudRepository<Inventory, UUID> {
    Flux<Inventory> findInventoriesById(UUID id);

    Mono<Inventory> findByIdAndBookstoreId(UUID id, Long bookstore_id);

}
