package fi.book.org.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;

import fi.book.org.api.ApiResponse;
import fi.book.org.component.JwtUtil;
import fi.book.org.controller.AuthController;
import fi.book.org.dto.AuthRequest;
import fi.book.org.dto.AuthResponse;
import fi.book.org.exception.AuthException;
import fi.book.org.model.User;
import fi.book.org.repository.UserRepository;
import fi.book.org.services.CustomUserDetailsService;
import fi.book.org.services.UserService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthController authController;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    private WebTestClient webTestClient;
    private String encodedPassword;
    private AuthRequest authRequest;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToController(authController)
                .configureClient().responseTimeout(Duration.ofSeconds(30)).build();
        encodedPassword = new BCryptPasswordEncoder().encode("password");
        authRequest = new AuthRequest("username", "password");

    }


    // Successful user login with correct credentials returns JWT token
    @Test
    public void test_login_with_valid_credentials_returns_token() {
        // Arrange
        String username = "testUser";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String token = "jwt.token.here";

        AuthRequest authRequest = new AuthRequest(username, password);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testUser", encodedPassword, new ArrayList<>());

        when(userDetailsService.findByUsername(username)).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // Act
        StepVerifier.create(authController.login(authRequest))
                .expectNextMatches(response -> {
                    assertNotNull(response);
                    assertTrue(response.getStatusCode() == 200); // Assuming ApiResponse has a success flag
                    assertEquals(token, ((AuthResponse) response.getResponse()).getToken());
                    return true;
                })
                .verifyComplete();

        // Assert
        verify(userDetailsService).findByUsername(username);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtUtil).generateToken(userDetails);
    }

    // Invalid password returns AuthException with appropriate message
    @Test
    public void test_invalid_password_throws_auth_exception() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("testUser", "wrongPassword");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testUser", encodedPassword, new ArrayList<>());

        when(userDetailsService.findByUsername("testUser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false);

        // Act
        Mono<ApiResponse<Object>> result = authController.login(authRequest);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AuthException
                                && ((AuthException) throwable).getStatusCode().equals(HttpStatus.UNAUTHORIZED)
                                && throwable.getMessage().contains("Invalid username or password"))
                .verify();
    }

    // New user registration with valid username and password returns CREATED status
    @Test
    public void test_register_new_user_returns_created() {
        AuthRequest request = new AuthRequest("newuser", "password123");
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .password("hashedPassword")
                .build();

        when(userRepository.findUserByUsername("newuser")).thenReturn(Mono.empty());
        when(userService.registerUser("newuser", "password123", "ROLE_USER"))
                .thenReturn(Mono.just(savedUser));

        StepVerifier.create(authController.register(request))
                .expectNextMatches(response -> {
                    return response.getStatusCode() == HttpStatus.CREATED.value() &&
                            response.getResponse().equals("User registered successfully");
                })
                .verifyComplete();
    }

    // Attempting to register with existing username returns BAD_REQUEST
    @Test
    public void test_register_existing_username_returns_bad_request() {
        AuthRequest request = new AuthRequest("existinguser", "password123");
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .username("existinguser")
                .password("hashedPassword")
                .build();

        when(userRepository.findUserByUsername("existinguser"))
                .thenReturn(Mono.just(existingUser));
        when(userService.registerUser("existinguser", "password123", "ROLE_USER"))
                .thenReturn(Mono.just(existingUser));

        StepVerifier.create(authController.register(request))
                .expectNextMatches(response -> {
                    return response.getStatusCode() == HttpStatus.BAD_REQUEST.value() &&
                            response.getResponse().equals("Username already taken");
                })
                .verifyComplete();
    }

    @Test
    public void test_register_unexpected_error_returns_bad_request() {
        AuthRequest request = new AuthRequest("newuser", "password123");

        when(userRepository.findUserByUsername("newuser")).thenReturn(Mono.empty());
        when(userService.registerUser("newuser", "password123", "ROLE_USER"))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error occurred")));

        StepVerifier.create(authController.register(request))
                .expectNextMatches(response -> {
                    return response.getStatusCode() == HttpStatus.BAD_REQUEST.value() &&
                            response.getResponse().equals("Unexpected error occurred");
                })
                .verifyComplete();
    }


}
