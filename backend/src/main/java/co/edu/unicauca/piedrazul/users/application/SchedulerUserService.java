package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.SchedulerRegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de las cuentas de agendadores (rol SCHEDULER).
 * Solo el administrador puede crearlas (ver SecurityConfig).
 */
@Service
public class SchedulerUserService {

    public static final String SCHEDULER_ROLE = "SCHEDULER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SchedulerUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserEntity createScheduler(SchedulerRegistrationRequest request) {

        String username = request.username().trim();
        String email = request.email().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "El nombre de usuario ya esta en uso"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "El correo electronico ya esta registrado"
            );
        }

        RoleEntity schedulerRole = roleRepository
                .findByName(SCHEDULER_ROLE)
                .orElseThrow(() ->
                        new IllegalStateException("Rol SCHEDULER no encontrado"));

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.getRoles().add(schedulerRole);

        return userRepository.save(user);
    }

    public List<UserEntity> findSchedulers() {
        return userRepository.findByRoles_NameOrderByFullNameAsc(SCHEDULER_ROLE);
    }
}
