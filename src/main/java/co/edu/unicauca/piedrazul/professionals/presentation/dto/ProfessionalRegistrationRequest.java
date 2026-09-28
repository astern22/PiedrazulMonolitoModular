package co.edu.unicauca.piedrazul.professionals.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro de un profesional junto con su cuenta de usuario,
 * de modo que nadie tenga que conocer ni digitar IDs internos.
 */
public record ProfessionalRegistrationRequest(
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
        String password,

        @NotNull
        Long specialtyId,

        @NotNull
        @Min(5)
        @Max(480)
        Integer appointmentIntervalMinutes
) {
}
