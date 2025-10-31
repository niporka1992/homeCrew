package ru.homecrew.service.auth;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.homecrew.config.security.JwtService;
import ru.homecrew.dto.auth.AuthRequest;
import ru.homecrew.dto.auth.AuthResponse;
import ru.homecrew.entity.AppUser;
import ru.homecrew.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Аутентификация пользователя: {}", request.username());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        if (!auth.isAuthenticated()) {
            throw new SecurityException("Ошибка авторизации: неверные учетные данные");
        }

        AppUser user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + request.username()));

        String token = jwtService.generateToken(user);

        log.info("Пользователь {} успешно авторизован", user.getUsername());
        return new AuthResponse(token);
    }

    public AppUser getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Некорректный токен");
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }
}
