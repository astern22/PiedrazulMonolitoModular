package co.edu.unicauca.piedrazul.security.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken
) {
}