package co.edu.unicauca.piedrazul.professionals.presentation;

import co.edu.unicauca.piedrazul.professionals.application.SpecialtyService;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyRequest;
import co.edu.unicauca.piedrazul.professionals.presentation.dto.SpecialtyResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SpecialtyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SpecialtyService service;

    @InjectMocks
    private SpecialtyController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testCreateSpecialty() throws Exception {
        SpecialtyResponse response = new SpecialtyResponse(1L, "Cardiologia");
        when(service.create(any(SpecialtyRequest.class))).thenReturn(response);

        String json = """
                {
                    "name": "Cardiologia"
                }
                """;

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cardiologia"));
    }

    @Test
    void testFindAll() throws Exception {
        SpecialtyResponse response = new SpecialtyResponse(1L, "Cardiologia");
        when(service.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Cardiologia"));
    }
}
