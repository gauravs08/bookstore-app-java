package fi.book.org.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import fi.book.org.model.User;
import fi.book.org.model.UserRole;
import fi.book.org.repository.UserRepository;
import fi.book.org.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> registerUser(String username, String rawPassword, String role) {
        if (username == null || username.trim().isEmpty()) { // ✅ Check for null or empty username
            return Mono.error(new IllegalArgumentException("Username cannot be null or empty"));
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) { // ✅ Check for null or empty password
            return Mono.error(new IllegalArgumentException("Password cannot be null or empty"));
        }
        String hashedPassword = passwordEncoder.encode(rawPassword); // Hash password
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password(hashedPassword).build();
        return userRepository.save(user) // Save user first
                .flatMap(savedUser -> {
                    UserRole userRole = new UserRole(savedUser.getId(), role);
                    return userRoleRepository.save(userRole) // Save role in user_roles table
                            .then(userRepository.findUserByUsername(username));
                });

    }
}
