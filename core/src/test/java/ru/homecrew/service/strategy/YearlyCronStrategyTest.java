package ru.homecrew.service.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.homecrew.dto.scheduler.type.YearlyScheduleDto;
import ru.homecrew.service.scheduler.strategy.YearlyCronStrategy;

@DisplayName("YearlyCronStrategy — генерация CRON для ежегодного расписания")
class YearlyCronStrategyTest {

    private final YearlyCronStrategy strategy = new YearlyCronStrategy();

    @Test
    @DisplayName("generate(): формирует корректный CRON при заданных дате и времени")
    void generate_withDateAndTime() {
        YearlyScheduleDto dto =
                new YearlyScheduleDto("job", LocalDate.of(2025, 12, 31), LocalTime.of(22, 10), null, null);
        String result = strategy.generate(dto);
        assertEquals("0 10 22 31 12 ?", result);
    }

    @Test
    @DisplayName("generate(): если дата не указана — подставляет 1 января текущего года")
    void generate_nullDate_defaultsToJanFirst() {
        YearlyScheduleDto dto = new YearlyScheduleDto("job", null, LocalTime.of(9, 0), null, null);
        String result = strategy.generate(dto);
        assertEquals("0 0 9 1 1 ?", result);
    }

    @Test
    @DisplayName("generate(): если время не указано — берёт полночь")
    void generate_nullTime_defaultsToMidnight() {
        YearlyScheduleDto dto = new YearlyScheduleDto("job", LocalDate.of(2025, 6, 10), null, null, null);
        String result = strategy.generate(dto);
        assertEquals("0 0 0 10 6 ?", result);
    }

    @Test
    @DisplayName("generate(): если дата и время не указаны — 1 января 00:00")
    void generate_bothNull_defaultsToJanFirstMidnight() {
        YearlyScheduleDto dto = new YearlyScheduleDto("job", null, null, null, null);
        String result = strategy.generate(dto);
        assertEquals("0 0 0 1 1 ?", result);
    }
}
