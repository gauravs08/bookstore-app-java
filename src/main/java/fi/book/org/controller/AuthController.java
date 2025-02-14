package fi.book.org.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
        return userDetailsService.findByUsername(authRequest.getUsername()) // Reactive way to load user details
                .flatMap(userDetails -> {
                    // Verify password (using the reactive password encoder or existing check)
                    if (passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
                        // Generate the JWT token
                        String token = jwtUtil.generateToken(userDetails);
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

                        return Mono.just(ApiResponse.ok((Object) new AuthResponse(token)));
                    } else {
                        // Return error response if password doesn't match
                        return Mono.error(new AuthException("Invalid username or password"));

                    }
                })
                .switchIfEmpty(Mono.error(new AuthException("User not found")))// 🔴 Throw exception instead of returning response
                .onErrorResume(e -> {
                    log.error("Invalid username or password", e);
                    return Mono.error(new AuthException("Invalid username or password"));
                });
    }


    // Registration Endpoint
    @PostMapping("/register")
    public Mono<ApiResponse<String>> register(@RequestBody AuthRequest authRequest) {
        return userRepository.findUserByUsername(authRequest.getUsername()) // Direct repository call to check existence
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
