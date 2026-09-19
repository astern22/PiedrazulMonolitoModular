package co.edu.unicauca.piedrazul.appointments.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(
        Long patientId,
        Long professionalId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime
) {}
