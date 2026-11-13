package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {

    Optional<ProfessionalEntity> findByUserId(Long userId);

    List<ProfessionalEntity> findBySpecialtyId(Long specialtyId);

    List<ProfessionalEntity> findByActiveTrue();

    boolean existsByUserId(Long userId);
}
