package fi.book.org.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

import fi.book.org.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {
    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findUserByUsername(username) // Fetch user
                .doOnNext(user -> log.debug("User found: {}", user))
                .flatMap(user ->
                        userRepository.findRolesByUserId(user.getId()) // Fetch roles separately
                                .collectList()
                                .doOnNext(roles -> log.debug("Roles found: {}", roles))
                                .map(roles -> {
                                    user.setRoles(new HashSet<>(roles)); // Manually set roles
                                    return user;
                                })
                )
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        user.getRoles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                ));
    }
}
