package fi.book.org;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (args.length == 0) {
            System.out.println("Usage: PasswordHasher <password>");
            return;
        }
        String hashedPassword = encoder.encode(args[0]);
        System.out.println("Hashed Password: " + hashedPassword);
    }
}
