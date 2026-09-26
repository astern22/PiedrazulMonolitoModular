package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
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
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;

    private RegisterRequest request;
    private RoleEntity patientRole;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest("juanperez", "Secret123!", "Juan Perez", "juan@example.com");
        patientRole = new RoleEntity();
        patientRole.setName("PATIENT");
    }

    @Test
    void testRegister_Success() {
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashedPassword");
        when(roleRepository.findByName("PATIENT")).thenReturn(Optional.of(patientRole));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserEntity result = registerUserService.register(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("juanperez", result.getUsername());
        assertEquals("Juan Perez", result.getFullName());
        assertEquals("juan@example.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getRoles().contains(patientRole));

        verify(passwordEncoder).encode("Secret123!");
        verify(roleRepository).findByName("PATIENT");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testRegister_RoleNotFound_ThrowsException() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName("PATIENT")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registerUserService.register(request)
        );

        assertEquals("Rol PATIENT no encontrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
