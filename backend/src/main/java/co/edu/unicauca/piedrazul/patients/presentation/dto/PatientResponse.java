package co.edu.unicauca.piedrazul.patients.presentation.dto;

import java.time.LocalDate;

import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;

public record PatientResponse(
        Long id,
        Long userId,
        String documentNumber,
        String phone,
        LocalDate birthDate,
        String fullName,
        String email
) {
    public static PatientResponse fromEntity(PatientEntity entity) {
        return new PatientResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getDocumentNumber(),
                entity.getPhone(),
                entity.getBirthDate(),
                null,
                null
        );
    }

    public static PatientResponse fromEntity(PatientEntity entity, String fullName, String email) {
        return new PatientResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getDocumentNumber(),
                entity.getPhone(),
                entity.getBirthDate(),
                fullName,
                email
        );
    }
}

