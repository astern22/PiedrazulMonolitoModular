package co.edu.unicauca.piedrazul.scheduling.application;

import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityRepository;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.WeeklyAvailabilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyAvailabilityServiceTest {

    @Mock
    private WeeklyAvailabilityRepository repository;

    @InjectMocks
    private WeeklyAvailabilityService service;

    private WeeklyAvailabilityEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new WeeklyAvailabilityEntity();
        sampleEntity.setId(1L);
        sampleEntity.setProfessionalId(10L);
        sampleEntity.setDayOfWeek(1);
        sampleEntity.setStartTime(LocalTime.of(8, 0));
        sampleEntity.setEndTime(LocalTime.of(12, 0));
        sampleEntity.setActive(true);
    }

    @Test
    void testCreate_Success() {
        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L,
                1,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        when(repository.save(any(WeeklyAvailabilityEntity.class))).thenAnswer(i -> {
            WeeklyAvailabilityEntity e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        WeeklyAvailabilityEntity created = service.create(request);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals(10L, created.getProfessionalId());
        assertEquals(1, created.getDayOfWeek());
        assertEquals(LocalTime.of(8, 0), created.getStartTime());
        assertEquals(LocalTime.of(12, 0), created.getEndTime());
        assertTrue(created.getActive());

        verify(repository).save(any(WeeklyAvailabilityEntity.class));
    }

    @Test
    void testCreate_StartTimeAfterEndTime_ThrowsException() {
        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L,
                1,
                LocalTime.of(14, 0),
                LocalTime.of(12, 0)
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(request));
        assertEquals("La hora de inicio debe ser anterior a la hora de finalizacion", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_StartTimeEqualsEndTime_ThrowsException() {
        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L,
                1,
                LocalTime.of(10, 0),
                LocalTime.of(10, 0)
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(request));
        assertEquals("La hora de inicio debe ser anterior a la hora de finalizacion", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testFindByProfessional() {
        when(repository.findByProfessionalIdAndActiveTrue(10L)).thenReturn(List.of(sampleEntity));

        List<WeeklyAvailabilityEntity> result = service.findByProfessional(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getProfessionalId());
        verify(repository).findByProfessionalIdAndActiveTrue(10L);
    }

    @Test
    void testFindByProfessionalAndDay() {
        when(repository.findByProfessionalIdAndDayOfWeek(10L, 1)).thenReturn(List.of(sampleEntity));

        List<WeeklyAvailabilityEntity> result = service.findByProfessionalAndDay(10L, 1);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getDayOfWeek());
        verify(repository).findByProfessionalIdAndDayOfWeek(10L, 1);
    }

    @Test
    void testDeactivate_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repository.save(any(WeeklyAvailabilityEntity.class))).thenAnswer(i -> i.getArgument(0));

        service.deactivate(1L);

        assertFalse(sampleEntity.getActive());
        verify(repository).save(sampleEntity);
    }

    @Test
    void testDeactivate_NotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deactivate(99L));
        assertEquals("Disponibilidad no encontrada", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_OverlappingRange_ThrowsException() {
        when(repository.findByProfessionalIdAndDayOfWeekAndActiveTrue(10L, 1))
                .thenReturn(List.of(sampleEntity));

        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L, 1, LocalTime.of(9, 0), LocalTime.of(13, 0));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(request));

        assertEquals(
                "El horario se cruza con otro ya registrado (08:00 - 12:00)",
                ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_ContiguousRange_IsAllowed() {
        when(repository.findByProfessionalIdAndDayOfWeekAndActiveTrue(10L, 1))
                .thenReturn(List.of(sampleEntity));
        when(repository.save(any(WeeklyAvailabilityEntity.class))).thenAnswer(i -> i.getArgument(0));

        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L, 1, LocalTime.of(12, 0), LocalTime.of(14, 0));

        assertNotNull(service.create(request));
        verify(repository).save(any(WeeklyAvailabilityEntity.class));
    }

    @Test
    void testCreate_InactiveOverlap_IsIgnored() {
        // El repositorio solo devuelve horarios activos: uno inactivo no llega al servicio
        when(repository.findByProfessionalIdAndDayOfWeekAndActiveTrue(10L, 1))
                .thenReturn(List.of());
        when(repository.save(any(WeeklyAvailabilityEntity.class))).thenAnswer(i -> i.getArgument(0));

        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L, 1, LocalTime.of(9, 0), LocalTime.of(11, 0));

        assertNotNull(service.create(request));
    }

    @Test
    void testCreate_OverlappingSchedule_ThrowsException() {
        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L,
                1,
                LocalTime.of(11, 0),
                LocalTime.of(13, 0)
        );

        when(repository.findByProfessionalIdAndDayOfWeekAndActiveTrue(10L, 1))
                .thenReturn(List.of(sampleEntity));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(request));
        assertTrue(ex.getMessage().startsWith("El horario se cruza"));
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_ContiguousSchedule_IsAllowed() {
        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest(
                10L,
                1,
                LocalTime.of(12, 0),
                LocalTime.of(14, 0)
        );

        when(repository.findByProfessionalIdAndDayOfWeekAndActiveTrue(10L, 1))
                .thenReturn(List.of(sampleEntity));
        when(repository.save(any(WeeklyAvailabilityEntity.class))).thenAnswer(i -> i.getArgument(0));

        WeeklyAvailabilityEntity created = service.create(request);

        assertEquals(LocalTime.of(12, 0), created.getStartTime());
        verify(repository).save(any(WeeklyAvailabilityEntity.class));
    }
}