package ru.homecrew.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;
import ru.homecrew.repository.AppUserRepository;

@DataJpaTest
@Import(AppUserDetailsService.class)
@DisplayName("AppUserDetailsService — проверка загрузки пользователя из базы")
class AppUserDetailsServiceTest {

    @Autowired
    private AppUserDetailsService service;

    @Autowired
    private AppUserRepository repo;

    @Test
    @DisplayName("Загружает пользователя и корректно проставляет роль ROLE_OWNER")
    void loadsUserCorrectly() {
        var user = new AppUser();
        user.setUsername("test");
        user.setPasswordHash("123");
        user.setRole(Role.OWNER);
        repo.save(user);

        UserDetails details = service.loadUserByUsername("test");

        assertThat(details.getUsername())
                .as("Имя пользователя должно совпадать")
                .isEqualTo("test");

        assertThat(details.getAuthorities())
                .as("Пользователь должен иметь только роль ROLE_OWNER")
                .extracting("authority")
                .containsExactly("ROLE_OWNER");
    }
}
