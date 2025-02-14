package fi.book.org.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import fi.book.org.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByUsername(String username); // Default method for fetching user

    @Query("""
                SELECT u.id, u.username, u.password, ur.role
                FROM users u
                LEFT JOIN user_roles ur ON u.id = ur.user_id
                WHERE u.username = :username
            """)
    Mono<User> findUserWithRoles(String username);

    @Query("SELECT id, username, password FROM users WHERE username = :username")
    Mono<User> findUserByUsername(String username);

    @Query("SELECT role FROM user_roles WHERE user_id = :userId")
    Flux<String> findRolesByUserId(UUID userId);
}
