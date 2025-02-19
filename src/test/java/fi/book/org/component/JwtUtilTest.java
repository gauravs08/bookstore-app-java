package fi.book.org.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtUtilTest {

    private final String SECRET_KEY = "xT+G2mBqX9oE3J5UhzR+FowBnCqLc9yRpYowLKH+X7k=";
    private final long JWT_TOKEN_VALIDITY = Duration.ofHours(1).toMillis(); // 1 hour
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "JWT_TOKEN_VALIDITY", JWT_TOKEN_VALIDITY);
        //jwtUtil.JWT_TOKEN_VALIDITY = JWT_TOKEN_VALIDITY;
    }

    @Test
    void testGenerateToken() {
        // Given
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());

        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        // Given
        String username = "testUser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testTokenIsValid() {
        // Given
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testTokenIsInvalidWhenTampered() {
        // Given
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        // When / Then
        assertThrows(SignatureException.class, () -> jwtUtil.extractUsername(token + "tampered"));
    }

    @Test
    void testTokenIsExpired() throws InterruptedException {
        // Given
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 10)) // 10 hours ago
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(jwtUtil.getSigningKey())
                .compact();


        // When
        boolean isExpired;
        try {
            isExpired = jwtUtil.isTokenValid(token, userDetails);
        } catch (ExpiredJwtException e) {
            isExpired = false;
        }

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testExtractExpiration() {
        // Given
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expirationDate = jwtUtil.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date())); // Expiration should be in the future
    }
}
