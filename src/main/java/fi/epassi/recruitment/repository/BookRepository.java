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
//    @Query("SELECT * FROM books ORDER BY id LIMIT :limit OFFSET :offset")
//    Flux<Books> findAllByPage(int limit, int offset);

//    default Flux<Books> findAllBy(Pageable pageable) {
//        int offset = pageable.getPageNumber() * pageable.getPageSize();
//        int limit = pageable.getPageSize();
//        return findAllByPage(limit, offset);
//    }
    Flux<BookModel> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Flux<BookModel> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Flux<BookModel> findByAuthorContainingIgnoreCaseAndTitleContainingIgnoreCase(String author, String title, Pageable pageable);

}
