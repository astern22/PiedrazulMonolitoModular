package co.edu.unicauca.piedrazul.patients.application;

import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientEntity;
import co.edu.unicauca.piedrazul.patients.infrastructure.persistence.PatientRepository;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientRequest;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientEntity samplePatient;
    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        samplePatient = new PatientEntity();
        samplePatient.setId(1L);
        samplePatient.setDocumentNumber("12345678");
        samplePatient.setPhone("3001234567");
        samplePatient.setBirthDate(LocalDate.of(1995, 5, 20));
        samplePatient.setUserId(2L);

        sampleUser = new UserEntity();
        sampleUser.setId(2L);
        sampleUser.setUsername("juanperez");
        sampleUser.setFullName("Juan Perez");
        sampleUser.setEmail("juan@example.com");
    }

    @Test
    void testCreate_Success_WithUserId() {
        PatientRequest request = new PatientRequest("12345678", "3001234567", LocalDate.of(1995, 5, 20), 2L);

        when(patientRepository.existsByDocumentNumber("12345678")).thenReturn(false);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(patientRepository.existsByUserId(2L)).thenReturn(false);
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(i -> {
            PatientEntity e = i.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        PatientResponse response = patientService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("12345678", response.documentNumber());
        assertEquals("3001234567", response.phone());
        assertEquals("Juan Perez", response.fullName());
        assertEquals("juan@example.com", response.email());
    }

    @Test
    void testCreate_Success_WithoutUserId() {
        PatientRequest request = new PatientRequest("12345678", "3001234567", LocalDate.of(1995, 5, 20), null);

        when(patientRepository.existsByDocumentNumber("12345678")).thenReturn(false);
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(i -> {
            PatientEntity e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        PatientResponse response = patientService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertNull(response.userId());
        assertNull(response.fullName());
        assertNull(response.email());
    }

    @Test
    void testCreate_DuplicateDocumentNumber_ThrowsException() {
        PatientRequest request = new PatientRequest("12345678", "3001234567", null, null);
        when(patientRepository.existsByDocumentNumber("12345678")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.create(request));
        assertEquals("El numero de documento ya esta registrado", ex.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void testCreate_UserDoesNotExist_ThrowsException() {
        PatientRequest request = new PatientRequest("12345678", "3001234567", null, 99L);
        when(patientRepository.existsByDocumentNumber("12345678")).thenReturn(false);
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.create(request));
        assertEquals("El usuario con ID 99 no existe", ex.getMessage());
    }

    @Test
    void testCreate_UserAlreadyRegisteredAsPatient_ThrowsException() {
        PatientRequest request = new PatientRequest("12345678", "3001234567", null, 2L);
        when(patientRepository.existsByDocumentNumber("12345678")).thenReturn(false);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(patientRepository.existsByUserId(2L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.create(request));
        assertEquals("El usuario ya esta registrado como paciente", ex.getMessage());
    }

    @Test
    void testFindAll() {
        when(patientRepository.findAll()).thenReturn(List.of(samplePatient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        List<PatientResponse> list = patientService.findAll();

        assertEquals(1, list.size());
        assertEquals("12345678", list.getFirst().documentNumber());
    }

    @Test
    void testFindById_Found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        PatientResponse response = patientService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Juan Perez", response.fullName());
    }

    @Test
    void testFindById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.findById(99L));
        assertTrue(ex.getMessage().contains("Paciente no encontrado"));
    }

    @Test
    void testFindByCurrentUser_Success() {
        when(userRepository.findByUsername("juanperez")).thenReturn(Optional.of(sampleUser));
        when(patientRepository.findByUserId(2L)).thenReturn(Optional.of(samplePatient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        PatientResponse response = patientService.findByCurrentUser("juanperez");

        assertNotNull(response);
        assertEquals("12345678", response.documentNumber());
    }

    @Test
    void testFindByCurrentUser_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientService.findByCurrentUser("unknown"));
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    void testFindByCurrentUser_PatientNotLinked() {
        when(userRepository.findByUsername("juanperez")).thenReturn(Optional.of(sampleUser));
        when(patientRepository.findByUserId(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientService.findByCurrentUser("juanperez"));
        assertTrue(ex.getMessage().contains("No se encontro un paciente vinculado"));
    }

    @Test
    void testFindByDocumentNumber_Found() {
        when(patientRepository.findByDocumentNumber("12345678")).thenReturn(Optional.of(samplePatient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        PatientResponse response = patientService.findByDocumentNumber("12345678");

        assertNotNull(response);
        assertEquals("12345678", response.documentNumber());
    }

    @Test
    void testFindByDocumentNumber_NotFound() {
        when(patientRepository.findByDocumentNumber("0000")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                patientService.findByDocumentNumber("0000"));
        assertTrue(ex.getMessage().contains("Paciente no encontrado"));
    }

    @Test
    void testUpdate_Success() {
        PatientRequest request = new PatientRequest("87654321", "3119876543", LocalDate.of(1990, 1, 1), 2L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(patientRepository.findByDocumentNumber("87654321")).thenReturn(Optional.empty());
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        PatientResponse updated = patientService.update(1L, request);

        assertNotNull(updated);
        assertEquals("87654321", updated.documentNumber());
        assertEquals("3119876543", updated.phone());
    }

    @Test
    void testUpdate_NotFound() {
        PatientRequest request = new PatientRequest("87654321", null, null, null);
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientService.update(99L, request));
    }

    @Test
    void testUpdate_DuplicateDocumentBelongingToOtherPatient() {
        PatientRequest request = new PatientRequest("99999999", null, null, 2L);

        PatientEntity otherPatient = new PatientEntity();
        otherPatient.setId(2L);
        otherPatient.setDocumentNumber("99999999");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(patientRepository.findByDocumentNumber("99999999")).thenReturn(Optional.of(otherPatient));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.update(1L, request));
        assertEquals("El numero de documento ya pertenece a otro paciente", ex.getMessage());
    }

    @Test
    void testUpdate_UserIdDoesNotExist() {
        PatientRequest request = new PatientRequest("12345678", null, null, 100L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(userRepository.existsById(100L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.update(1L, request));
        assertEquals("El usuario con ID 100 no existe", ex.getMessage());
    }

    @Test
    void testUpdate_UserAlreadyLinkedToOtherPatient() {
        PatientRequest request = new PatientRequest("12345678", null, null, 100L);

        PatientEntity otherPatient = new PatientEntity();
        otherPatient.setId(2L);
        otherPatient.setUserId(100L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
        when(userRepository.existsById(100L)).thenReturn(true);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(otherPatient));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientService.update(1L, request));
        assertEquals("El usuario ya esta vinculado a otro paciente", ex.getMessage());
    }

    @Test
    void testDelete_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(samplePatient));
        doNothing().when(patientRepository).delete(samplePatient);

        patientService.delete(1L);

        verify(patientRepository).delete(samplePatient);
    }

    @Test
    void testDelete_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientService.delete(99L));
        verify(patientRepository, never()).delete(any());
    }
}
