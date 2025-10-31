package ru.homecrew.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import ru.homecrew.config.security.JwtService;
import ru.homecrew.dto.auth.AuthRequest;
import ru.homecrew.dto.auth.AuthResponse;
import ru.homecrew.entity.AppUser;
import ru.homecrew.repository.AppUserRepository;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new AppUser();
        testUser.setUsername("ivan");
        testUser.setPasswordHash("secret");
    }

    // =================== authenticate() ===================

    @Test
    @DisplayName("authenticate(): успешная авторизация возвращает токен")
    void authenticate_success() {
        AuthRequest req = new AuthRequest("ivan", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        AuthResponse resp = authService.authenticate(req);

        assertNotNull(resp);
        assertEquals("jwt-token", resp.token());
        verify(jwtService).generateToken(testUser);
        verify(userRepository).findByUsername("ivan");
    }

    @Test
    @DisplayName("authenticate(): выбрасывает SecurityException, если не аутентифицирован")
    void authenticate_notAuthenticated() {
        AuthRequest req = new AuthRequest("ivan", "badpass");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class, () -> authService.authenticate(req));

        assertTrue(ex.getMessage().contains("Ошибка авторизации"));
    }

    @Test
    @DisplayName("authenticate(): выбрасывает EntityNotFoundException, если пользователь не найден")
    void authenticate_userNotFound() {
        AuthRequest req = new AuthRequest("nonexistent", "123");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.authenticate(req));
    }

    // =================== getUserFromToken() ===================

    @Test
    @DisplayName("getUserFromToken(): успешное извлечение пользователя из токена")
    void getUserFromToken_success() {
        String header = "Bearer goodtoken";

        when(jwtService.extractUsername("goodtoken")).thenReturn("ivan");
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(testUser));

        AppUser user = authService.getUserFromToken(header);

        assertEquals("ivan", user.getUsername());
        verify(jwtService).extractUsername("goodtoken");
    }

    @Test
    @DisplayName("getUserFromToken(): выбрасывает IllegalArgumentException, если токен некорректный")
    void getUserFromToken_invalidHeader() {
        assertThrows(IllegalArgumentException.class, () -> authService.getUserFromToken(null));

        assertThrows(IllegalArgumentException.class, () -> authService.getUserFromToken("InvalidFormat"));
    }

    @Test
    @DisplayName("getUserFromToken(): выбрасывает EntityNotFoundException, если пользователь не найден")
    void getUserFromToken_userNotFound() {
        String header = "Bearer goodtoken";
        when(jwtService.extractUsername("goodtoken")).thenReturn("ivan");
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.getUserFromToken(header));
    }
}
