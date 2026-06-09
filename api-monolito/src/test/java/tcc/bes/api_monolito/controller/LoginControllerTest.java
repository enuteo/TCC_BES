package tcc.bes.api_monolito.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tcc.bes.api_monolito.dto.LoginRequestDTO;
import tcc.bes.api_monolito.dto.LoginResponseDTO;
import tcc.bes.api_monolito.dto.UserDTO;
import tcc.bes.api_monolito.exception.InvalidCredentialsException;
import tcc.bes.api_monolito.exception.GlobalExceptionHandler;
import tcc.bes.api_monolito.exception.UserNotFoundException;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.service.LoginService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tcc.bes.api_monolito.filter.CorrelationIdFilter.CORRELATION_ID_HEADER;

@WebMvcTest(LoginController.class)
@Import({GlobalExceptionHandler.class, CorrelationIdFilter.class})
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @Test
    void shouldAuthenticateWhenPayloadIsValid() throws Exception {
        LoginResponseDTO response = new LoginResponseDTO(
                true,
                "Login successful",
                new UserDTO(1L, "admin", "admin@example.com", "Admin", "User")
        );

        when(loginService.authenticate(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.username").value("admin"));

        verify(loginService).authenticate(any(LoginRequestDTO.class));
    }

    @Test
    void shouldRejectBlankUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header(CORRELATION_ID_HEADER, "teste-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.correlationId").value("teste-123"))
                .andExpect(jsonPath("$.fields[0].field").value("username"))
                .andExpect(jsonPath("$.fields[0].message").value("Username cannot be empty"));

        verifyNoInteractions(loginService);
    }

    @Test
    void shouldRejectBlankPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields[0].field").value("password"))
                .andExpect(jsonPath("$.fields[0].message").value("Password cannot be empty"));

        verifyNoInteractions(loginService);
    }

    @Test
    void shouldRejectFieldsAboveMaximumLength() throws Exception {
        String username = "a".repeat(101);
        String password = "b".repeat(256);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.length()").value(2))
                .andExpect(jsonPath("$.fields[0].field").value("password"))
                .andExpect(jsonPath("$.fields[0].message").value("Password must have at most 255 characters"))
                .andExpect(jsonPath("$.fields[1].field").value("username"))
                .andExpect(jsonPath("$.fields[1].message").value("Username must have at most 100 characters"));

        verifyNoInteractions(loginService);
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(loginService.authenticate(any(LoginRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.method").value("POST"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(loginService.authenticate(any(LoginRequestDTO.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "missing",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("Malformed request body"))
                .andExpect(jsonPath("$.fields.length()").value(0));

        verifyNoInteractions(loginService);
    }

    @Test
    void shouldReturnInternalErrorWithoutExposingExceptionDetails() throws Exception {
        when(loginService.authenticate(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("database password leaked"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("database password leaked"))));
    }
}
