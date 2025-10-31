package ru.homecrew.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@DisplayName("CorsConfig — настройка CORS для dev и prod профилей")
class CorsConfigTest {

    @Nested
    @DisplayName("dev-профиль")
    class DevProfile {

        @Test
        @DisplayName("Разрешает все источники и методы в режиме разработки")
        void allowsAllOriginsInDev() {
            CorsConfig config = new CorsConfig();
            WebMvcConfigurer devConfig = config.devCorsConfigurer();

            MockCorsRegistry registry = new MockCorsRegistry();
            devConfig.addCorsMappings(registry);

            assertThat(registry.allowedOriginPatterns).containsExactly("*");
            assertThat(registry.allowedMethods).containsExactly("*");
            assertThat(registry.allowCredentials).isTrue();
        }
    }

    @Nested
    @DisplayName("prod-профиль")
    class ProdProfile {

        @Test
        @DisplayName("Разрешает только домены из конфигурации и безопасные HTTP-методы")
        void allowsConfiguredOriginsInProd() throws Exception {
            CorsConfig config = new CorsConfig();

            // Подставляем тестовые домены через reflection
            Field field = CorsConfig.class.getDeclaredField("allowedOrigins");
            field.setAccessible(true);
            field.set(config, new String[] {"https://homecrew.ru", "https://www.homecrew.ru"});

            WebMvcConfigurer prodConfig = config.prodCorsConfigurer();
            MockCorsRegistry registry = new MockCorsRegistry();
            prodConfig.addCorsMappings(registry);

            assertThat(registry.allowedOriginPatterns)
                    .containsExactly("https://homecrew.ru", "https://www.homecrew.ru");
            assertThat(registry.allowedMethods).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
            assertThat(registry.allowCredentials).isTrue();
        }
    }

    // ===== Мини-мок =====
    static class MockCorsRegistry extends CorsRegistry {
        String[] allowedOriginPatterns;
        String[] allowedMethods;
        boolean allowCredentials;

        @Override
        public @NonNull org.springframework.web.servlet.config.annotation.CorsRegistration addMapping(
                @NonNull String pathPattern) {
            return new org.springframework.web.servlet.config.annotation.CorsRegistration(pathPattern) {
                @Override
                public @NonNull org.springframework.web.servlet.config.annotation.CorsRegistration
                        allowedOriginPatterns(@NonNull String... origins) {
                    allowedOriginPatterns = origins;
                    return this;
                }

                @Override
                public @NonNull org.springframework.web.servlet.config.annotation.CorsRegistration allowedMethods(
                        @NonNull String... methods) {
                    allowedMethods = methods;
                    return this;
                }

                @Override
                public @NonNull org.springframework.web.servlet.config.annotation.CorsRegistration allowCredentials(
                        boolean allow) {
                    allowCredentials = allow;
                    return this;
                }
            };
        }
    }
}
