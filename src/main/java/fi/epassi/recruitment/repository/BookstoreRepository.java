package fi.epassi.recruitment.repository;

import fi.epassi.recruitment.model.Inventory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BookstoreRepository extends ReactiveCrudRepository<Inventory, Long> {
    Mono<Boolean> existsById(Long bookstoreId);

}
