package ru.homecrew.service.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.homecrew.dto.scheduler.type.DailyScheduleDto;
import ru.homecrew.service.scheduler.strategy.DailyCronStrategy;

@DisplayName("DailyCronStrategy — генерация CRON для ежедневного расписания")
class DailyCronStrategyTest {

    private final DailyCronStrategy strategy = new DailyCronStrategy();

    @Test
    @DisplayName("generate(): формирует корректный CRON при указанном времени")
    void generate_withExplicitTime() {
        DailyScheduleDto dto = new DailyScheduleDto("job", LocalDate.now(), LocalTime.of(8, 30), null, null);
        String result = strategy.generate(dto);
        assertEquals("0 30 8 * * ?", result);
    }

    @Test
    @DisplayName("generate(): если время не указано — используется полночь")
    void generate_withNullTime_defaultsToMidnight() {
        DailyScheduleDto dto = new DailyScheduleDto("job", LocalDate.now(), null, null, null);
        String result = strategy.generate(dto);
        assertEquals("0 0 0 * * ?", result);
    }
}
