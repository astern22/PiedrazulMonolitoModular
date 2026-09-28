package co.edu.unicauca.piedrazul.professionals.presentation.dto;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;

public record ProfessionalResponse(
        Long id,
        Long userId,
        String fullName,
        Long specialtyId,
        String professionalType,
        Integer appointmentIntervalMinutes,
        Boolean active
) {

    public static ProfessionalResponse fromEntity(
            ProfessionalEntity professional) {

        return fromEntity(professional, null);
    }

    public static ProfessionalResponse fromEntity(
            ProfessionalEntity professional,
            String fullName) {

        return new ProfessionalResponse(
                professional.getId(),
                professional.getUserId(),
                fullName,
                professional.getSpecialtyId(),
                professional.getProfessionalType(),
                professional.getAppointmentIntervalMinutes(),
                professional.getActive()
        );
    }
}
