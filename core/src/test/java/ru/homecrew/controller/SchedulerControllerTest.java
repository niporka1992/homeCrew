package ru.homecrew.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
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
import ru.homecrew.dto.scheduler.jobs.JobInfoDto;
import ru.homecrew.service.scheduler.SchedulerService;

@WebMvcTest(
        controllers = SchedulerController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "ru\\.homecrew\\.config\\.security\\..*")
        })
@Import(SchedulerControllerTest.TestSecurityConfig.class)
class SchedulerControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    MockMvc mvc;

    @MockBean
    SchedulerService schedulerService;

    static final String SIMPLE_JSON =
            """
        {
          "type": "SIMPLE",
          "jobName": "testJob",
          "startDate": "2025-10-30",
          "time": "10:30",
          "repeatCount": 2,
          "repeatIntervalMs": 60000
        }
        """;

    static final String CRON_JSON =
            """
    {
      "type": "CUSTOM",
      "jobName": "1212",
      "endDate": null,
      "endTime": null,
      "customCron": "0 30 8 ? * MON,WED,FRI"
    }
    """;
    ;

    static Stream<MockHttpServletRequestBuilder> allEndpoints() {
        return Stream.of(
                post("/api/scheduler/job/simple")
                        .with(csrf())
                        .contentType("application/json")
                        .content(SIMPLE_JSON),
                post("/api/scheduler/job/cron")
                        .with(csrf())
                        .contentType("application/json")
                        .content(CRON_JSON),
                patch("/api/scheduler/job/demo/status").param("active", "true").with(csrf()),
                get("/api/scheduler/jobs").param("status", "ALL"));
    }

    @ParameterizedTest(name = "Без авторизации: {0}")
    @MethodSource("allEndpoints")
    void shouldReturn401_whenNoAuth(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "WORKER не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "WORKER")
    void shouldReturn403_forWorker(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "GUEST не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "GUEST")
    void shouldReturn403_forGuest(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWNER может создать SIMPLE-задачу")
    @WithMockUser(roles = "OWNER")
    void ownerCanCreateSimpleJob() throws Exception {
        mvc.perform(post("/api/scheduler/job/simple")
                        .with(csrf())
                        .contentType("application/json")
                        .content(SIMPLE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OWNER может создать CRON-задачу")
    @WithMockUser(roles = "OWNER")
    void ownerCanCreateCronJob() throws Exception {
        mvc.perform(post("/api/scheduler/job/cron")
                        .with(csrf())
                        .contentType("application/json")
                        .content(CRON_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OWNER может менять статус задачи")
    @WithMockUser(roles = "OWNER")
    void ownerCanChangeJobStatus() throws Exception {
        mvc.perform(patch("/api/scheduler/job/demo/status")
                        .param("active", "true")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OWNER может получить список задач")
    @WithMockUser(roles = "OWNER")
    void ownerCanGetJobs() throws Exception {
        when(schedulerService.getAllJobsByStatus("ALL"))
                .thenReturn(List.of(new JobInfoDto(
                        "jobA",
                        "test job",
                        "RUNNING",
                        LocalDateTime.now().minusMinutes(5),
                        LocalDateTime.now().plusMinutes(5))));

        mvc.perform(get("/api/scheduler/jobs").param("status", "ALL")).andExpect(status().isOk());
    }
}
