package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalAccessGuardTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfessionalAccessGuard guard;

    private UserEntity user;
    private ProfessionalEntity ownProfessional;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(10L);

        ownProfessional = new ProfessionalEntity();
        ownProfessional.setId(1L);
        ownProfessional.setUserId(10L);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private void stubOwnProfessional() {
        when(userRepository.findByUsername("medico1")).thenReturn(Optional.of(user));
        when(professionalRepository.findByUserId(10L)).thenReturn(Optional.of(ownProfessional));
    }

    @Test
    void professionalCanUseOwnProfile() {
        authenticateAs("medico1", "PROFESSIONAL");
        stubOwnProfessional();

        assertDoesNotThrow(() -> guard.assertOwnProfileIfProfessional(1L));
    }

    @Test
    void professionalCannotUseAnotherProfessionalProfile() {
        authenticateAs("medico1", "PROFESSIONAL");
        stubOwnProfessional();

        assertThrows(SecurityException.class,
                () -> guard.assertOwnProfileIfProfessional(2L));
    }

    @Test
    void schedulerIsNotRestricted() {
        authenticateAs("agendador1", "SCHEDULER");

        assertDoesNotThrow(() -> guard.assertOwnProfileIfProfessional(2L));
        verifyNoInteractions(userRepository, professionalRepository);
    }

    @Test
    void currentProfessionalReturnsOwnProfile() {
        authenticateAs("medico1", "PROFESSIONAL");
        stubOwnProfessional();

        assertEquals(1L, guard.currentProfessional().getId());
    }

    @Test
    void currentProfessionalWithoutAuthenticationFails() {
        assertThrows(SecurityException.class, () -> guard.currentProfessional());
    }
}
