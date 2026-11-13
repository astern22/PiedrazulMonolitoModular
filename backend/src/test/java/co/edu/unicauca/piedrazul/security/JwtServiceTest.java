package co.edu.unicauca.piedrazul.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

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

    @Test
    @SuppressWarnings("unchecked")
    void testGenerateTokenWithRoles() {
        String username = "doctor_juan";
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_DOCTOR");

        String token = jwtService.generateToken(username, roles);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertNotNull(claims);
        List<String> extractedRoles = claims.get("roles", List.class);
        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
        assertTrue(extractedRoles.contains("ROLE_DOCTOR"));
    }

    @Test
    void testExtractExpiration() {
        String username = "patient_maria";
        String token = jwtService.generateToken(username);

        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
}
