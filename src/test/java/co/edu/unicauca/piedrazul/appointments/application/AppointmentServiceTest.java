package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentService service;

    private AppointmentEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new AppointmentEntity();
        sampleEntity.setId(1L);
        sampleEntity.setPatientId(10L);
        sampleEntity.setProfessionalId(20L);
        sampleEntity.setAppointmentDate(LocalDate.of(2026, 10, 15));
        sampleEntity.setStartTime(LocalTime.of(9, 0));
        sampleEntity.setEndTime(LocalTime.of(10, 0));
        sampleEntity.setStatus("SCHEDULED");
    }

    @Test
    void testCreateAppointment() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        CreateAppointmentCommand command = new CreateAppointmentCommand(
                10L,
                20L,
                futureDate,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        when(repository.save(any(AppointmentEntity.class))).thenAnswer(invocation -> {
            AppointmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        AppointmentEntity created = service.createAppointment(command);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals(10L, created.getPatientId());
        assertEquals(20L, created.getProfessionalId());
        assertEquals("SCHEDULED", created.getStatus());
        assertEquals(futureDate, created.getAppointmentDate());
        assertEquals(LocalTime.of(9, 0), created.getStartTime());
        assertEquals(LocalTime.of(10, 0), created.getEndTime());

        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("SCHEDULED", captor.getValue().getStatus());
    }

    @Test
    void testCreateAppointment_TodayIsAllowed() {
        CreateAppointmentCommand command = new CreateAppointmentCommand(
                10L, 20L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0));

        when(repository.save(any(AppointmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentEntity created = service.createAppointment(command);

        assertEquals(LocalDate.now(), created.getAppointmentDate());
        verify(repository).save(any(AppointmentEntity.class));
    }

    @Test
    void testCreateAppointment_PastDate_ThrowsException() {
        CreateAppointmentCommand command = new CreateAppointmentCommand(
                10L, 20L, LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createAppointment(command));

        assertEquals("No se puede agendar una cita en una fecha anterior a hoy.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAppointmentsByPatientId() {
        when(repository.findByPatientId(10L)).thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> results = service.getAppointmentsByPatientId(10L);

        assertEquals(1, results.size());
        assertEquals(10L, results.getFirst().getPatientId());
        verify(repository).findByPatientId(10L);
    }

    @Test
    void testGetAppointmentById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        AppointmentEntity result = service.getAppointmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void testGetAppointmentById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        AppointmentEntity result = service.getAppointmentById(99L);

        assertNull(result);
        verify(repository).findById(99L);
    }

    @Test
    void testGetAppointmentByProfessionalId_Found() {
        when(repository.findByProfessionalId(20L)).thenReturn(List.of(sampleEntity));

        AppointmentEntity result = service.getAppointmentByProfessionalId(20L);

        assertNotNull(result);
        assertEquals(20L, result.getProfessionalId());
        verify(repository).findByProfessionalId(20L);
    }

    @Test
    void testGetAppointmentByProfessionalId_NotFound() {
        when(repository.findByProfessionalId(99L)).thenReturn(Collections.emptyList());

        AppointmentEntity result = service.getAppointmentByProfessionalId(99L);

        assertNull(result);
        verify(repository).findByProfessionalId(99L);
    }

    @Test
    void testGetAllAppointments() {
        when(repository.findAll()).thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> list = service.getAllAppointments();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(1L, list.getFirst().getId());
        verify(repository).findAll();
    }

    @Test
    void testUpdateAppointment() {
        sampleEntity.setStatus("COMPLETED");
        when(repository.save(sampleEntity)).thenReturn(sampleEntity);

        AppointmentEntity updated = service.updateAppointment(sampleEntity);

        assertNotNull(updated);
        assertEquals("COMPLETED", updated.getStatus());
        verify(repository).save(sampleEntity);
    }

    @Test
    void testDeleteAppointment() {
        doNothing().when(repository).deleteById(1L);

        service.deleteAppointment(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void testFindAppointments() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        when(repository.findByProfessionalIdAndAppointmentDate(20L, date))
                .thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> results = service.findAppointments(20L, date);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(repository).findByProfessionalIdAndAppointmentDate(20L, date);
    }
}