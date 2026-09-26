package co.edu.unicauca.piedrazul.scheduling.presentation;

import co.edu.unicauca.piedrazul.scheduling.application.AvailableSlotService;
import co.edu.unicauca.piedrazul.scheduling.presentation.dto.AvailableSlotResponse;
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
class AvailableSlotControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AvailableSlotService service;

    @InjectMocks
    private AvailableSlotController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetAvailableSlots() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 12);
        AvailableSlotResponse slot1 = new AvailableSlotResponse(LocalTime.of(8, 0), LocalTime.of(8, 30));
        AvailableSlotResponse slot2 = new AvailableSlotResponse(LocalTime.of(8, 30), LocalTime.of(9, 0));

        when(service.getAvailableSlots(10L, date)).thenReturn(List.of(slot1, slot2));

        mockMvc.perform(get("/api/availability/professional/10/slots")
                        .param("date", "2026-10-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").value("08:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("08:30:00"))
                .andExpect(jsonPath("$[1].startTime").value("08:30:00"))
                .andExpect(jsonPath("$[1].endTime").value("09:00:00"));
    }
}
