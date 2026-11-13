package co.edu.unicauca.piedrazul.patients.presentation;

import co.edu.unicauca.piedrazul.patients.application.PatientService;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientRequest;
import co.edu.unicauca.piedrazul.patients.presentation.dto.PatientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private PatientResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
        sampleResponse = new PatientResponse(
                1L,
                2L,
                "12345678",
                "3001234567",
                LocalDate.of(1995, 5, 20),
                "Juan Perez",
                "juan@example.com"
        );
    }

    @Test
    void testCreatePatient() throws Exception {
        when(patientService.create(any(PatientRequest.class))).thenReturn(sampleResponse);

        String json = """
                {
                    "documentNumber": "12345678",
                    "phone": "3001234567",
                    "birthDate": "1995-05-20",
                    "userId": 2
                }
                """;

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678"))
                .andExpect(jsonPath("$.fullName").value("Juan Perez"));
    }

    @Test
    void testFindAll() throws Exception {
        when(patientService.findAll()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].documentNumber").value("12345678"));
    }

    @Test
    void testFindByCurrentUser() throws Exception {
        when(patientService.findByCurrentUser("juanperez")).thenReturn(sampleResponse);

        TestingAuthenticationToken auth = new TestingAuthenticationToken("juanperez", null);

        mockMvc.perform(get("/api/patients/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678"));
    }

    @Test
    void testFindById() throws Exception {
        when(patientService.findById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Juan Perez"));
    }

    @Test
    void testFindByDocumentNumber() throws Exception {
        when(patientService.findByDocumentNumber("12345678")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/patients/document/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678"));
    }

    @Test
    void testUpdatePatient() throws Exception {
        when(patientService.update(eq(1L), any(PatientRequest.class))).thenReturn(sampleResponse);

        String json = """
                {
                    "documentNumber": "12345678",
                    "phone": "3001234567",
                    "birthDate": "1995-05-20",
                    "userId": 2
                }
                """;

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678"));
    }

    @Test
    void testDeletePatient() throws Exception {
        doNothing().when(patientService).delete(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());

        verify(patientService).delete(1L);
    }
}
