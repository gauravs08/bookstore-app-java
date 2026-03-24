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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class JwtAuthenticationFilterTest {

    @Test
    public void test_valid_jwt_token_authentication_success() {
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
        when(jwtUtil.isTokenValid("test-token", userDetails)).thenReturn(true);
        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil).isTokenValid("test-token", userDetails);
        verify(chain).filter(exchange);
    }

    @Test
    public void test_missing_auth_header_returns_null() {
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

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    public void test_invalid_jwt_token_proceeds_without_authentication() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        ReactiveUserDetailsService userDetailsService = mock(ReactiveUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer invalid-token");
        WebFilterChain chain = mock(WebFilterChain.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(jwtUtil.extractUsername("invalid-token")).thenReturn("testuser");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(jwtUtil.isTokenValid("invalid-token", userDetails)).thenReturn(false);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    public void test_auth_header_without_bearer_prefix_ignored() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        ReactiveUserDetailsService userDetailsService = mock(ReactiveUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic dXNlcjpwYXNz");
        WebFilterChain chain = mock(WebFilterChain.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    public void test_user_not_found_for_token_proceeds_without_auth() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        ReactiveUserDetailsService userDetailsService = mock(ReactiveUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer some-token");
        WebFilterChain chain = mock(WebFilterChain.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(jwtUtil.extractUsername("some-token")).thenReturn("unknownuser");
        when(userDetailsService.findByUsername("unknownuser")).thenReturn(Mono.empty());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(jwtUtil, never()).isTokenValid(any(), any());
    }
}
