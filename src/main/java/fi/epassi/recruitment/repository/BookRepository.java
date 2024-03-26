package fi.epassi.recruitment.repository;

import fi.epassi.recruitment.model.BookModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BookRepository extends ReactiveCrudRepository<BookModel, UUID> {
    Mono<BookModel> findByIsbn(UUID isbn);

    Flux<BookModel> findAllBy(Pageable pageable);

    Flux<BookModel> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Flux<BookModel> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Flux<BookModel> findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(String author, String title, Pageable pageable);

}
