//package fi.book.org.component;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.Map;
//import java.util.function.Function;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//
//@Component
//public class JwtUtil {
//
//    @Value("${jwt.secret}") // Secret key should be stored in application.properties or secrets management
//    private String SECRET_KEY;
//
//    @Value("${jwt.expiration.ms:3600000}") // Token expiration (default 1 hour)
//    private long JWT_TOKEN_VALIDITY;
//
//    // Retrieve username from JWT token
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    @Value("${jwt.expiration.ms:3600000}") // Token expiration (default 1 hour)
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//    }
//    // Retrieve expiration date from the JWT token
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    // Generic method to extract any claim using the ClaimsResolver
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // Extract all claims from the token
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .setSigningKey(SECRET_KEY)  // Use your secret key to parse and validate the JWT
/// /                .verifyWith(getSigningKey())  // Use your secret key to parse and validate the JWT
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
//
//    // Check if the token is expired
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    // Validate the JWT token
//    public boolean validateToken(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//
//    // Generate a JWT token for a given user
//    public String generateToken(UserDetails userDetails) {
//        // Create claims with the user's username and other optional claims
//        return Jwts.builder()
//                .claims(Map.of())
//                .subject(userDetails.getUsername())
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // Signing the JWT with the secret key
//                .compact();
////        return Jwts.builder()
////                .claims(Map.of())
////                .subject(userDetails.getUsername())
////                .issuedAt(new Date())
////                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
////                //.signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // Signing the JWT with the secret key
////                .signWith(getSigningKey()) // or RS512, PS256, EdDSA, etc...
////                .compact();
//    }
//
//}

package fi.book.org.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("${jwt.expiration.ms:3600000}") // Token expiration (default 1 hour)
    private long JWT_TOKEN_VALIDITY;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}

