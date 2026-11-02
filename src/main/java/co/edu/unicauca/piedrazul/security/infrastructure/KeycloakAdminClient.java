package co.edu.unicauca.piedrazul.security.infrastructure;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KeycloakAdminClient {

    private final WebClient webClient;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public KeycloakAdminClient(WebClient keycloakWebClient) {
        this.webClient = keycloakWebClient;
    }

    // 1. Obtener token administrativo de Keycloak
    public String obtainAdminToken() {

        KeycloakTokenResponse response = webClient.post()
                .uri(serverUrl + "/realms/" + realm
                        + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(
                                "grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .block();

        if (response == null || response.accessToken() == null
                || response.accessToken().isBlank()) {
            throw new IllegalStateException(
                    "Keycloak no devolvió un token administrativo");
        }

        return response.accessToken();
    }

    // 2. Crear un usuario en Keycloak
    public String createUser(
            String username,
            String email,
            String fullName,
            String password) {

        String token = obtainAdminToken();

        String[] nameParts = fullName.trim().split("\\s+", 2);

        Map<String, Object> user = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "firstName", nameParts[0],
                "lastName", nameParts.length > 1 ? nameParts[1] : "",
                "credentials", List.of(
                        Map.of(
                                "type", "password",
                                "value", password,
                                "temporary", false
                        )
                )
        );

        ResponseEntity<Void> response = webClient.post()
                .uri(serverUrl + "/admin/realms/" + realm + "/users")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .retrieve()
                .toBodilessEntity()
                .block();

        if (response == null || response.getHeaders().getLocation() == null) {
            throw new IllegalStateException(
                    "Keycloak creó el usuario, pero no devolvió su identificador");
        }

        URI location = response.getHeaders().getLocation();
        String path = location.getPath();

        return path.substring(path.lastIndexOf('/') + 1);
    }

    // 3. Asignar un rol del realm al usuario
    public void assignRealmRole(String userId, String roleName) {

        String token = obtainAdminToken();

        WebClient.RequestHeadersSpec<?> getRoleRequest =
                webClient.get()
                        .uri(serverUrl + "/admin/realms/" + realm
                                + "/roles/" + roleName);

        getRoleRequest.headers(headers -> headers.setBearerAuth(token));

        Map<String, Object> role = getRoleRequest
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (role == null || role.isEmpty()) {
            throw new IllegalStateException(
                    "No se encontró el rol " + roleName + " en Keycloak");
        }

        WebClient.RequestBodySpec postRoleRequest =
                webClient.post()
                        .uri(serverUrl + "/admin/realms/" + realm
                                + "/users/" + userId + "/role-mappings/realm");

        postRoleRequest.headers(headers -> headers.setBearerAuth(token));
        postRoleRequest.contentType(MediaType.APPLICATION_JSON);

        postRoleRequest
                .bodyValue(List.of(role))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    // 4. Eliminar usuario si falla una operación posterior
    public void deleteUser(String userId) {

        String token = obtainAdminToken();

        WebClient.RequestHeadersSpec<?> deleteRequest =
                webClient.delete()
                        .uri(serverUrl + "/admin/realms/" + realm
                                + "/users/" + userId);

        deleteRequest.headers(headers -> headers.setBearerAuth(token));

        deleteRequest
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
