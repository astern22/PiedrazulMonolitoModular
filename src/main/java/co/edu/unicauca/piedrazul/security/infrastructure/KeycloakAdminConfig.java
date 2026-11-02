package co.edu.unicauca.piedrazul.security.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakAdminConfig {

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder().build();
    }
}
