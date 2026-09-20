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
