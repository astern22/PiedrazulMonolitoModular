package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.SpecialtyRepository;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRegistrationRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRequest;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProfessionalService {

    /** Tipos de profesional que existen en el centro de medicina alternativa. */
    public static final Set<String> ALLOWED_TYPES = Set.of("MEDICO", "TERAPEUTA");

    private static final int MIN_INTERVAL_MINUTES = 5;
    private static final int MAX_INTERVAL_MINUTES = 480;

    private final ProfessionalRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessionalService(
            ProfessionalRepository repository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            SpecialtyRepository specialtyRepository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.specialtyRepository = specialtyRepository;
        this.passwordEncoder = passwordEncoder;
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

    /**
     * Crea la cuenta de usuario (rol PROFESSIONAL) y el profesional en un solo paso.
     */
    @Transactional
    public ProfessionalEntity register(ProfessionalRegistrationRequest request) {

        String type = request.professionalType().trim().toUpperCase();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "El tipo de profesional debe ser MEDICO o TERAPEUTA"
            );
        }

        if (!specialtyRepository.existsById(request.specialtyId())) {
            throw new IllegalArgumentException(
                    "La especialidad seleccionada no existe"
            );
        }

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

        RoleEntity professionalRole = roleRepository
                .findByName("PROFESSIONAL")
                .orElseThrow(() ->
                        new IllegalStateException("Rol PROFESSIONAL no encontrado"));

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.getRoles().add(professionalRole);

        UserEntity savedUser = userRepository.save(user);

        ProfessionalEntity professional = new ProfessionalEntity();
        professional.setUserId(savedUser.getId());
        professional.setSpecialtyId(request.specialtyId());
        professional.setProfessionalType(type);
        professional.setAppointmentIntervalMinutes(
                request.appointmentIntervalMinutes()
        );
        professional.setActive(true);

        return repository.save(professional);
    }

    /**
     * Cambia la duracion de las citas del profesional. Las citas ya agendadas
     * conservan su horario; solo se recalculan las franjas libres futuras.
     */
    @Transactional
    public ProfessionalEntity updateAppointmentInterval(Long id, Integer minutes) {

        if (minutes == null
                || minutes < MIN_INTERVAL_MINUTES
                || minutes > MAX_INTERVAL_MINUTES) {
            throw new IllegalArgumentException(
                    "La duracion de la cita debe estar entre "
                            + MIN_INTERVAL_MINUTES + " y "
                            + MAX_INTERVAL_MINUTES + " minutos"
            );
        }

        ProfessionalEntity professional = findById(id);
        professional.setAppointmentIntervalMinutes(minutes);

        return repository.save(professional);
    }

    /**
     * Devuelve el nombre completo de los usuarios indicados, indexado por id de usuario.
     */
    public Map<Long, String> findFullNamesByUserIds(Collection<Long> userIds) {
        Map<Long, String> names = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return names;
        }

        userRepository.findAllById(userIds)
                .forEach(user -> names.put(user.getId(), user.getFullName()));

        return names;
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
