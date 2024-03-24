package fi.epassi.recruitment.repository;

import java.util.UUID;

import fi.epassi.recruitment.model.Books;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookRepository extends R2dbcRepository<Books, UUID> {

    Mono<Books> findByIsbn(UUID isbn);
    Flux<Books> findAllBy(Pageable pageable);
    Flux<Books> findByTitle(String title, Pageable pageable);

    Flux<Books> findByAuthor(String author, Pageable pageable);

    Flux<Books> findByAuthorAndTitle(String author, String title, Pageable pageable);

}
