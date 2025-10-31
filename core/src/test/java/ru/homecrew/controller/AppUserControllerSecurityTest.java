package ru.homecrew.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.homecrew.mapper.AppUserMapper;
import ru.homecrew.service.userappservice.UserAppService;

@WebMvcTest(
        controllers = AppUserController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "ru\\.homecrew\\.config\\.security\\..*")
        })
@Import(AppUserControllerSecurityTest.TestSecurityConfig.class)
class AppUserControllerSecurityTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    MockMvc mvc;

    @MockBean
    UserAppService userService;

    @MockBean
    AppUserMapper mapper;

    static Stream<MockHttpServletRequestBuilder> allEndpoints() {
        String dummyJson =
                """
        {"username":"dummy","fullName":"test","role":"WORKER","phone":"+70000000000","email":"x@y.com","password":"Valid123!"}
        """;

        return Stream.of(
                get("/api/users"),
                get("/api/users/test"),
                post("/api/users").with(csrf()).contentType("application/json").content(dummyJson),
                put("/api/users/test")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"fullName\":\"Test\"}"),
                delete("/api/users/test").with(csrf()),
                patch("/api/users/test/status").param("isBlocked", "true").with(csrf()),
                put("/api/users/password")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"username\":\"dummy\",\"newPassword\":\"NewPass123!\"}"));
    }

    @ParameterizedTest(name = "⛔ Без авторизации: {0}")
    @MethodSource("allEndpoints")
    void allEndpoints_shouldReturn401_whenNoAuth(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "🚫 WORKER не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "WORKER")
    void allEndpoints_shouldReturn403_forWorker(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "🚫 GUEST не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "GUEST")
    void allEndpoints_shouldReturn403_forGuest(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("✅ OWNER может получить список пользователей")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_getAll() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ OWNER может удалить пользователя")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_delete() throws Exception {
        mvc.perform(delete("/api/users/test").with(csrf())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ OWNER может менять пароль")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_passwordChange() throws Exception {
        String json =
                """
            {
              "username": "dummy",
              "newPassword": "NewPass123!"
            }
            """;

        mvc.perform(put("/api/users/password")
                        .with(csrf())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ OWNER может менять статус пользователя (isBlocked)")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_updateStatus() throws Exception {
        mvc.perform(patch("/api/users/test/status").param("isBlocked", "true").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ OWNER может создавать пользователя")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_createUser() throws Exception {
        String json =
                """
                {
                  "username": "newUser",
                  "fullName": "Test User",
                  "role": "WORKER",
                  "phone": "+70000000000",
                  "email": "test@example.com",
                  "password": "Pass123!"
                }
                """;

        mvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🚫 Новый пароль слишком простой — ошибка 400")
    @WithMockUser(roles = "OWNER")
    void changePassword_shouldFail_onWeakPassword() throws Exception {
        String weakJson =
                """
                {
                  "newPassword": "qF34#y"
                }
                """;

        mvc.perform(put("/api/users/password")
                        .with(csrf())
                        .contentType("application/json")
                        .content(weakJson))
                .andExpect(status().isBadRequest());
    }
}
