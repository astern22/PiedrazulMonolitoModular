package co.edu.unicauca.piedrazul.users.application;

import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private static final String DOCUMENT_NUMBER = "1061789234";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;

    private RegisterRequest request;
    private RoleEntity patientRole;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest(
                "juanperez", "Secret123!", "Juan Perez", "juan@example.com", DOCUMENT_NUMBER);
        patientRole = new RoleEntity();
        patientRole.setName("PATIENT");
    }

    private void stubSuccessfulUserCreation() {
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashedPassword");
        when(roleRepository.findByName("PATIENT")).thenReturn(Optional.of(patientRole));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
    }

    @Test
    void testRegister_Success_CreatesNewPatient() {
        when(patientRepository.findByDocumentNumber(DOCUMENT_NUMBER)).thenReturn(Optional.empty());
        stubSuccessfulUserCreation();

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

        ArgumentCaptor<PatientEntity> captor = ArgumentCaptor.forClass(PatientEntity.class);
        verify(patientRepository).save(captor.capture());
        assertEquals(DOCUMENT_NUMBER, captor.getValue().getDocumentNumber());
        assertEquals(10L, captor.getValue().getUserId());
    }

    @Test
    void testRegister_TrimsDocumentNumber() {
        RegisterRequest requestWithSpaces = new RegisterRequest(
                "juanperez", "Secret123!", "Juan Perez", "juan@example.com", "  " + DOCUMENT_NUMBER + "  ");
        when(patientRepository.findByDocumentNumber(DOCUMENT_NUMBER)).thenReturn(Optional.empty());
        stubSuccessfulUserCreation();

        registerUserService.register(requestWithSpaces);

        verify(patientRepository).findByDocumentNumber(DOCUMENT_NUMBER);
        ArgumentCaptor<PatientEntity> captor = ArgumentCaptor.forClass(PatientEntity.class);
        verify(patientRepository).save(captor.capture());
        assertEquals(DOCUMENT_NUMBER, captor.getValue().getDocumentNumber());
    }

    @Test
    void testRegister_LinksExistingPatientWithoutAccount() {
        PatientEntity existingPatient = new PatientEntity();
        existingPatient.setId(5L);
        existingPatient.setDocumentNumber(DOCUMENT_NUMBER);
        existingPatient.setUserId(null);

        when(patientRepository.findByDocumentNumber(DOCUMENT_NUMBER)).thenReturn(Optional.of(existingPatient));
        stubSuccessfulUserCreation();

        UserEntity result = registerUserService.register(request);

        assertEquals(10L, result.getId());
        assertEquals(10L, existingPatient.getUserId());
        assertEquals(5L, existingPatient.getId());
        verify(patientRepository).save(existingPatient);
    }

    @Test
    void testRegister_DocumentAlreadyHasAccount_ThrowsException() {
        PatientEntity existingPatient = new PatientEntity();
        existingPatient.setId(5L);
        existingPatient.setDocumentNumber(DOCUMENT_NUMBER);
        existingPatient.setUserId(99L);

        when(patientRepository.findByDocumentNumber(DOCUMENT_NUMBER)).thenReturn(Optional.of(existingPatient));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registerUserService.register(request)
        );

        assertEquals("Ya existe una cuenta asociada a este numero de documento", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void testRegister_RoleNotFound_ThrowsException() {
        when(patientRepository.findByDocumentNumber(DOCUMENT_NUMBER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName("PATIENT")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                registerUserService.register(request)
        );

        assertEquals("Rol PATIENT no encontrado", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(patientRepository, never()).save(any());
    }
}