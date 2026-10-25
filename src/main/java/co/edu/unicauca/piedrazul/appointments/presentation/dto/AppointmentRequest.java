package co.edu.unicauca.piedrazul.appointments.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(
        @NotNull(message = "El paciente es obligatorio")
        Long patientId,

        @NotNull(message = "El profesional es obligatorio")
        Long professionalId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate appointmentDate,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime endTime
) {}