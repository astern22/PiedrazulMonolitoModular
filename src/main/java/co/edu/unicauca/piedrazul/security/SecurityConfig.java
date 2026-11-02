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
                                .requestMatchers(
                                        "/auth/**"
                                )
                                .permitAll()
                                // Solo el administrador gestiona cuentas internas (agendadores)
                                .requestMatchers("/api/users/**")
                                .hasRole("ADMIN")

                                // Pacientes: cada paciente solo accede a su propio registro (/me)
                                .requestMatchers(HttpMethod.GET, "/api/patients/me")
                                .authenticated()
                                // El resto de datos de pacientes (PII) es solo para el personal
                                .requestMatchers("/api/patients/**")
                                .hasAnyRole("ADMIN", "SCHEDULER", "PROFESSIONAL")

                                // Citas: el paciente solo ve las suyas (/me) y puede agendar
                                .requestMatchers(HttpMethod.GET, "/api/appointments/me")
                                .hasRole("PATIENT")
                                .requestMatchers(HttpMethod.POST, "/api/appointments/create")
                                .hasAnyRole("PATIENT", "ADMIN", "SCHEDULER")
                                // Modificar o cancelar citas: solo administracion / agendadores
                                .requestMatchers(HttpMethod.PUT, "/api/appointments/**")
                                .hasAnyRole("ADMIN", "SCHEDULER")
                                .requestMatchers(HttpMethod.DELETE, "/api/appointments/**")
                                .hasAnyRole("ADMIN", "SCHEDULER")
                                // Listados y consultas generales de citas: solo personal
                                .requestMatchers("/api/appointments/**")
                                .hasAnyRole("ADMIN", "SCHEDULER", "PROFESSIONAL")

                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
