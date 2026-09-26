package co.edu.unicauca.piedrazul.appointments.presentation;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentService;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void testCreateAppointment() throws Exception {
        AppointmentEntity createdEntity = new AppointmentEntity();
        createdEntity.setId(1L);
        createdEntity.setPatientId(10L);
        createdEntity.setProfessionalId(20L);
        createdEntity.setAppointmentDate(LocalDate.of(2026, 9, 20));
        createdEntity.setStartTime(LocalTime.of(9, 0));
        createdEntity.setEndTime(LocalTime.of(10, 0));
        createdEntity.setStatus("SCHEDULED");

        when(appointmentService.createAppointment(any(CreateAppointmentCommand.class))).thenReturn(createdEntity);

        String jsonRequest = """
                {
                    "patientId": 10,
                    "professionalId": 20,
                    "appointmentDate": "2026-09-20",
                    "startTime": "09:00:00",
                    "endTime": "10:00:00"
                }
                """;

        mockMvc.perform(post("/api/appointments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.patientId").value(10L))
                .andExpect(jsonPath("$.professionalId").value(20L))
                .andExpect(jsonPath("$.appointmentDate").value("2026-09-20"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
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
    void testGetAppointmentById_Found() throws Exception {
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

    @Test
    void testGetAppointmentById_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/appointments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAppointmentByProfessionalId_Found() throws Exception {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(8L);
        entity.setPatientId(12L);
        entity.setProfessionalId(20L);
        entity.setStatus("SCHEDULED");

        when(appointmentService.getAppointmentByProfessionalId(20L)).thenReturn(entity);

        mockMvc.perform(get("/api/appointments/professional/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8L))
                .andExpect(jsonPath("$.professionalId").value(20L));
    }

    @Test
    void testGetAppointmentByProfessionalId_NotFound() throws Exception {
        when(appointmentService.getAppointmentByProfessionalId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/appointments/professional/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAppointment_Success() throws Exception {
        AppointmentEntity existing = new AppointmentEntity();
        existing.setId(1L);
        existing.setPatientId(10L);
        existing.setProfessionalId(20L);
        existing.setAppointmentDate(LocalDate.of(2026, 9, 20));
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setEndTime(LocalTime.of(10, 0));
        existing.setStatus("SCHEDULED");

        when(appointmentService.getAppointmentById(1L)).thenReturn(existing);
        when(appointmentService.updateAppointment(any(AppointmentEntity.class))).thenAnswer(i -> i.getArgument(0));

        String updateJson = """
                {
                    "patientId": 10,
                    "professionalId": 20,
                    "appointmentDate": "2026-09-25",
                    "startTime": "10:00:00",
                    "endTime": "11:00:00",
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/appointments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.appointmentDate").value("2026-09-25"));
    }

    @Test
    void testUpdateAppointment_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(99L)).thenReturn(null);

        String updateJson = """
                {
                    "patientId": 10,
                    "professionalId": 20,
                    "appointmentDate": "2026-09-25",
                    "startTime": "10:00:00",
                    "endTime": "11:00:00",
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/appointments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAppointment_Success() throws Exception {
        AppointmentEntity existing = new AppointmentEntity();
        existing.setId(1L);

        when(appointmentService.getAppointmentById(1L)).thenReturn(existing);
        doNothing().when(appointmentService).deleteAppointment(1L);

        mockMvc.perform(delete("/api/appointments/1"))
                .andExpect(status().isNoContent());

        verify(appointmentService).deleteAppointment(1L);
    }

    @Test
    void testDeleteAppointment_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/appointments/99"))
                .andExpect(status().isNotFound());

        verify(appointmentService, never()).deleteAppointment(99L);
    }

    @Test
    void testSearchAppointments() throws Exception {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(1L);
        entity.setProfessionalId(20L);
        entity.setAppointmentDate(LocalDate.of(2026, 9, 20));

        when(appointmentService.findAppointments(eq(20L), eq(LocalDate.of(2026, 9, 20))))
                .thenReturn(List.of(entity));

        mockMvc.perform(get("/api/appointments/search")
                        .param("professionalId", "20")
                        .param("date", "2026-09-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].professionalId").value(20L));
    }
}
