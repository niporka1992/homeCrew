package ru.homecrew.bot.config;

import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.homecrew.bot.annotation.RoleMapping;
import ru.homecrew.bot.strategy.BotUserStrategy;
import ru.homecrew.enums.Role;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoleStrategyAutoRegistrar {

    private final ApplicationContext context;

    @Bean
    public Map<Role, BotUserStrategy> roleStrategies() {
        Map<String, BotUserStrategy> beans = context.getBeansOfType(BotUserStrategy.class);

        Map<Role, BotUserStrategy> strategies = new EnumMap<>(Role.class);

        beans.values().forEach(strategy -> {
            RoleMapping mapping = strategy.getClass().getAnnotation(RoleMapping.class);
            if (mapping != null) {
                strategies.put(mapping.value(), strategy);
                log.info(
                        " Зарегистрирована стратегия для роли {} → {}",
                        mapping.value(),
                        strategy.getClass().getSimpleName());
            }
        });

        if (!strategies.containsKey(Role.WORKER)) {
            log.warn(" Не найдена стратегия для роли WORKER — бот будет использовать null по умолчанию!");
        }

        return strategies;
    }
}
