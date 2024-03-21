package fi.epassi.recruitment.book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookModel, UUID>  {

    Optional<BookModel> findByIsbn(UUID isbn);

    Page<BookModel> findByTitle(String title, Pageable pageable);

    Page<BookModel> findByAuthor(String author, Pageable pageable);

    Page<BookModel> findByAuthorAndTitle(String author, String title, Pageable pageable);

}
