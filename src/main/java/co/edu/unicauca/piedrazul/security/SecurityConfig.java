package co.edu.unicauca.piedrazul.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/auth/**")
                                .permitAll()
                                // Solo el administrador gestiona cuentas internas (agendadores)
                                .requestMatchers("/api/users/**")
                                .hasRole("ADMIN")
                                // El listado completo de citas es solo para el personal de agenda
                                .requestMatchers(HttpMethod.GET, "/api/appointments")
                                .hasAnyRole("ADMIN", "SCHEDULER")
                                // La busqueda de citas por profesional o fecha es para el personal;
                                // el paciente solo consulta sus propias citas
                                .requestMatchers(HttpMethod.GET, "/api/appointments/search")
                                .hasAnyRole("ADMIN", "SCHEDULER", "PROFESSIONAL")
                                // Perfil del profesional autenticado
                                .requestMatchers(HttpMethod.GET, "/api/professionals/me")
                                .hasRole("PROFESSIONAL")
                                // Horarios y duracion de citas: un paciente no debe poder modificarlos.
                                // Si quien llama es un profesional, el servicio valida que sean los suyos
                                .requestMatchers(HttpMethod.POST, "/api/availability")
                                .hasAnyRole("ADMIN", "SCHEDULER", "PROFESSIONAL")
                                .requestMatchers(HttpMethod.PATCH, "/api/availability/{id}/deactivate")
                                .hasAnyRole("ADMIN", "SCHEDULER", "PROFESSIONAL")
                                .requestMatchers(HttpMethod.PATCH, "/api/professionals/{id}/appointment-interval")
                                .hasAnyRole("ADMIN", "SCHEDULER", "PROFESSIONAL")
                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}