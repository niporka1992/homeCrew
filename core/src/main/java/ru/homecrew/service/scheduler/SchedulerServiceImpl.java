package ru.homecrew.service.scheduler;

import static ru.homecrew.util.DateTimeUtils.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.dto.scheduler.jobs.JobInfoDto;
import ru.homecrew.dto.scheduler.type.ScheduleDto;
import ru.homecrew.dto.scheduler.type.SimpleScheduleDto;
import ru.homecrew.enums.TaskTypeTrigger;
import ru.homecrew.exception.SchedulerServiceException;
import ru.homecrew.service.scheduler.jobs.TaskCreationJob;
import ru.homecrew.util.DateTimeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private static final String DEFAULT_DESCRIPTION = "Без описания";
    private static final String JOB_PREFIX = "job_";
    private static final String TRIGGER_SUFFIX = "_trigger";
    private static final String DATA_KEY_LAST_EXECUTED = "lastExecutedAt";

    private static final String FILTER_ACTIVE = "active";
    private static final String FILTER_PAUSED = "paused";
    private static final String FILTER_OTHER = "other";

    private static final String STATUS_COMPLETE = "COMPLETE";

    private static final int PRIORITY_NORMAL = 0;
    private static final int PRIORITY_PAUSED = 1;
    private static final int PRIORITY_OTHER = 2;

    private final Scheduler quartzScheduler;
    private final ScheduleExpressionService expressionService;

    @Transactional
    @Override
    public void scheduleCronJob(ScheduleDto dto) {
        try {
            String jobName = generateJobName(TaskTypeTrigger.CRON.toString());
            JobDetail jobDetail = buildJob(jobName, dto.jobName());

            String cronExpression = expressionService.toCron(dto);
            CronTrigger trigger = buildCronTrigger(jobName, cronExpression);

            quartzScheduler.scheduleJob(jobDetail, trigger);
            log.info("Добавлена CRON-задача [{}]: {}", jobName, cronExpression);

        } catch (SchedulerException e) {
            log.error("Ошибка при создании CRON-задачи", e);
            throw new SchedulerServiceException("Ошибка при создании CRON-задачи", e);
        }
    }

    @Transactional
    @Override
    public void scheduleSimpleJob(SimpleScheduleDto dto) {
        try {
            String jobName = generateJobName(TaskTypeTrigger.SIMPLE.toString());
            JobDetail jobDetail = buildJob(jobName, dto.jobName());
            quartzScheduler.addJob(jobDetail, true);

            Trigger trigger = buildSimpleTrigger(jobName, dto);
            quartzScheduler.scheduleJob(trigger);

            log.info(
                    "SIMPLE-задача [{}]: старт={}, повторов={}, интервал={}мс",
                    jobName,
                    dto.startDate(),
                    dto.repeatCount(),
                    dto.repeatIntervalMs());

        } catch (SchedulerException e) {
            log.error("Ошибка при создании SIMPLE-задачи", e);
            throw new SchedulerServiceException("Ошибка при создании SIMPLE-задачи", e);
        }
    }

    @Override
    public void setJobActive(String jobName, boolean active) {
        try {
            JobKey key = JobKey.jobKey(jobName);
            if (!quartzScheduler.checkExists(key)) {
                throw new SchedulerServiceException(
                        "Задача не найдена: " + jobName, new NoSuchElementException("Job not found"));
            }

            if (active) {
                quartzScheduler.resumeJob(key);
            } else {
                quartzScheduler.pauseJob(key);
            }

            log.info("{} задача [{}]", active ? "Возобновлена" : "⏸ Приостановлена", jobName);

        } catch (SchedulerException e) {
            throw new SchedulerServiceException("Ошибка при изменении статуса задачи: " + jobName, e);
        }
    }

    @Override
    public boolean isJobScheduled(String jobName) {
        try {
            return quartzScheduler.checkExists(JobKey.jobKey(jobName));
        } catch (SchedulerException e) {
            throw new SchedulerServiceException("Ошибка при проверке существования задачи: " + jobName, e);
        }
    }

    @Override
    public List<JobInfoDto> getAllJobsByStatus(String statusFilter) {
        try {
            return quartzScheduler.getJobKeys(GroupMatcher.anyGroup()).stream()
                    .map(jobKey -> mapJobToInfo(jobKey, statusFilter))
                    .filter(Objects::nonNull)
                    .sorted(this::compareJobs)
                    .toList();
        } catch (SchedulerException e) {
            throw new SchedulerServiceException("Не удалось получить список задач", e);
        }
    }

    private JobDetail buildJob(String jobName, String description) {
        return JobBuilder.newJob(TaskCreationJob.class)
                .storeDurably(true)
                .withIdentity(jobName)
                .withDescription(description)
                .build();
    }

    private CronTrigger buildCronTrigger(String jobName, String cronExpression) {
        CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule(cronExpression);
        return TriggerBuilder.newTrigger()
                .withIdentity(jobName + TRIGGER_SUFFIX)
                .withSchedule(schedule)
                .startNow()
                .build();
    }

    private Trigger buildSimpleTrigger(String jobName, SimpleScheduleDto dto) {
        SimpleScheduleBuilder schedule = SimpleScheduleBuilder.simpleSchedule()
                .withRepeatCount(Optional.ofNullable(dto.repeatCount()).orElse(0))
                .withIntervalInMilliseconds(
                        Optional.ofNullable(dto.repeatIntervalMs()).orElse(0L))
                .withMisfireHandlingInstructionNowWithRemainingCount();

        return TriggerBuilder.newTrigger()
                .forJob(jobName)
                .withIdentity(jobName + TRIGGER_SUFFIX)
                .startAt(toDate(dto.startDate(), dto.startTime()))
                .withSchedule(schedule)
                .build();
    }

    private JobInfoDto mapJobToInfo(JobKey jobKey, String statusFilter) {
        try {
            JobDetail detail = quartzScheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggers = quartzScheduler.getTriggersOfJob(jobKey);
            String desc = Optional.ofNullable(detail.getDescription()).orElse(DEFAULT_DESCRIPTION);
            JobDataMap data = detail.getJobDataMap();

            if (triggers.isEmpty()) {
                return handleCompletedJob(jobKey, desc, data, statusFilter);
            }

            Trigger trigger = triggers.getFirst();
            Trigger.TriggerState state = quartzScheduler.getTriggerState(trigger.getKey());

            LocalDateTime prev = toLocal(trigger.getPreviousFireTime());
            LocalDateTime next = toLocal(trigger.getNextFireTime());

            if (prev == null && data.containsKey(DATA_KEY_LAST_EXECUTED)) {
                prev = fromInstant(Instant.ofEpochMilli(data.getLong(DATA_KEY_LAST_EXECUTED)));
            }

            if (matchesFilter(state, statusFilter)) {
                return new JobInfoDto(jobKey.getName(), desc, state.name(), prev, next);
            }
            return null;

        } catch (SchedulerException e) {
            log.warn("⚠️ Ошибка при обработке {}", jobKey, e);
            return null;
        }
    }

    private JobInfoDto handleCompletedJob(JobKey jobKey, String desc, JobDataMap map, String filter) {
        if (!FILTER_OTHER.equalsIgnoreCase(filter)) {
            return null;
        }
        LocalDateTime last = null;
        if (map.containsKey(DATA_KEY_LAST_EXECUTED)) {
            last = fromInstant(Instant.ofEpochMilli(map.getLong(DATA_KEY_LAST_EXECUTED)));
        }
        return new JobInfoDto(jobKey.getName(), desc, STATUS_COMPLETE, last, null);
    }

    private boolean matchesFilter(Trigger.TriggerState state, String filter) {
        return switch (filter.toLowerCase()) {
            case FILTER_ACTIVE -> state == Trigger.TriggerState.NORMAL;
            case FILTER_PAUSED -> state == Trigger.TriggerState.PAUSED;
            case FILTER_OTHER ->
                Set.of(
                                Trigger.TriggerState.BLOCKED,
                                Trigger.TriggerState.COMPLETE,
                                Trigger.TriggerState.ERROR,
                                Trigger.TriggerState.NONE)
                        .contains(state);
            default -> false;
        };
    }

    private int compareJobs(JobInfoDto a, JobInfoDto b) {
        int statusCompare = Integer.compare(getStatusPriority(a.status()), getStatusPriority(b.status()));
        if (statusCompare != 0) {
            return statusCompare;
        }
        int timeCompare = Comparator.<LocalDateTime>reverseOrder()
                .compare(DateTimeUtils.minSafe(a.previousFireTime()), DateTimeUtils.minSafe(b.previousFireTime()));
        return (timeCompare != 0) ? timeCompare : a.name().compareTo(b.name());
    }

    private int getStatusPriority(String status) {
        return switch (status) {
            case "NORMAL" -> PRIORITY_NORMAL;
            case "PAUSED" -> PRIORITY_PAUSED;
            default -> PRIORITY_OTHER;
        };
    }

    private String generateJobName(String prefix) {
        return JOB_PREFIX + prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
