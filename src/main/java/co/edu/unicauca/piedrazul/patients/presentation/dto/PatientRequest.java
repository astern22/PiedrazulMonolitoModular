package co.edu.unicauca.piedrazul.patients.presentation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientRequest(
        @NotBlank(message = "El numero de documento es obligatorio")
        @Size(max = 20, message = "El numero de documento no debe exceder 20 caracteres")
        String documentNumber,

        @Size(max = 20, message = "El telefono no debe exceder 20 caracteres")
        String phone,

        LocalDate birthDate,

        Long userId
) {
}

