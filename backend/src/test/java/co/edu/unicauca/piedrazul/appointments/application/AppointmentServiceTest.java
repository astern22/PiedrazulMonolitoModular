package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.patients.application.PatientService;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.PatientAppointmentHistoryResponse;
import co.edu.unicauca.piedrazul.professionals.application.ProfessionalService;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private PatientService patientService;

    @Mock
    private ProfessionalService professionalService;

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
    void testGetAppointmentsByProfessionalId_Found() {
        when(repository.findByProfessionalId(20L)).thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> result = service.getAppointmentsByProfessionalId(20L);

        assertEquals(1, result.size());
        assertEquals(20L, result.getFirst().getProfessionalId());
        verify(repository).findByProfessionalId(20L);
    }

    @Test
    void testGetAppointmentsByProfessionalId_NoAppointments_ReturnsEmptyList() {
        when(repository.findByProfessionalId(99L)).thenReturn(Collections.emptyList());

        List<AppointmentEntity> result = service.getAppointmentsByProfessionalId(99L);

        assertTrue(result.isEmpty());
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

    @Test
    void testFindAppointments_OnlyProfessional() {
        when(repository.findByProfessionalId(20L)).thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> results = service.findAppointments(20L, null);

        assertEquals(1, results.size());
        verify(repository).findByProfessionalId(20L);
    }

    @Test
    void testFindAppointments_OnlyDate() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        when(repository.findByAppointmentDate(date)).thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> results = service.findAppointments(null, date);

        assertEquals(1, results.size());
        verify(repository).findByAppointmentDate(date);
    }

    @Test
    void testFindAppointments_OrderedByDateAndStartTime() {
        AppointmentEntity later = new AppointmentEntity();
        later.setAppointmentDate(LocalDate.of(2026, 10, 16));
        later.setStartTime(LocalTime.of(8, 0));
        AppointmentEntity sameDayLate = new AppointmentEntity();
        sameDayLate.setAppointmentDate(LocalDate.of(2026, 10, 15));
        sameDayLate.setStartTime(LocalTime.of(11, 0));
        // sampleEntity: 2026-10-15 09:00
        when(repository.findByProfessionalId(20L))
                .thenReturn(List.of(later, sameDayLate, sampleEntity));

        List<AppointmentEntity> results = service.findAppointments(20L, null);

        assertEquals(List.of(sampleEntity, sameDayLate, later), results);
    }

    @Test
    void testFindAppointments_NoFilters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.findAppointments(null, null));
    }

    @Test
    void testCreateAppointment_OverlappingSlot_ThrowsConflict() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        sampleEntity.setAppointmentDate(futureDate);
        when(repository.findByProfessionalIdAndAppointmentDate(20L, futureDate))
                .thenReturn(List.of(sampleEntity));

        CreateAppointmentCommand command = new CreateAppointmentCommand(
                11L, 20L, futureDate, LocalTime.of(9, 30), LocalTime.of(10, 30));

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.createAppointment(command));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("El horario ya no está disponible. Por favor selecciona otra franja.", ex.getReason());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAppointment_AdjacentSlot_IsAllowed() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        sampleEntity.setAppointmentDate(futureDate);
        when(repository.findByProfessionalIdAndAppointmentDate(20L, futureDate))
                .thenReturn(List.of(sampleEntity));
        when(repository.save(any(AppointmentEntity.class))).thenAnswer(i -> i.getArgument(0));

        CreateAppointmentCommand command = new CreateAppointmentCommand(
                11L, 20L, futureDate, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertNotNull(service.createAppointment(command));
    }

    @Test
    void testCreateAppointment_CancelledSlotDoesNotBlock() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        sampleEntity.setAppointmentDate(futureDate);
        sampleEntity.setStatus("CANCELLED");
        when(repository.findByProfessionalIdAndAppointmentDate(20L, futureDate))
                .thenReturn(List.of(sampleEntity));
        when(repository.save(any(AppointmentEntity.class))).thenAnswer(i -> i.getArgument(0));

        CreateAppointmentCommand command = new CreateAppointmentCommand(
                11L, 20L, futureDate, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertNotNull(service.createAppointment(command));
    }

    @Test
    void testGetAppointmentsByUsername_OnlyReturnsOwnAppointments() {
        PatientResponse patient = new PatientResponse(10L, 5L, "123", null, null, "Ana", "ana@mail.com");
        when(patientService.findByCurrentUser("ana")).thenReturn(patient);
        when(repository.findByPatientId(10L)).thenReturn(List.of(sampleEntity));

        List<AppointmentEntity> result = service.getAppointmentsByUsername("ana", null, null);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getPatientId());
        verify(repository).findByPatientId(10L);
        verify(repository, never()).findAll();
    }

    @Test
    void testGetAppointmentsByUsername_FiltersWithinOwnAppointments() {
        PatientResponse patient = new PatientResponse(10L, 5L, "123", null, null, "Ana", "ana@mail.com");
        when(patientService.findByCurrentUser("ana")).thenReturn(patient);
        when(repository.findByPatientId(10L)).thenReturn(List.of(sampleEntity));

        assertEquals(1, service.getAppointmentsByUsername("ana", 20L, LocalDate.of(2026, 10, 15)).size());
        assertTrue(service.getAppointmentsByUsername("ana", 99L, null).isEmpty());
        assertTrue(service.getAppointmentsByUsername("ana", null, LocalDate.of(2026, 1, 1)).isEmpty());
    }

    @Test
    void testGetPatientIdByUsername() {
        PatientResponse patient = new PatientResponse(10L, 5L, "123", null, null, "Ana", "ana@mail.com");
        when(patientService.findByCurrentUser("ana")).thenReturn(patient);

        assertEquals(10L, service.getPatientIdByUsername("ana"));
    }

    // ==========================================
    // E4-HU3: Listar citas medicas por cedula
    // ==========================================

    @Test
    void testCA1_GetAppointmentHistory_SuccessWithRecords() {
        String doc = "1061789234";
        PatientResponse patient = new PatientResponse(10L, 2L, doc, "3001234567", LocalDate.of(1995, 5, 20), "Carlos Gomez", "carlos@mail.com");
        when(patientService.findOptionalByDocumentNumber(doc)).thenReturn(Optional.of(patient));

        AppointmentEntity apt1 = new AppointmentEntity();
        apt1.setId(101L);
        apt1.setPatientId(10L);
        apt1.setProfessionalId(20L);
        apt1.setAppointmentDate(LocalDate.of(2026, 11, 10));
        apt1.setStartTime(LocalTime.of(10, 0));
        apt1.setEndTime(LocalTime.of(10, 30));
        apt1.setStatus("ATENDIDA");

        when(repository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(10L))
                .thenReturn(List.of(apt1));

        ProfessionalEntity prof = new ProfessionalEntity();
        prof.setId(20L);
        prof.setUserId(50L);
        prof.setSpecialtyId(3L);
        when(professionalService.findById(20L)).thenReturn(prof);
        when(professionalService.findFullNamesByUserIds(Set.of(50L))).thenReturn(Map.of(50L, "Dr. Roberto Casas"));
        when(professionalService.findSpecialtyNamesByIds(Set.of(3L))).thenReturn(Map.of(3L, "Medicina General"));

        PatientAppointmentHistoryResponse response = service.getAppointmentHistoryByDocument(doc);

        assertNotNull(response);
        assertEquals(patient, response.patient());
        assertEquals(1, response.appointments().size());
        var item = response.appointments().getFirst();
        assertEquals(101L, item.id());
        assertEquals("Dr. Roberto Casas", item.professionalName());
        assertEquals("Medicina General", item.specialtyName());
        assertEquals("ATENDIDA", item.status());
        assertEquals(LocalDate.of(2026, 11, 10), item.appointmentDate());
        assertEquals(LocalTime.of(10, 0), item.startTime());
    }

    @Test
    void testCA2_GetAppointmentHistory_PatientWithoutAppointments() {
        String doc = "1061789234";
        PatientResponse patient = new PatientResponse(10L, 2L, doc, "3001234567", LocalDate.of(1995, 5, 20), "Carlos Gomez", "carlos@mail.com");
        when(patientService.findOptionalByDocumentNumber(doc)).thenReturn(Optional.of(patient));
        when(repository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(10L))
                .thenReturn(Collections.emptyList());

        PatientAppointmentHistoryResponse response = service.getAppointmentHistoryByDocument(doc);

        assertNotNull(response);
        assertEquals(patient, response.patient());
        assertTrue(response.appointments().isEmpty());
    }

    @Test
    void testCA3_GetAppointmentHistory_PatientNotFound() {
        String doc = "9999999999";
        when(patientService.findOptionalByDocumentNumber(doc)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.getAppointmentHistoryByDocument(doc));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("No se encontró ningún paciente registrado con la cédula ingresada"));
    }

    @Test
    void testCA4_GetAppointmentHistory_InvalidDocumentFormat() {
        assertThrows(IllegalArgumentException.class, () -> service.getAppointmentHistoryByDocument(null));
        assertThrows(IllegalArgumentException.class, () -> service.getAppointmentHistoryByDocument(""));
        assertThrows(IllegalArgumentException.class, () -> service.getAppointmentHistoryByDocument("   "));
        assertThrows(IllegalArgumentException.class, () -> service.getAppointmentHistoryByDocument("123ABC456"));
        assertThrows(IllegalArgumentException.class, () -> service.getAppointmentHistoryByDocument("123-456"));
    }
}
