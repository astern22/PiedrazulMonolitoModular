package co.edu.unicauca.piedrazul.users.presentation;

import co.edu.unicauca.piedrazul.users.application.LoginService;
import co.edu.unicauca.piedrazul.users.application.RegisterUserService;
import co.edu.unicauca.piedrazul.users.infrastructure.persistence.UserEntity;
import co.edu.unicauca.piedrazul.users.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegisterUserService registerUserService;

    @Mock
    private LoginService loginService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegister() throws Exception {
        UserEntity createdUser = new UserEntity();
        createdUser.setId(1L);
        createdUser.setUsername("juanperez");

        when(registerUserService.register(any(RegisterRequest.class))).thenReturn(createdUser);

        String jsonRequest = """
                {
                    "username": "juanperez",
                    "password": "Password123!",
                    "fullName": "Juan Perez",
                    "email": "juan@example.com",
                    "documentNumber": "1061789234"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("juanperez"));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(registerUserService).register(captor.capture());
        assertEquals("1061789234", captor.getValue().documentNumber());
    }

    @Test
    void testLogin() throws Exception {
        when(loginService.login(eq("juanperez"), eq("Password123!"))).thenReturn("sample-jwt-token");

        String jsonRequest = """
                {
                    "username": "juanperez",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("sample-jwt-token"));
    }
}