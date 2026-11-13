package co.edu.unicauca.piedrazul.scheduling.presentation.dto;

import java.time.LocalTime;

public record AvailableSlotResponse(
        LocalTime startTime,
        LocalTime endTime
) {
}
