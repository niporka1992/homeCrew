package ru.homecrew.service.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.homecrew.dto.scheduler.type.CustomScheduleDto;
import ru.homecrew.service.scheduler.strategy.CustomCronStrategy;

@DisplayName("CustomCronStrategy — генерация пользовательского CRON")
class CustomCronStrategyTest {

    private final CustomCronStrategy strategy = new CustomCronStrategy();

    @Test
    @DisplayName("generate(): возвращает customCron, если он задан")
    void generate_validCron() {
        CustomScheduleDto dto =
                new CustomScheduleDto("job1", LocalDate.now(), LocalTime.NOON, null, null, "0 0 12 * * ?");

        String result = strategy.generate(dto);
        assertEquals("0 0 12 * * ?", result);
    }

    @Test
    @DisplayName("generate(): выбрасывает исключение при пустом customCron")
    void generate_emptyOrNullCron_throws() {
        CustomScheduleDto empty = new CustomScheduleDto("job", null, null, null, null, "");
        CustomScheduleDto nullCron = new CustomScheduleDto("job", null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> strategy.generate(empty));
        assertThrows(IllegalArgumentException.class, () -> strategy.generate(nullCron));
    }
}
