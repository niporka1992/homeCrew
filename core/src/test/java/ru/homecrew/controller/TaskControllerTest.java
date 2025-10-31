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
import ru.homecrew.service.task.TaskService;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "ru\\.homecrew\\.config\\.security\\..*")
        })
@Import(TaskControllerTest.TestSecurityConfig.class)
class TaskControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    MockMvc mvc;

    @MockBean
    TaskService taskService;

    /**
     * Все эндпоинты TaskController.
     */
    static Stream<MockHttpServletRequestBuilder> allEndpoints() {
        String taskJson = """
        {"title":"Test Task","description":"Do something","status":"NEW"}
        """;

        return Stream.of(
                get("/api/tasks"), // список задач
                get("/api/tasks/1"), // по id
                get("/api/tasks/1/details"), // детали
                put("/api/tasks/1")
                        .with(csrf()) // обновление
                        .contentType("application/json")
                        .content(taskJson),
                delete("/api/tasks/1").with(csrf()) // удаление
                );
    }

    // 401 — без авторизации
    @ParameterizedTest(name = "⛔ Без авторизации: {0}")
    @MethodSource("allEndpoints")
    void allEndpoints_shouldReturn401_whenNoAuth(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isUnauthorized());
    }

    // 403 — для WORKER
    @ParameterizedTest(name = "🚫 WORKER не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "WORKER")
    void allEndpoints_shouldReturn403_forWorker(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    // 403 — для GUEST
    @ParameterizedTest(name = "🚫 GUEST не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "GUEST")
    void allEndpoints_shouldReturn403_forGuest(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    // 200 — OWNER может получить список
    @Test
    @DisplayName("✅ OWNER может получить список задач")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_getTasks() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isOk());
    }

    // 200 — OWNER может получить задачу
    @Test
    @DisplayName("✅ OWNER может получить задачу по id")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_getById() throws Exception {
        mvc.perform(get("/api/tasks/1")).andExpect(status().isOk());
    }

    // 200 — OWNER может получить детали задачи
    @Test
    @DisplayName("✅ OWNER может получить детали задачи")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_getDetails() throws Exception {
        mvc.perform(get("/api/tasks/1/details")).andExpect(status().isOk());
    }

    // 200 — OWNER может обновить задачу
    @Test
    @DisplayName("✅ OWNER может обновить задачу")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_updateTask() throws Exception {
        String json =
                """
                {"title":"Updated Task","description":"Fix this","status":"IN_PROGRESS"}
                """;

        mvc.perform(put("/api/tasks/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());
    }

    // 204 — OWNER может удалить задачу
    @Test
    @DisplayName("✅ OWNER может удалить задачу")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_deleteTask() throws Exception {
        mvc.perform(delete("/api/tasks/1").with(csrf())).andExpect(status().isNoContent());
    }
}
