package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.SpecialtyRepository;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRegistrationRequest;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.RoleRepository;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfessionalService service;

    private ProfessionalEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new ProfessionalEntity();
        sampleEntity.setId(1L);
        sampleEntity.setUserId(10L);
        sampleEntity.setSpecialtyId(2L);
        sampleEntity.setAppointmentIntervalMinutes(30);
        sampleEntity.setActive(true);
    }

    @Test
    void testCreate_Success() {
        ProfessionalRequest request = new ProfessionalRequest(10L, 2L, 30);

        when(repository.existsByUserId(10L)).thenReturn(false);
        when(repository.save(any(ProfessionalEntity.class))).thenAnswer(i -> {
            ProfessionalEntity e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        ProfessionalEntity created = service.create(request);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals(10L, created.getUserId());
        assertEquals(2L, created.getSpecialtyId());
        assertEquals(30, created.getAppointmentIntervalMinutes());
        assertTrue(created.getActive());

        verify(repository).save(any(ProfessionalEntity.class));
    }

    @Test
    void testCreate_UserAlreadyRegisteredAsProfessional_ThrowsException() {
        ProfessionalRequest request = new ProfessionalRequest(10L, 2L, 30);
        when(repository.existsByUserId(10L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(request));
        assertEquals("El usuario ya esta registrado como profesional", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(sampleEntity));

        List<ProfessionalEntity> list = service.findAll();

        assertEquals(1, list.size());
        assertEquals(1L, list.getFirst().getId());
        verify(repository).findAll();
    }

    @Test
    void testFindActive() {
        when(repository.findByActiveTrue()).thenReturn(List.of(sampleEntity));

        List<ProfessionalEntity> list = service.findActive();

        assertEquals(1, list.size());
        assertTrue(list.getFirst().getActive());
        verify(repository).findByActiveTrue();
    }

    @Test
    void testFindById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        ProfessionalEntity result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(99L));
        assertEquals("Profesional no encontrado", ex.getMessage());
    }

    @Test
    void testFindBySpecialty() {
        when(repository.findBySpecialtyId(2L)).thenReturn(List.of(sampleEntity));

        List<ProfessionalEntity> list = service.findBySpecialty(2L);

        assertEquals(1, list.size());
        assertEquals(2L, list.getFirst().getSpecialtyId());
        verify(repository).findBySpecialtyId(2L);
    }

    private ProfessionalRegistrationRequest registrationRequest() {
        return new ProfessionalRegistrationRequest(
                "Carlos Perez", "cperez", "cperez@piedrazul.com",
                "secreto123", 2L, 30);
    }

    @Test
    void testRegister_CreatesUserWithProfessionalRoleAndProfessional() {
        RoleEntity role = new RoleEntity();
        role.setName("PROFESSIONAL");

        when(specialtyRepository.existsById(2L)).thenReturn(true);
        when(userRepository.existsByUsername("cperez")).thenReturn(false);
        when(userRepository.existsByEmail("cperez@piedrazul.com")).thenReturn(false);
        when(roleRepository.findByName("PROFESSIONAL")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secreto123")).thenReturn("hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> {
            UserEntity u = i.getArgument(0);
            u.setId(55L);
            return u;
        });
        when(repository.save(any(ProfessionalEntity.class))).thenAnswer(i -> {
            ProfessionalEntity e = i.getArgument(0);
            e.setId(7L);
            return e;
        });

        ProfessionalEntity created = service.register(registrationRequest());

        assertEquals(7L, created.getId());
        assertEquals(55L, created.getUserId());
        assertEquals(30, created.getAppointmentIntervalMinutes());
        assertTrue(created.getActive());

        org.mockito.ArgumentCaptor<UserEntity> captor =
                org.mockito.ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Carlos Perez", captor.getValue().getFullName());
        assertEquals("hash", captor.getValue().getPassword());
        assertTrue(captor.getValue().getRoles().contains(role));
    }

    @Test
    void testRegister_UsernameAlreadyExists_ThrowsException() {
        when(specialtyRepository.existsById(2L)).thenReturn(true);
        when(userRepository.existsByUsername("cperez")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(registrationRequest()));

        assertEquals("El nombre de usuario ya esta en uso", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testRegister_SpecialtyNotFound_ThrowsException() {
        when(specialtyRepository.existsById(2L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(registrationRequest()));

        assertEquals("La especialidad seleccionada no existe", ex.getMessage());
    }

    @Test
    void testUpdateAppointmentInterval_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repository.save(any(ProfessionalEntity.class))).thenAnswer(i -> i.getArgument(0));

        ProfessionalEntity updated = service.updateAppointmentInterval(1L, 45);

        assertEquals(45, updated.getAppointmentIntervalMinutes());
        verify(repository).save(sampleEntity);
    }

    @Test
    void testUpdateAppointmentInterval_OutOfRange_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateAppointmentInterval(1L, 2));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateAppointmentInterval(1L, 500));
        verify(repository, never()).save(any());
    }

    @Test
    void testFindFullNamesByUserIds() {
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setFullName("Carlos Perez");
        when(userRepository.findAllById(List.of(10L))).thenReturn(List.of(user));

        assertEquals("Carlos Perez", service.findFullNamesByUserIds(List.of(10L)).get(10L));
    }
}
