package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessionalService {

    private final ProfessionalRepository repository;

    public ProfessionalService(
            ProfessionalRepository repository) {

        this.repository = repository;
    }

    public ProfessionalEntity create(
            ProfessionalRequest request) {

        if (repository.existsByUserId(request.userId())) {
            throw new RuntimeException(
                    "El usuario ya esta registrado como profesional"
            );
        }

        ProfessionalEntity professional =
                new ProfessionalEntity();

        professional.setUserId(request.userId());
        professional.setSpecialtyId(request.specialtyId());
        professional.setProfessionalType(
                request.professionalType()
        );
        professional.setAppointmentIntervalMinutes(
                request.appointmentIntervalMinutes()
        );
        professional.setActive(true);

        return repository.save(professional);
    }

    public List<ProfessionalEntity> findAll() {
        return repository.findAll();
    }

    public List<ProfessionalEntity> findActive() {
        return repository.findByActiveTrue();
    }

    public ProfessionalEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Profesional no encontrado"
                        )
                );
    }

    public List<ProfessionalEntity> findBySpecialty(
            Long specialtyId) {

        return repository.findBySpecialtyId(specialtyId);
    }
}
