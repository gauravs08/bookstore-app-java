package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.book.BookModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryModel, UUID> {

    Optional<InventoryModel> findByIsbn(UUID isbn);

}
