package fi.book.org.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import fi.book.org.component.JwtAuthenticationFilter;
import fi.book.org.component.JwtUtil;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Book Management API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;

    public SecurityConfig(JwtUtil jwtUtil, ReactiveUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Uses strong hashing
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        // Allow Swagger UI and API docs
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**").permitAll()

                        // Public access to login
                        .pathMatchers("/auth/login").permitAll()

                        // Only admin can register new users
                        .pathMatchers("/auth/register").hasRole("ADMIN")

                        // Inventory: Only authenticated users can GET, only admin can PUT
                        .pathMatchers(HttpMethod.GET, "/api/v1/inventory/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/inventory/**").hasRole("ADMIN")

                        // Book: Any authenticated user can GET & POST, only admin can PUT & DELETE
                        .pathMatchers(HttpMethod.GET, "/api/v1/books/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/books").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/v1/books").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/books/{isbn}").hasRole("ADMIN")

                        // Any other request must be authenticated
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)  // 🔴 Disable HTTP Basic Auth
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // Disable session storage
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
