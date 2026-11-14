package co.edu.unicauca.piedrazul.appointments.presentation;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentService;
import co.edu.unicauca.piedrazul.appointments.infrastructure.persistence.AppointmentEntity;
import co.edu.unicauca.piedrazul.appointments.presentation.command.CreateAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.AppointmentHistoryItemResponse;
import co.edu.unicauca.piedrazul.appointments.presentation.dto.PatientAppointmentHistoryResponse;
import co.edu.unicauca.piedrazul.common.GlobalExceptionHandler;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
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
                mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
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

                when(appointmentService.createAppointment(any(CreateAppointmentCommand.class)))
                                .thenReturn(createdEntity);

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
        void testCreateAppointment_PastDate_ReturnsBadRequest() throws Exception {
                when(appointmentService.createAppointment(any(CreateAppointmentCommand.class)))
                                .thenThrow(new IllegalArgumentException(
                                                "No se puede agendar una cita en una fecha anterior a hoy."));

                String jsonRequest = """
                                {
                                    "patientId": 10,
                                    "professionalId": 20,
                                    "appointmentDate": "2020-01-01",
                                    "startTime": "09:00:00",
                                    "endTime": "10:00:00"
                                }
                                """;

                mockMvc.perform(post("/api/appointments/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error")
                                                .value("No se puede agendar una cita en una fecha anterior a hoy."));
        }

        @Test
        void testGetAppointmentsByPatientId() throws Exception {
                AppointmentEntity entity = new AppointmentEntity();
                entity.setId(1L);
                entity.setPatientId(10L);
                entity.setProfessionalId(20L);
                entity.setStatus("SCHEDULED");

                when(appointmentService.getAppointmentsByPatientId(10L)).thenReturn(List.of(entity));

                mockMvc.perform(get("/api/appointments/patient/10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1L))
                                .andExpect(jsonPath("$[0].patientId").value(10L));
        }

        @Test
        void testGetAppointmentsByPatientId_Empty() throws Exception {
                when(appointmentService.getAppointmentsByPatientId(99L)).thenReturn(List.of());

                mockMvc.perform(get("/api/appointments/patient/99"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isEmpty());
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
        void testGetAppointmentsByProfessionalId_Found() throws Exception {
                AppointmentEntity entity = new AppointmentEntity();
                entity.setId(8L);
                entity.setPatientId(12L);
                entity.setProfessionalId(20L);
                entity.setStatus("SCHEDULED");

                when(appointmentService.getAppointmentsByProfessionalId(20L)).thenReturn(List.of(entity));

                mockMvc.perform(get("/api/appointments/professional/20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(8L))
                                .andExpect(jsonPath("$[0].professionalId").value(20L));
        }

        @Test
        void testGetAppointmentsByProfessionalId_NoAppointments_ReturnsEmptyList() throws Exception {
                when(appointmentService.getAppointmentsByProfessionalId(99L)).thenReturn(List.of());

                mockMvc.perform(get("/api/appointments/professional/99"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
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
                when(appointmentService.updateAppointment(any(AppointmentEntity.class)))
                                .thenAnswer(i -> i.getArgument(0));

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

        @Test
        void testSearchAppointments_OnlyDate() throws Exception {
                AppointmentEntity entity = new AppointmentEntity();
                entity.setId(2L);
                entity.setAppointmentDate(LocalDate.of(2026, 9, 20));

                when(appointmentService.findAppointments(org.mockito.ArgumentMatchers.isNull(),
                                eq(LocalDate.of(2026, 9, 20))))
                                .thenReturn(List.of(entity));

                mockMvc.perform(get("/api/appointments/search")
                                .param("date", "2026-09-20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(2L));
        }

        @Test
        void testGetMyAppointments_UsesAuthenticatedUser() throws Exception {
                AppointmentEntity entity = new AppointmentEntity();
                entity.setId(1L);
                entity.setPatientId(10L);
                when(appointmentService.getAppointmentsByProfessionalDate("ana", null, null))
                                .thenReturn(List.of(entity));

                var auth = new UsernamePasswordAuthenticationToken("ana", null,
                                List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));

                mockMvc.perform(get("/api/appointments/me").principal(auth))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].patientId").value(10L));
                verify(appointmentService, never()).getAllAppointments();
        }

        @Test
        void testCreateAppointment_AsPatient_IgnoresPatientIdFromBody() throws Exception {
                AppointmentEntity created = new AppointmentEntity();
                created.setId(1L);
                created.setPatientId(10L);
                created.setStatus("SCHEDULED");
                when(appointmentService.getPatientIdByUsername("ana")).thenReturn(10L);
                when(appointmentService.createAppointment(any(CreateAppointmentCommand.class))).thenReturn(created);

                var auth = new UsernamePasswordAuthenticationToken("ana", null,
                                List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));

                // El cliente intenta agendar a nombre del paciente 99
                String json = """
                                {"patientId": 99, "professionalId": 20, "appointmentDate": "2026-09-20",
                                 "startTime": "09:00:00", "endTime": "10:00:00"}
                                """;

                mockMvc.perform(post("/api/appointments/create").principal(auth)
                                .contentType(MediaType.APPLICATION_JSON).content(json))
                                .andExpect(status().isOk());

                verify(appointmentService).createAppointment(
                                argThat(cmd -> cmd.patientId().equals(10L)));
        }

        // ==========================================
        // E4-HU3: Listar citas medicas por cedula
        // ==========================================

        @Test
        void testCA1_GetAppointmentHistoryByDocument_Success() throws Exception {
                String doc = "1061789234";
                PatientResponse patient = new PatientResponse(10L, 2L, doc, "3001234567", LocalDate.of(1995, 5, 20),
                                "Carlos Gomez", "carlos@mail.com");
                AppointmentHistoryItemResponse item = new AppointmentHistoryItemResponse(
                                101L, 10L, 20L, LocalDate.of(2026, 11, 10),
                                LocalTime.of(10, 0), LocalTime.of(10, 30),
                                "Dr. Roberto Casas", "Medicina General", "ATENDIDA");
                PatientAppointmentHistoryResponse response = new PatientAppointmentHistoryResponse(patient,
                                List.of(item));

                when(appointmentService.getAppointmentHistoryByDocument(doc)).thenReturn(response);

                mockMvc.perform(get("/api/appointments/patient/document/{documentNumber}/history", doc))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.patient.documentNumber").value(doc))
                                .andExpect(jsonPath("$.patient.fullName").value("Carlos Gomez"))
                                .andExpect(jsonPath("$.appointments[0].id").value(101))
                                .andExpect(jsonPath("$.appointments[0].professionalName").value("Dr. Roberto Casas"))
                                .andExpect(jsonPath("$.appointments[0].specialtyName").value("Medicina General"))
                                .andExpect(jsonPath("$.appointments[0].status").value("ATENDIDA"));
        }

        @Test
        void testCA2_GetAppointmentHistoryByDocument_EmptyList() throws Exception {
                String doc = "1061789234";
                PatientResponse patient = new PatientResponse(10L, 2L, doc, "3001234567", LocalDate.of(1995, 5, 20),
                                "Carlos Gomez", "carlos@mail.com");
                PatientAppointmentHistoryResponse response = new PatientAppointmentHistoryResponse(patient,
                                Collections.emptyList());

                when(appointmentService.getAppointmentHistoryByDocument(doc)).thenReturn(response);

                mockMvc.perform(get("/api/appointments/patient/document/{documentNumber}/history", doc))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.patient.documentNumber").value(doc))
                                .andExpect(jsonPath("$.appointments").isEmpty());
        }

        @Test
        void testCA3_GetAppointmentHistoryByDocument_NotFound() throws Exception {
                String doc = "9999999999";
                when(appointmentService.getAppointmentHistoryByDocument(doc))
                                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "No se encontró ningún paciente registrado con la cédula ingresada"));

                mockMvc.perform(get("/api/appointments/patient/document/{documentNumber}/history", doc))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value(
                                                "No se encontró ningún paciente registrado con la cédula ingresada"));
        }

        @Test
        void testCA4_GetAppointmentHistoryByDocument_InvalidFormat() throws Exception {
                String doc = "invalid-doc";
                when(appointmentService.getAppointmentHistoryByDocument(doc))
                                .thenThrow(new IllegalArgumentException(
                                                "El campo es obligatorio y solo acepta caracteres numéricos válidos."));

                mockMvc.perform(get("/api/appointments/patient/document/{documentNumber}/history", doc))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(
                                                "El campo es obligatorio y solo acepta caracteres numéricos válidos."));
        }
}
