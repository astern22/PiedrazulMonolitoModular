package co.edu.unicauca.piedrazul.appointments.presentation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class CancelAppointmentRequest {

    @NotBlank(message = "El motivo de la cancelación es obligatorio.")
    @JsonAlias({"motivo", "cancellationReason", "justificacion"})
    private String reason;

    public CancelAppointmentRequest() {
    }

    public CancelAppointmentRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
