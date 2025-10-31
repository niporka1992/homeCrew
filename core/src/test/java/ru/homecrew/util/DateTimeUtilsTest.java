package ru.homecrew.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DateTimeUtils — утиль для конвертации времени и дат")
class DateTimeUtilsTest {

    @Test
    @DisplayName("toLocal(): корректно конвертирует Date в LocalDateTime")
    void toLocal_success() {
        Instant instant = Instant.parse("2025-01-24T12:30:00Z");
        Date date = Date.from(instant);
        LocalDateTime local = DateTimeUtils.toLocal(date);

        assertNotNull(local);
        assertEquals(2025, local.getYear());
        assertEquals(24, local.getDayOfMonth());
    }

    @Test
    @DisplayName("toLocal(): возвращает null при null-входе")
    void toLocal_nullSafe() {
        assertNull(DateTimeUtils.toLocal(null));
    }

    @Test
    @DisplayName("toDate(): конвертирует LocalDate и LocalTime в Date")
    void toDate_success() {
        LocalDate date = LocalDate.of(2024, 10, 30);
        LocalTime time = LocalTime.of(15, 45);

        Date result = DateTimeUtils.toDate(date, time);
        assertNotNull(result);

        LocalDateTime reconverted = DateTimeUtils.toLocal(result);
        assertEquals(date.getYear(), reconverted.getYear());
        assertEquals(date.getDayOfMonth(), reconverted.getDayOfMonth());
        assertEquals(time.getHour(), reconverted.getHour());
        assertEquals(time.getMinute(), reconverted.getMinute());
    }

    @Test
    @DisplayName("toDate(): подставляет текущие дату и время при null-входе")
    void toDate_withNulls() {
        Date result = DateTimeUtils.toDate(null, null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("fromInstant(): корректно конвертирует Instant")
    void fromInstant_success() {
        Instant instant = Instant.parse("2025-05-01T00:00:00Z");
        LocalDateTime local = DateTimeUtils.fromInstant(instant);

        assertEquals(2025, local.getYear());
        assertEquals(5, local.getMonthValue());
    }

    @Test
    @DisplayName("fromInstant(): возвращает null при null-входе")
    void fromInstant_nullSafe() {
        assertNull(DateTimeUtils.fromInstant(null));
    }

    @Test
    @DisplayName("minSafe(): возвращает MIN, если вход null")
    void minSafe_null() {
        LocalDateTime result = DateTimeUtils.minSafe(null);
        assertEquals(LocalDateTime.MIN, result);
    }

    @Test
    @DisplayName("minSafe(): возвращает исходное значение, если не null")
    void minSafe_normal() {
        LocalDateTime input = LocalDateTime.of(2023, 12, 31, 23, 59);
        assertSame(input, DateTimeUtils.minSafe(input));
    }

    @Test
    @DisplayName("formatRu(): форматирует дату в русском стиле")
    void formatRu_success() {
        LocalDateTime dt = LocalDateTime.of(2025, 1, 24, 19, 0);
        String formatted = DateTimeUtils.formatRu(dt);

        assertTrue(formatted.contains("2025"));
        assertTrue(formatted.contains("года"));
        assertTrue(formatted.matches(".*\\d{2}:\\d{2}$"));
    }

    @Test
    @DisplayName("formatRu(): возвращает '—' при null")
    void formatRu_null() {
        assertEquals("—", DateTimeUtils.formatRu(null));
    }
}
