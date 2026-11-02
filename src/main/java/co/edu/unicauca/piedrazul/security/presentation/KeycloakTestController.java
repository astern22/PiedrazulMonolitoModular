package co.edu.unicauca.piedrazul.security.presentation;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedrazul.security.infrastructure.KeycloakAdminClient;

@RestController
@RequestMapping("/internal/keycloak-test")
public class KeycloakTestController {

    private final KeycloakAdminClient keycloakAdminClient;

    public KeycloakTestController(
            KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @GetMapping
    public Map<String, String> testConnection() {

        String token = keycloakAdminClient.obtainAdminToken();

        return Map.of(
                "message", "Conexión administrativa con Keycloak exitosa",
                "tokenReceived", String.valueOf(token != null && !token.isBlank())
        );
    }
}