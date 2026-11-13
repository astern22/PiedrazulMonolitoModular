package co.edu.unicauca.piedrazul.scheduling.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record WeeklyAvailabilityRequest(
        @NotNull
        Long professionalId,

        @NotNull
        @Min(1)
        @Max(7)
        Integer dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime

) {
}
