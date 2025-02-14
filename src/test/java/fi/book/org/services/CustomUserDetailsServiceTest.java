package fi.book.org.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.UUID;

import fi.book.org.model.User;
import fi.book.org.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    // ✅ Test case: User exists with roles
    @Test
    public void test_findByUsername_userExistsWithRoles() {
        String username = "testUser";
        UUID userId = UUID.randomUUID();
        User mockUser = User.builder().id(userId).username(username).password("hashedPassword").build();
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        when(userRepository.findUserByUsername(username)).thenReturn(Mono.just(mockUser));
        when(userRepository.findRolesByUserId(userId)).thenReturn(Flux.fromIterable(roles));

        StepVerifier.create(userDetailsService.findByUsername(username))
                .expectNextMatches(userDetails ->
                        userDetails.getUsername().equals(username) &&
                                userDetails.getPassword().equals("hashedPassword") &&
                                userDetails.getAuthorities().size() == 2 &&
                                userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")) &&
                                userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))
                )
                .verifyComplete();

        verify(userRepository).findUserByUsername(username);
        verify(userRepository).findRolesByUserId(userId);
    }

    // ✅ Test case: User exists but has no roles
    @Test
    public void test_findByUsername_userExistsNoRoles() {
        String username = "testUser";
        UUID userId = UUID.randomUUID();
        User mockUser = User.builder().id(userId).username(username).password("hashedPassword").build();

        when(userRepository.findUserByUsername(username)).thenReturn(Mono.just(mockUser));
        when(userRepository.findRolesByUserId(userId)).thenReturn(Flux.empty());

        StepVerifier.create(userDetailsService.findByUsername(username))
                .expectNextMatches(userDetails ->
                        userDetails.getUsername().equals(username) &&
                                userDetails.getPassword().equals("hashedPassword") &&
                                userDetails.getAuthorities().isEmpty()
                )
                .verifyComplete();

        verify(userRepository).findUserByUsername(username);
        verify(userRepository).findRolesByUserId(userId);
    }

    // ✅ Test case: User not found
    @Test
    public void test_findByUsername_userNotFound() {
        String username = "nonExistentUser";

        when(userRepository.findUserByUsername(username)).thenReturn(Mono.empty());

        StepVerifier.create(userDetailsService.findByUsername(username))
                .expectErrorMatches(error -> error instanceof UsernameNotFoundException &&
                        error.getMessage().equals("User not found: " + username))
                .verify();

        verify(userRepository).findUserByUsername(username);
        verify(userRepository, never()).findRolesByUserId(any(UUID.class)); // Ensure roles are not queried
    }

    // ✅ Test case: Database error while fetching user
    @Test
    public void test_findByUsername_dbErrorFetchingUser() {
        String username = "testUser";

        when(userRepository.findUserByUsername(username)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(userDetailsService.findByUsername(username))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("DB error"))
                .verify();

        verify(userRepository).findUserByUsername(username);
        verify(userRepository, never()).findRolesByUserId(any(UUID.class));
    }

    // ✅ Test case: Database error while fetching roles
    @Test
    public void test_findByUsername_dbErrorFetchingRoles() {
        String username = "testUser";
        UUID userId = UUID.randomUUID();
        User mockUser = User.builder().id(userId).username(username).password("hashedPassword").build();

        when(userRepository.findUserByUsername(username)).thenReturn(Mono.just(mockUser));
        when(userRepository.findRolesByUserId(userId)).thenReturn(Flux.error(new RuntimeException("DB error fetching roles")));

        StepVerifier.create(userDetailsService.findByUsername(username))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("DB error fetching roles"))
                .verify();

        verify(userRepository).findUserByUsername(username);
        verify(userRepository).findRolesByUserId(userId);
    }
}
