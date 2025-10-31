package ru.homecrew.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.homecrew.service.file.FileService;

@WebMvcTest(
        controllers = FileController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = "ru\\.homecrew\\.config\\.security\\..*")
        })
@Import(FileControllerTest.TestSecurityConfig.class)
@DisplayName(" Тесты безопасности FileController — доступ только для ROLE_OWNER")
class FileControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    MockMvc mvc;

    @MockBean
    FileService fileService;

    static Stream<MockHttpServletRequestBuilder> allEndpoints() {
        return Stream.of(get("/api/files/testFile123"));
    }

    @ParameterizedTest(name = " Без авторизации: {0}")
    @MethodSource("allEndpoints")
    void allEndpoints_shouldReturn401_whenNoAuth(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = " WORKER не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "WORKER")
    void allEndpoints_shouldReturn403_forWorker(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = " GUEST не имеет доступа: {0}")
    @MethodSource("allEndpoints")
    @WithMockUser(roles = "GUEST")
    void allEndpoints_shouldReturn403_forGuest(MockHttpServletRequestBuilder req) throws Exception {
        mvc.perform(req).andExpect(status().isForbidden());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName(" OWNER имеет доступ к файлам — 200 OK")
    @WithMockUser(roles = "OWNER")
    void ownerCanAccess_getFile() throws Exception {
        when(fileService.getFileById(anyString())).thenReturn((ResponseEntity) ResponseEntity.ok("mocked-file-data"));

        mvc.perform(get("/api/files/testFile123")).andExpect(status().isOk());
    }
}
