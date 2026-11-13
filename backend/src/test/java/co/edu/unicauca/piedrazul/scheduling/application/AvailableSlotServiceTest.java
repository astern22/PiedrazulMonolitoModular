package co.edu.unicauca.piedrazul.scheduling.application;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentService;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalRepository;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityRepository;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.AvailableSlotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailableSlotServiceTest {

    @Mock
    private WeeklyAvailabilityRepository availabilityRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AvailableSlotService service;

    private ProfessionalEntity professional;

    @BeforeEach
    void setUp() {
        professional = new ProfessionalEntity();
        professional.setId(10L);
        professional.setActive(true);
        professional.setAppointmentIntervalMinutes(30);
    }

    @Test
    void testGetAvailableSlots_ProfessionalNotFound_ThrowsException() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.getAvailableSlots(99L, LocalDate.of(2026, 10, 12)));

        assertEquals("Profesional no encontrado", ex.getMessage());
    }

    @Test
    void testGetAvailableSlots_ProfessionalInactive_ThrowsException() {
        professional.setActive(false);
        when(professionalRepository.findById(10L)).thenReturn(Optional.of(professional));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.getAvailableSlots(10L, LocalDate.of(2026, 10, 12)));

        assertEquals("El profesional no esta activo", ex.getMessage());
    }

    @Test
    void testGetAvailableSlots_NoOccupiedAppointments() {
        LocalDate monday = LocalDate.of(2026, 10, 12); // Day 1 (Monday)
        WeeklyAvailabilityEntity availability = new WeeklyAvailabilityEntity();
        availability.setId(1L);
        availability.setProfessionalId(10L);
        availability.setDayOfWeek(1);
        availability.setStartTime(LocalTime.of(8, 0));
        availability.setEndTime(LocalTime.of(10, 0));
        availability.setActive(true);

        when(professionalRepository.findById(10L)).thenReturn(Optional.of(professional));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(10L, 1))
                .thenReturn(List.of(availability));
        when(appointmentService.findAppointments(10L, monday))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotResponse> slots = service.getAvailableSlots(10L, monday);

        assertNotNull(slots);
        assertEquals(4, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).startTime());
        assertEquals(LocalTime.of(8, 30), slots.get(0).endTime());
        assertEquals(LocalTime.of(8, 30), slots.get(1).startTime());
        assertEquals(LocalTime.of(9, 0), slots.get(1).endTime());
        assertEquals(LocalTime.of(9, 0), slots.get(2).startTime());
        assertEquals(LocalTime.of(9, 30), slots.get(2).endTime());
        assertEquals(LocalTime.of(9, 30), slots.get(3).startTime());
        assertEquals(LocalTime.of(10, 0), slots.get(3).endTime());
    }

    @Test
    void testGetAvailableSlots_WithOverlappingAppointment() {
        LocalDate monday = LocalDate.of(2026, 10, 12); // Day 1 (Monday)
        WeeklyAvailabilityEntity availability = new WeeklyAvailabilityEntity();
        availability.setId(1L);
        availability.setProfessionalId(10L);
        availability.setDayOfWeek(1);
        availability.setStartTime(LocalTime.of(8, 0));
        availability.setEndTime(LocalTime.of(10, 0));
        availability.setActive(true);

        AppointmentEntity booked = new AppointmentEntity();
        booked.setId(100L);
        booked.setStartTime(LocalTime.of(8, 30));
        booked.setEndTime(LocalTime.of(9, 0));

        when(professionalRepository.findById(10L)).thenReturn(Optional.of(professional));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(10L, 1))
                .thenReturn(List.of(availability));
        when(appointmentService.findAppointments(10L, monday))
                .thenReturn(List.of(booked));

        List<AvailableSlotResponse> slots = service.getAvailableSlots(10L, monday);

        assertNotNull(slots);
        assertEquals(3, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).startTime());
        assertEquals(LocalTime.of(9, 0), slots.get(1).startTime());
        assertEquals(LocalTime.of(9, 30), slots.get(2).startTime());
    }

    @Test
    void testGetAvailableSlots_NoAvailabilityForDay() {
        LocalDate tuesday = LocalDate.of(2026, 10, 13); // Day 2 (Tuesday)

        when(professionalRepository.findById(10L)).thenReturn(Optional.of(professional));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(10L, 2))
                .thenReturn(Collections.emptyList());
        when(appointmentService.findAppointments(10L, tuesday))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotResponse> slots = service.getAvailableSlots(10L, tuesday);

        assertNotNull(slots);
        assertTrue(slots.isEmpty());
    }

    @Test
    void testGetAvailableSlots_OverlappingRangesAreMerged_NoDuplicateSlots() {
        LocalDate monday = LocalDate.of(2026, 10, 12);

        WeeklyAvailabilityEntity morning = new WeeklyAvailabilityEntity();
        morning.setDayOfWeek(1);
        morning.setStartTime(LocalTime.of(8, 0));
        morning.setEndTime(LocalTime.of(12, 0));
        morning.setActive(true);

        WeeklyAvailabilityEntity overlapping = new WeeklyAvailabilityEntity();
        overlapping.setDayOfWeek(1);
        overlapping.setStartTime(LocalTime.of(9, 0));
        overlapping.setEndTime(LocalTime.of(12, 0));
        overlapping.setActive(true);

        when(professionalRepository.findById(10L)).thenReturn(Optional.of(professional));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(10L, 1))
                .thenReturn(List.of(morning, overlapping));
        when(appointmentService.findAppointments(10L, monday))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotResponse> slots = service.getAvailableSlots(10L, monday);

        // 08:00 a 12:00 en bloques de 30 min = 8 franjas, sin repetir las de 09:00-12:00
        assertEquals(8, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).startTime());
        assertEquals(LocalTime.of(11, 30), slots.get(7).startTime());
    }
}
