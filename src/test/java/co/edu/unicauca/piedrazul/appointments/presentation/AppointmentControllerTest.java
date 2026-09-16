package co.edu.unicauca.piedrazul.appointments.presentation;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentService;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
    }

    @Test
    void testGetAllAppointments() throws Exception {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(1L);
        entity.setPatientId(10L);
        entity.setProfessionalId(20L);
        entity.setAppointmentDate(LocalDate.of(2026, 9, 20));
        entity.setStartTime(LocalTime.of(9, 0));
        entity.setEndTime(LocalTime.of(10, 0));
        entity.setStatus("SCHEDULED");

        when(appointmentService.getAllAppointments()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].patientId").value(10L))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void testGetAppointmentById() throws Exception {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(5L);
        entity.setPatientId(10L);
        entity.setProfessionalId(20L);
        entity.setStatus("CONFIRMED");

        when(appointmentService.getAppointmentById(5L)).thenReturn(entity);

        mockMvc.perform(get("/api/appointments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}

