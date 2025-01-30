package fi.book.org.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import fi.book.org.model.BookModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookRepository extends ReactiveCrudRepository<BookModel, UUID> {
    Mono<BookModel> findById(UUID id);

    Flux<BookModel> findAllBy(Pageable pageable);

    Flux<BookModel> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Flux<BookModel> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Flux<BookModel> findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(String author, String title, Pageable pageable);

    Flux<BookModel> findByBookstoreId(Long bookstore_id, Pageable pageable);
}
