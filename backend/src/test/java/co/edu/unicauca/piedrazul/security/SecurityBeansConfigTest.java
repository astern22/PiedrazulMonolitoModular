package co.edu.unicauca.piedrazul.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityBeansConfigTest {

    @Test
    void testPasswordEncoderBean() {
        SecurityBeansConfig config = new SecurityBeansConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        String raw = "secretPassword123";
        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }
}
