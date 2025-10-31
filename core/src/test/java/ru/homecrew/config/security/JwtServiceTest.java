package ru.homecrew.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;

@DisplayName("JwtService — генерация и валидация JWT токенов")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secret =
                java.util.Base64.getEncoder().encodeToString("test-secret-key-should-be-very-long-for-hmac".getBytes());
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    @DisplayName("Генерирует токен с корректным именем пользователя и ролью")
    void generatesTokenWithCorrectClaims() {
        AppUser user = AppUser.builder().username("admin").role(Role.OWNER).build();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull();
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("admin");

        Claims claims = jwtService.extractClaim(token, c -> c);
        assertThat(claims.get("role")).isEqualTo("OWNER");
    }

    @Test
    @DisplayName("Проверяет токен как валидный для совпадающего пользователя")
    void validatesTokenForCorrectUser() {
        AppUser user = AppUser.builder().username("admin").role(Role.OWNER).build();

        String token = jwtService.generateToken(user);

        var userDetails = User.withUsername("admin")
                .password("123")
                .authorities("ROLE_OWNER")
                .build();

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid)
                .as("Токен должен быть валидным для того же пользователя")
                .isTrue();
    }

    @Test
    @DisplayName("Распознаёт токен как невалидный, если имя пользователя не совпадает")
    void invalidForDifferentUser() {
        AppUser user = AppUser.builder().username("admin").role(Role.OWNER).build();

        String token = jwtService.generateToken(user);

        var otherUser = User.withUsername("hacker")
                .password("321")
                .authorities("ROLE_OWNER")
                .build();

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertThat(valid)
                .as("Токен не должен быть валидным для другого пользователя")
                .isFalse();
    }
}
