package fi.book.org.component;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class JwtAuthenticationFilterTest {

    // Valid JWT token in Authorization header leads to successful authentication
    @Test
    public void test_valid_jwt_token_authentication_success() {
        // Arrange
        JwtUtil jwtUtil = mock(JwtUtil.class);
        ReactiveUserDetailsService userDetailsService = mock(ReactiveUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-token");
        WebFilterChain chain = mock(WebFilterChain.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(jwtUtil.extractUsername("test-token")).thenReturn("testuser");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(jwtUtil.validateToken("test-token", userDetails)).thenReturn(true);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = filter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).validateToken("test-token", userDetails);
        verify(chain).filter(exchange);
    }

    // Missing Authorization header returns null token
    @Test
    public void test_missing_auth_header_returns_null() {
        // Arrange
        JwtUtil jwtUtil = mock(JwtUtil.class);
        ReactiveUserDetailsService userDetailsService = mock(ReactiveUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        WebFilterChain chain = mock(WebFilterChain.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = filter.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userDetailsService);
    }
}
