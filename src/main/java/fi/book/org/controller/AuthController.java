package fi.book.org.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.book.org.api.ApiResponse;
import fi.book.org.component.JwtUtil;
import fi.book.org.dto.AuthRequest;
import fi.book.org.dto.AuthResponse;
import fi.book.org.exception.AuthException;
import fi.book.org.repository.UserRepository;
import fi.book.org.services.CustomUserDetailsService;
import fi.book.org.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;


    @PostMapping("/login")
    public Mono<ApiResponse<Object>> login(@RequestBody AuthRequest authRequest) {
        if (authRequest.getUsername() == null || authRequest.getUsername().isBlank()
                || authRequest.getPassword() == null || authRequest.getPassword().isBlank()) {
            return Mono.error(new AuthException("Username and password must not be empty"));
        }
        return userDetailsService.findByUsername(authRequest.getUsername())
                .flatMap(userDetails -> {
                    if (passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
                        String token = jwtUtil.generateToken(userDetails);
                        return Mono.just(ApiResponse.ok((Object) new AuthResponse(token)));
                    } else {
                        return Mono.error(new AuthException("Invalid username or password"));
                    }
                })
                .switchIfEmpty(Mono.error(new AuthException("User not found")))
                .onErrorResume(e -> {
                    log.error("Invalid username or password", e);
                    return Mono.error(new AuthException("Invalid username or password"));
                });
    }


    // Registration Endpoint
    @PostMapping("/register")
    public Mono<ApiResponse<String>> register(@RequestBody AuthRequest authRequest) {
        return userRepository.findUserByUsername(authRequest.getUsername())
                .flatMap(existingUser ->
                        Mono.just(ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, "Username already taken"))
                )
                .switchIfEmpty(
                        userService.registerUser(authRequest.getUsername(), authRequest.getPassword(), "ROLE_USER")
                                .flatMap(user -> Mono.just(ApiResponse.buildResponse(HttpStatus.CREATED, "User registered successfully")))
                                .onErrorResume(e -> Mono.just(ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, e.getMessage())))
                );
    }
}
