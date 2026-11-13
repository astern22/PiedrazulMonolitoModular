package co.edu.unicauca.piedrazul.scheduling.presentation;

import co.edu.unicauca.piedrazul.scheduling.application.WeeklyAvailabilityService;
import co.edu.unicauca.piedrazul.scheduling.infrastructure.persistence.WeeklyAvailabilityEntity;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.WeeklyAvailabilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WeeklyAvailabilityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WeeklyAvailabilityService service;

    @InjectMocks
    private WeeklyAvailabilityController controller;

    private WeeklyAvailabilityEntity sampleEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleEntity = new WeeklyAvailabilityEntity();
        sampleEntity.setId(1L);
        sampleEntity.setProfessionalId(10L);
        sampleEntity.setDayOfWeek(1);
        sampleEntity.setStartTime(LocalTime.of(8, 0));
        sampleEntity.setEndTime(LocalTime.of(12, 0));
        sampleEntity.setActive(true);
    }

    @Test
    void testCreateAvailability() throws Exception {
        when(service.create(any(WeeklyAvailabilityRequest.class))).thenReturn(sampleEntity);

        String json = """
                {
                    "professionalId": 10,
                    "dayOfWeek": 1,
                    "startTime": "08:00:00",
                    "endTime": "12:00:00"
                }
                """;

        mockMvc.perform(post("/api/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.professionalId").value(10L))
                .andExpect(jsonPath("$.dayOfWeek").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void testFindByProfessional() throws Exception {
        when(service.findByProfessional(10L)).thenReturn(List.of(sampleEntity));

        mockMvc.perform(get("/api/availability/professional/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].professionalId").value(10L));
    }

    @Test
    void testFindByProfessionalAndDay() throws Exception {
        when(service.findByProfessionalAndDay(10L, 1)).thenReturn(List.of(sampleEntity));

        mockMvc.perform(get("/api/availability/professional/10/day/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].dayOfWeek").value(1));
    }

    @Test
    void testDeactivate() throws Exception {
        doNothing().when(service).deactivate(1L);

        mockMvc.perform(patch("/api/availability/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(service).deactivate(1L);
    }
}
