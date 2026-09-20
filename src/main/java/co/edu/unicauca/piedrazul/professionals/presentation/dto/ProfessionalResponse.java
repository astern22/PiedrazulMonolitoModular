package co.edu.unicauca.piedrazul.professionals.presentation.dto;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;

public record ProfessionalResponse(
        Long id,
        Long userId,
        Long specialtyId,
        String professionalType,
        Integer appointmentIntervalMinutes,
        Boolean active
) {

    public static ProfessionalResponse fromEntity(
            ProfessionalEntity professional) {

        return new ProfessionalResponse(
                professional.getId(),
                professional.getUserId(),
                professional.getSpecialtyId(),
                professional.getProfessionalType(),
                professional.getAppointmentIntervalMinutes(),
                professional.getActive()
        );
    }
}
