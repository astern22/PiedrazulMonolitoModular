package co.edu.unicauca.piedrazul.professionals.presentation.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProfessionalRequest(
        @NotNull
        Long userId,

        @NotNull
        Long specialtyId,

        @NotNull
        @Min(1)
        Integer appointmentIntervalMinutes
) {
}
