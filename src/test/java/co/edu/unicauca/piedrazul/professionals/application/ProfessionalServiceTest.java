package co.edu.unicauca.piedrazul.professionals.application;

import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
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

    @InjectMocks
    private ProfessionalService service;

    private ProfessionalEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new ProfessionalEntity();
        sampleEntity.setId(1L);
        sampleEntity.setUserId(10L);
        sampleEntity.setSpecialtyId(2L);
        sampleEntity.setProfessionalType("DOCTOR");
        sampleEntity.setAppointmentIntervalMinutes(30);
        sampleEntity.setActive(true);
    }

    @Test
    void testCreate_Success() {
        ProfessionalRequest request = new ProfessionalRequest(10L, 2L, "DOCTOR", 30);

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
        assertEquals("DOCTOR", created.getProfessionalType());
        assertEquals(30, created.getAppointmentIntervalMinutes());
        assertTrue(created.getActive());

        verify(repository).save(any(ProfessionalEntity.class));
    }

    @Test
    void testCreate_UserAlreadyRegisteredAsProfessional_ThrowsException() {
        ProfessionalRequest request = new ProfessionalRequest(10L, 2L, "DOCTOR", 30);
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
}
