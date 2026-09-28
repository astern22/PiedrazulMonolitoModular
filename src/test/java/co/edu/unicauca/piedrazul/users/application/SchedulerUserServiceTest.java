package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.SchedulerRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SchedulerUserService service;

    private SchedulerRegistrationRequest request() {
        return new SchedulerRegistrationRequest(
                "Ana Gomez", "agomez", "agomez@piedrazul.com", "secreto123");
    }

    @Test
    void testCreateScheduler_AssignsSchedulerRole() {
        RoleEntity role = new RoleEntity();
        role.setName("SCHEDULER");

        when(userRepository.existsByUsername("agomez")).thenReturn(false);
        when(userRepository.existsByEmail("agomez@piedrazul.com")).thenReturn(false);
        when(roleRepository.findByName("SCHEDULER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secreto123")).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserEntity created = service.createScheduler(request());

        assertEquals("agomez", created.getUsername());
        assertEquals("Ana Gomez", created.getFullName());
        assertEquals("hash", created.getPassword());
        assertTrue(created.isEnabled());
        assertTrue(created.getRoles().contains(role));
    }

    @Test
    void testCreateScheduler_UsernameAlreadyExists_ThrowsException() {
        when(userRepository.existsByUsername("agomez")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createScheduler(request()));

        assertEquals("El nombre de usuario ya esta en uso", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateScheduler_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByUsername("agomez")).thenReturn(false);
        when(userRepository.existsByEmail("agomez@piedrazul.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createScheduler(request()));

        assertEquals("El correo electronico ya esta registrado", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
