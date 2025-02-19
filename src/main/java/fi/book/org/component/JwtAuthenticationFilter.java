package fi.book.org.component;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.List;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ReactiveUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange.getRequest());  // Method to extract the token from the request header

        if (token != null) {
            // Extract the username from the token
            String username = jwtUtil.extractUsername(token);

            return userDetailsService.findByUsername(username)  // Fetch user details using username from token
                    .flatMap(userDetails -> {
                        // Validate the token with the user details
                        if (jwtUtil.isTokenValid(token, userDetails)) {
                            // Create the authentication token for security context
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)); // Set reactive context
                        }
                        return chain.filter(exchange);  // Proceed with the filter chain
                    });
        }
        return chain.filter(exchange);  // Continue without authentication if no token or invalid token
    }


    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return authHeaders.get(0).startsWith("Bearer ") ? authHeaders.get(0).substring(7) : null;
        }
        return null;
    }
}
