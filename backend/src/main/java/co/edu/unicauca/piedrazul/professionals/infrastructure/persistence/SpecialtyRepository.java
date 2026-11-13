package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, Long> {

    boolean existsByNameIgnoreCase(String name);
}
