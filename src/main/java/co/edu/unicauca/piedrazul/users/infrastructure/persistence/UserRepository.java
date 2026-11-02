package co.edu.unicauca.piedrazul.users.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByKeycloakUserId(String keycloakUserId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UserEntity> findByRoles_NameOrderByFullNameAsc(String roleName);
}
