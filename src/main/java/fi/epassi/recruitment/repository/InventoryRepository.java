package fi.epassi.recruitment.repository;

import fi.epassi.recruitment.model.Inventory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface InventoryRepository extends ReactiveCrudRepository<Inventory, UUID> {
    Flux<Inventory> findByIsbn(UUID isbn);

    Mono<Inventory> findByIsbnAndBookstoreId(UUID isbn, Long bookstore_id);

}
