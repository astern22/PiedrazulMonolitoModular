package co.edu.unicauca.piedrazul.professionals.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AppointmentIntervalRequest(
        @NotNull
        @Min(5)
        @Max(480)
        Integer appointmentIntervalMinutes
) {
}
