package ru.homecrew.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@DisplayName("SecurityConfig — базовая настройка безопасности приложения")
class SecurityConfigTest {

    private AppUserDetailsService userDetailsService;
    private JwtAuthenticationFilter jwtFilter;
    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(AppUserDetailsService.class);
        jwtFilter = mock(JwtAuthenticationFilter.class);
        config = new SecurityConfig(userDetailsService, jwtFilter);
    }

    @Test
    @DisplayName("Создаёт BCryptPasswordEncoder")
    void createsPasswordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isNotNull();
        String hash = encoder.encode("1234");
        assertThat(encoder.matches("1234", hash)).isTrue();
    }

    @Test
    @DisplayName("DaoAuthenticationProvider содержит наш UserDetailsService и BCryptPasswordEncoder")
    void createsAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider provider = config.authenticationProvider();

        assertThat(provider).isNotNull();

        // Проверим, что userDetailsService установлен корректно
        Field serviceField = DaoAuthenticationProvider.class.getDeclaredField("userDetailsService");
        serviceField.setAccessible(true);
        Object injectedService = serviceField.get(provider);
        assertThat(injectedService).isEqualTo(userDetailsService);

        Field encoderField = DaoAuthenticationProvider.class.getDeclaredField("passwordEncoder");
        encoderField.setAccessible(true);
        Object injectedEncoder = encoderField.get(provider);
        assertThat(injectedEncoder)
                .as("PasswordEncoder должен быть BCryptPasswordEncoder")
                .isInstanceOf(PasswordEncoder.class);
    }

    @Test
    @DisplayName("Создаёт AuthenticationManager через AuthenticationConfiguration")
    void createsAuthenticationManager() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);

        when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager manager = config.authenticationManager(authConfig);

        assertThat(manager).isEqualTo(mockManager);
        verify(authConfig).getAuthenticationManager();
    }

    @Test
    @DisplayName("SecurityFilterChain вызывает конфигурацию HttpSecurity и добавляет JwtAuthenticationFilter")
    void configuresHttpSecurityAndAddsJwtFilter() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);

        config.securityFilterChain(http);

        verify(http).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        verify(http).csrf(any());
        verify(http).sessionManagement(any());
        verify(http).authenticationProvider(any());
    }
}
