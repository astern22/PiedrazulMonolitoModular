package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.SpecialtyEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.SpecialtyRepository;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialtyService {

    private final SpecialtyRepository repository;

    public SpecialtyService(SpecialtyRepository repository) {
        this.repository = repository;
    }

    public SpecialtyResponse create(SpecialtyRequest request) {

        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException(
                    "La especialidad ya existe"
            );
        }

        SpecialtyEntity entity = new SpecialtyEntity();
        entity.setName(request.name());

        SpecialtyEntity saved = repository.save(entity);

        return new SpecialtyResponse(
                saved.getId(),
                saved.getName()
        );
    }

    public List<SpecialtyResponse> findAll() {

        return repository.findAll()
                .stream()
                .map(entity ->
                        new SpecialtyResponse(
                                entity.getId(),
                                entity.getName()
                        )
                )
                .toList();
    }
}
