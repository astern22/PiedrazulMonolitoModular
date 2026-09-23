package co.edu.unicauca.piedrazul.patients.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    Optional<PatientEntity> findByDocumentNumber(String documentNumber);

    Optional<PatientEntity> findByUserId(Long userId);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByUserId(Long userId);
}

