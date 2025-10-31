package ru.homecrew.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.homecrew.dto.auth.AuthRequest;
import ru.homecrew.dto.auth.AuthResponse;
import ru.homecrew.service.auth.AuthService;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        },
        excludeFilters =
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "ru\\.homecrew\\.config\\.security\\..*"))
@DisplayName("Тесты AuthController — проверка /login")
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AuthService authService;

    @Test
    @DisplayName("Успешный логин возвращает токен")
    void login_success() throws Exception {
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(new AuthResponse("mocked-jwt-token"));

        String json = """
                {"username":"ivan","password":"Valid123!"}
                """;

        mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"token\":\"mocked-jwt-token\"}"));
    }
}
