
package co.edu.unicauca.piedrazul.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Registro local temporalmente público.
                        // Lo adaptaremos para que cree cuentas en Keycloak.
                        .requestMatchers("/auth/**").permitAll()

                        // Administración de usuarios.
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // Historial por documento: solo personal autorizado.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/patient/document/*/history"
                        ).hasAnyRole(
                                "PROFESSIONAL", "SCHEDULER", "ADMIN"
                        )

                        // Consultas generales de citas.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/search",
                                "/api/appointments/professional/**",
                                "/api/appointments"
                        ).hasAnyRole(
                                "PROFESSIONAL", "SCHEDULER", "ADMIN"
                        )

                        // Consultas de citas por paciente.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/patient/**"
                        ).hasAnyRole(
                                "PATIENT", "PROFESSIONAL", "SCHEDULER", "ADMIN"
                        )

                        // Crear citas.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/appointments/**"
                        ).hasAnyRole(
                                "PATIENT", "PROFESSIONAL", "SCHEDULER", "ADMIN"
                        )

                        // Modificar citas.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/appointments/**"
                        ).hasAnyRole(
                                "PROFESSIONAL", "SCHEDULER", "ADMIN"
                        )

                        // Eliminar citas.
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/appointments/**"
                        ).hasAnyRole(
                                "PROFESSIONAL", "SCHEDULER", "ADMIN"
                        )

                        .anyRequest().authenticated()
                )

                // Activar validación de JWT con Keycloak.
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        keycloakJwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<GrantedAuthority> authorities =
                    new ArrayList<>();

            // Conservar conversión de scopes estándar.
            JwtGrantedAuthoritiesConverter scopesConverter =
                    new JwtGrantedAuthoritiesConverter();

            Collection<GrantedAuthority> scopeAuthorities =
                    scopesConverter.convert(jwt);

            if (scopeAuthorities != null) {
                authorities.addAll(scopeAuthorities);
            }

            // Extraer roles del realm de Keycloak.
            Map<String, Object> realmAccess =
                    jwt.getClaimAsMap("realm_access");

            if (realmAccess != null) {

                Object rolesObject = realmAccess.get("roles");

                if (rolesObject instanceof Collection<?> roles) {

                    for (Object role : roles) {

                        if (role instanceof String roleName) {

                            authorities.add(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + roleName
                                    )
                            );
                        }
                    }
                }
            }

            return authorities;
        });

        return converter;
    }
}
