package fi.book.org;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        // Insert a Hashed Password in Flyway Migration
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("user123"); // Replace with your password
        System.out.println("Hashed Password: " + hashedPassword);
        //admin123: $2a$10$TlQBI3zTvxxTdd6ZPw2LIeeTzG0OmnVlcTwhtBz5Sx4swqAPutvIi
        //user123: $2a$10$AtTGB0PU4Z2o7egEvLreserPVWoVJz/sntDjxdQOnrNdZJPv8Y9i6
    }
}
