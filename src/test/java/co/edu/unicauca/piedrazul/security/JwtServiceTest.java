package co.edu.unicauca.piedrazul.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testGenerateAndExtractUsername() {
        String username = "doctor_juan";
        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String extracted = jwtService.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    void testIsTokenValid() {
        String username = "doctor_juan";
        String token = jwtService.generateToken(username);

        assertTrue(jwtService.isTokenValid(token, username));
        assertFalse(jwtService.isTokenValid(token, "otro_usuario"));
        assertFalse(jwtService.isTokenExpired(token));
    }
}

