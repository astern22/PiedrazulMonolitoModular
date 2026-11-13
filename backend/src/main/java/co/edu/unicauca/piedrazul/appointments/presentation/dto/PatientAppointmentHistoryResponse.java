package co.edu.unicauca.piedrazul.appointments.presentation.dto;

import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import java.util.List;

public record PatientAppointmentHistoryResponse(
        PatientResponse patient,
        List<AppointmentHistoryItemResponse> appointments
) {
}

