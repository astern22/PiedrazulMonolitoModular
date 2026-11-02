package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Garantiza que un usuario con rol PROFESSIONAL solo trabaje sobre su propio perfil.
 * Los demas roles (ADMIN, SCHEDULER) no tienen esta restriccion.
 */
@Component
public class ProfessionalAccessGuard {

    private static final String ROLE_PROFESSIONAL = "ROLE_PROFESSIONAL";

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public ProfessionalAccessGuard(
            ProfessionalRepository professionalRepository,
            UserRepository userRepository) {

        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    /**
     * Si el usuario autenticado es un profesional, el id recibido debe ser el suyo.
     * Si tiene otro rol, no hace nada.
     */
    public void assertOwnProfileIfProfessional(Long professionalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!hasProfessionalRole(authentication)) {
            return;
        }

        ProfessionalEntity own = findOwnProfessional(authentication.getName());

        if (!own.getId().equals(professionalId)) {
            throw new SecurityException(
                    "No tienes permiso para modificar los datos de otro profesional");
        }
    }

    /** Devuelve el profesional asociado al usuario autenticado. */
    public ProfessionalEntity currentProfessional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new SecurityException("Usuario no autenticado");
        }

        return findOwnProfessional(authentication.getName());
    }

    private boolean hasProfessionalRole(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_PROFESSIONAL.equals(a.getAuthority()));
    }

    private ProfessionalEntity findOwnProfessional(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "No se encontro un profesional asociado a esta cuenta"));
    }
}
