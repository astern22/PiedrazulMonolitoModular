package co.edu.unicauca.piedrazul.users.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos con los que el administrador crea la cuenta de un agendador.
 */
public record SchedulerRegistrationRequest(
        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Email
        @Size(max = 120)
        String email,

        @NotBlank
        @Size(min = 6, max = 255)
        String password
) {
}
