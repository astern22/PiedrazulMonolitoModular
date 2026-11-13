package co.edu.unicauca.piedrazul.appointments.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        Long professionalId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        String status
) {
}
