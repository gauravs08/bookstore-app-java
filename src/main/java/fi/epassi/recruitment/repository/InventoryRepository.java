package fi.epassi.recruitment.repository;

import fi.epassi.recruitment.model.Inventory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface InventoryRepository extends R2dbcRepository<Inventory, UUID> {

    Mono<Inventory> findByIsbn(UUID isbn);

}
