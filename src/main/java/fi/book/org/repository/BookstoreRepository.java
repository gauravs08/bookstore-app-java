package fi.book.org.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import fi.book.org.model.Bookstore;
import reactor.core.publisher.Mono;

@Repository
public interface BookstoreRepository extends ReactiveCrudRepository<Bookstore, Long> {
    Mono<Boolean> existsById(Long id);

    Mono<Bookstore> findById(Long id);

}
