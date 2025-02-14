package fi.book.org.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import fi.book.org.model.User;
import fi.book.org.model.UserRole;
import fi.book.org.repository.UserRepository;
import fi.book.org.repository.UserRoleRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    //    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);  // ✅ Initializes mocks
//    }
    // User registration with valid username, password and role succeeds
    @Test
    public void test_register_user_success() {
        String username = "testUser";
        String password = "password123";
        String role = "ROLE_USER";
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .username(username)
                .password("hashedPassword")
                .build();

        UserRole userRole = new UserRole(userId, role);

        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(Mono.just(userRole));
        when(userRepository.findUserByUsername(username)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.registerUser(username, password, role))
                .expectNext(user)
                .verifyComplete();

        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    // Registration with null or empty username
    @Test
    public void test_register_user_null_username() {
        String password = "password123";
        String role = "ROLE_USER";

        StepVerifier.create(userService.registerUser(null, password, role))
                .expectError(IllegalArgumentException.class)
                .verify();

        StepVerifier.create(userService.registerUser("", password, role))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).save(any(User.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    public void test_register_hashPassword_null_username() {
        String user = "userabc";
        String role = "ROLE_USER";

        StepVerifier.create(userService.registerUser(user, null, role))
                .expectError(IllegalArgumentException.class)
                .verify();

        StepVerifier.create(userService.registerUser(user, "", role))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).save(any(User.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

}