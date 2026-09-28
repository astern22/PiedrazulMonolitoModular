package co.edu.unicauca.piedrazul.professionals.presentation;

import co.edu.unicauca.piedrazul.professionals.application.ProfessionalService;
import co.edu.unicauca.piedrazul.professionals.infrastructure.persistence.ProfessionalEntity;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfessionalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfessionalService service;

    @InjectMocks
    private ProfessionalController controller;

    private ProfessionalEntity sampleEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleEntity = new ProfessionalEntity();
        sampleEntity.setId(1L);
        sampleEntity.setUserId(10L);
        sampleEntity.setSpecialtyId(2L);
        sampleEntity.setAppointmentIntervalMinutes(30);
        sampleEntity.setActive(true);
    }

    @Test
    void testCreateProfessional() throws Exception {
        when(service.create(any(ProfessionalRequest.class))).thenReturn(sampleEntity);

        String json = """
                {
                    "userId": 10,
                    "specialtyId": 2,
                    "appointmentIntervalMinutes": 30
                }
                """;

        mockMvc.perform(post("/api/professionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.specialtyId").value(2L))
                .andExpect(jsonPath("$.appointmentIntervalMinutes").value(30))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void testFindAll() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleEntity));

        mockMvc.perform(get("/api/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(10L));
    }

    @Test
    void testFindActive() throws Exception {
        when(service.findActive()).thenReturn(List.of(sampleEntity));

        mockMvc.perform(get("/api/professionals/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void testFindById() throws Exception {
        when(service.findById(1L)).thenReturn(sampleEntity);

        mockMvc.perform(get("/api/professionals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testFindBySpecialty() throws Exception {
        when(service.findBySpecialty(2L)).thenReturn(List.of(sampleEntity));

        mockMvc.perform(get("/api/professionals/specialty/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].specialtyId").value(2L));
    }

    @Test
    void testRegisterProfessional_ReturnsFullName() throws Exception {
        when(service.register(any(co.edu.unicauca.piedrazul.professionals.presentation.dto.ProfessionalRegistrationRequest.class)))
                .thenReturn(sampleEntity);
        when(service.findFullNamesByUserIds(any())).thenReturn(java.util.Map.of(10L, "Carlos Perez"));

        String json = """
                {
                    "fullName": "Carlos Perez",
                    "username": "cperez",
                    "email": "cperez@piedrazul.com",
                    "password": "secreto123",
                    "specialtyId": 2,
                    "appointmentIntervalMinutes": 30
                }
                """;

        mockMvc.perform(post("/api/professionals/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Carlos Perez"));
    }

    @Test
    void testUpdateAppointmentInterval() throws Exception {
        sampleEntity.setAppointmentIntervalMinutes(45);
        when(service.updateAppointmentInterval(eq(1L), eq(45))).thenReturn(sampleEntity);

        mockMvc.perform(patch("/api/professionals/1/appointment-interval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentIntervalMinutes\": 45}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentIntervalMinutes").value(45));
    }
}
