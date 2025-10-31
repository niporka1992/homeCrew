package ru.homecrew.config.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.homecrew.config.security.JwtService;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;
import ru.homecrew.repository.AppUserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.admin.default-password:admin}")
    private String defaultAdminPassword;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @PostConstruct
    public void initAdmin() {
        AppUser admin = repository.findByUsername("admin").orElseGet(() -> {
            AppUser newAdmin = AppUser.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode(defaultAdminPassword))
                    .fullName("Главный администратор")
                    .role(Role.OWNER)
                    .phone("")
                    .email("")
                    .isBlocked(false)
                    .build();

            repository.save(newAdmin);
            log.info("✅ Администратор создан: username='{}'", newAdmin.getUsername());
            return newAdmin;
        });

        if ("dev".equalsIgnoreCase(activeProfile)) {
            String token = jwtService.generateToken(admin);
            log.info("🔑 [DEV] JWT токен администратора: Bearer {}", token);
        }
    }
}
