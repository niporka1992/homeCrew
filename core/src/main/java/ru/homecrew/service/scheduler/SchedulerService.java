package ru.homecrew.service.scheduler;

import java.util.List;
import ru.homecrew.dto.scheduler.jobs.JobInfoDto;
import ru.homecrew.dto.scheduler.type.*;

/**
 * {@code SchedulerService} — интерфейс управления заданиями Quartz-планировщика HomeCrew.
 *
 * <p>Поддерживает добавление задач двух типов:
 * <ul>
 *   <li>{@link ScheduleDto} — для расписаний по CRON (DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM);</li>
 *   <li>{@link SimpleScheduleDto} — для одноразовых или повторяющихся задач.</li>
 * </ul>
 * </p>
 */
public interface SchedulerService {

    /**
     * Добавляет новую CRON-задачу в планировщик.
     *
     * <p>Тип передаваемого DTO зависит от режима расписания:
     * <ul>
     *   <li>{@link DailyScheduleDto} — ежедневная;</li>
     *   <li>{@link WeeklyScheduleDto} — еженедельная;</li>
     *   <li>{@link MonthlyScheduleDto} — ежемесячная;</li>
     *   <li>{@link YearlyScheduleDto} — ежегодная;</li>
     *   <li>{@link CustomScheduleDto} — произвольное CRON-выражение.</li>
     * </ul>
     * </p>
     *
     * @param dto параметры расписания
     * @throws RuntimeException если задача не может быть добавлена
     */
    void scheduleCronJob(ScheduleDto dto);

    /**
     * Добавляет новую SIMPLE-задачу в планировщик.
     *
     * @param dto параметры для одноразового или повторяющегося запуска
     * @throws RuntimeException если задача не может быть добавлена
     */
    void scheduleSimpleJob(SimpleScheduleDto dto);

    /**
     * Включает или приостанавливает задачу по её имени.
     *
     * @param jobName имя задачи
     * @param active  {@code true} — возобновить, {@code false} — приостановить
     */
    void setJobActive(String jobName, boolean active);

    /**
     * Проверяет, зарегистрирована ли задача в текущем планировщике.
     *
     * @param jobName имя задачи
     * @return {@code true}, если задача активна, иначе {@code false}
     */
    boolean isJobScheduled(String jobName);

    /**
     * Возвращает список задач по фильтру состояния.
     *
     * @param status "active" — активные, "paused" — приостановленные, "other" — прочие
     * @return список задач с информацией о состоянии
     */
    List<JobInfoDto> getAllJobsByStatus(String status);
}
