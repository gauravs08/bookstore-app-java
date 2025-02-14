package fi.book.org.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import fi.book.org.model.UserRole;
import reactor.core.publisher.Flux;

@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, UUID> {

    @Query("SELECT role FROM user_roles WHERE user_id = :userId")
    Flux<String> findRolesByUserId(UUID userId);
}
