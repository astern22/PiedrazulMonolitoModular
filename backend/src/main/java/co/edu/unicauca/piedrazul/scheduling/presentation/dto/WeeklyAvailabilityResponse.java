package co.edu.unicauca.piedrazul.scheduling.presentation.dto;

import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;

import java.time.LocalTime;

public record WeeklyAvailabilityResponse(
        Long id,
        Long professionalId,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Boolean active
) {

    public static WeeklyAvailabilityResponse fromEntity(
            WeeklyAvailabilityEntity availability) {

        return new WeeklyAvailabilityResponse(
                availability.getId(),
                availability.getProfessionalId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getActive()
        );
    }
}
