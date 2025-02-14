package fi.book.org.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Date;

import io.jsonwebtoken.Claims;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtUtilTest {

    private final String SECRET_KEY = "xT+G2mBqX9oE3J5UhzR+FowBnCqLc9yRpYowLKH+X7k=";
    private final long JWT_TOKEN_VALIDITY = Duration.ofHours(1).toMillis(); // 1 hour
    private JwtUtil jwtUtil;
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "JWT_TOKEN_VALIDITY", JWT_TOKEN_VALIDITY);
        //jwtUtil.JWT_TOKEN_VALIDITY = JWT_TOKEN_VALIDITY;
    }

    @Test
    public void testGenerateToken() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ")); // JWT tokens start with "eyJ"
    }

    @Test
    public void testExtractUsername() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        assertEquals("testUser", username);
    }

    @Test
    public void testExtractExpiration() {
        String token = jwtUtil.generateToken(userDetails);
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    public void testValidateToken() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    public void testValidateTokenWithWrongUsername() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("correctUser"); // This is the username for the token

        // Generate a token with the correct username
        String token = jwtUtil.generateToken(userDetails);

        // Now, create a new UserDetails mock for validation with a different username
        UserDetails wrongUserDetails = mock(UserDetails.class);
        when(wrongUserDetails.getUsername()).thenReturn("wrongUser");

        // Act & Assert
        assertFalse(jwtUtil.validateToken(token, wrongUserDetails)); // This should return false
    }


    @Test
    public void testExtractClaim() {
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractClaim(token, Claims::getSubject);
        assertEquals("testUser", username);
    }


}
