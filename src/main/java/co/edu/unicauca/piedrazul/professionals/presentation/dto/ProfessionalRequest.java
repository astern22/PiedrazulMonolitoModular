package co.edu.unicauca.piedrazul.professionals.presentation.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfessionalRequest(
        @NotNull
        Long userId,

        @NotNull
        Long specialtyId,

        @NotBlank
        String professionalType,

        @NotNull
        @Min(1)
        Integer appointmentIntervalMinutes
) {
}
