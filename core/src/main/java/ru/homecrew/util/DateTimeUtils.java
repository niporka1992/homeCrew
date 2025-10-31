package ru.homecrew.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.experimental.UtilityClass;

/**
 * Утильный класс для конвертации между {@link java.time} и {@link java.util.Date}.
 * Все методы null-safe и возвращают корректное значение даже при null-входах.
 */
@UtilityClass
public class DateTimeUtils {

    private static final Locale RU = Locale.of("ru", "RU");

    /**
     * Преобразует {@link Date} в {@link LocalDateTime} в системной таймзоне.
     */
    public static LocalDateTime toLocal(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Преобразует {@link LocalDate}/{@link LocalTime} в {@link Date} в системной таймзоне.
     * Если дата или время отсутствуют — подставляет текущее.
     */
    public static Date toDate(LocalDate date, LocalTime time) {
        LocalDate safeDate = Optional.ofNullable(date).orElse(LocalDate.now());
        LocalTime safeTime = Optional.ofNullable(time).orElse(LocalTime.now());
        return Date.from(LocalDateTime.of(safeDate, safeTime)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * Преобразует {@link Instant} в {@link LocalDateTime} в системной таймзоне.
     */
    public static LocalDateTime fromInstant(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Возвращает минимальное (безопасное) значение времени для сортировок.
     */
    public static LocalDateTime minSafe(LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime).orElse(LocalDateTime.MIN);
    }

    private static final DateTimeFormatter RUSSIAN_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'года,' HH:mm").withLocale(RU);

    /**
     * Форматирует LocalDateTime в человеко-читабельную строку:
     * 24 января 2025 года, 19:00
     */
    public static String formatRu(LocalDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(RUSSIAN_FORMATTER);
    }
}
