package ru.homecrew.service.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import ru.homecrew.dto.scheduler.type.DailyScheduleDto;
import ru.homecrew.dto.scheduler.type.SimpleScheduleDto;

@DisplayName("SchedulerServiceImpl — управление Quartz-задачами")
class SchedulerServiceImplTest {

    @Mock
    private Scheduler quartzScheduler;

    @Mock
    private ScheduleExpressionService expressionService;

    @InjectMocks
    private SchedulerServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("scheduleCronJob(): успешно создаёт CRON-задачу")
    void scheduleCronJob_success() throws Exception {
        DailyScheduleDto dto = new DailyScheduleDto("job1", LocalDate.now(), LocalTime.NOON, null, null);
        when(expressionService.toCron(dto)).thenReturn("0 0 12 * * ?");

        service.scheduleCronJob(dto);

        verify(expressionService).toCron(dto);
        verify(quartzScheduler).scheduleJob(any(JobDetail.class), any(CronTrigger.class));
    }

    @Test
    @DisplayName("scheduleCronJob(): выбрасывает RuntimeException при ошибке Quartz")
    void scheduleCronJob_schedulerError() throws Exception {
        DailyScheduleDto dto = new DailyScheduleDto("job2", LocalDate.now(), LocalTime.NOON, null, null);
        when(expressionService.toCron(dto)).thenReturn("0 0 12 * * ?");
        doThrow(new SchedulerException("fail"))
                .when(quartzScheduler)
                .scheduleJob(any(JobDetail.class), any(CronTrigger.class));

        assertThrows(RuntimeException.class, () -> service.scheduleCronJob(dto));
    }

    @Test
    @DisplayName("scheduleSimpleJob(): успешно создаёт SIMPLE-задачу")
    void scheduleSimpleJob_success() throws Exception {
        SimpleScheduleDto dto =
                new SimpleScheduleDto("simpleJob", LocalDate.now(), LocalTime.NOON, null, null, 3, 1000L);

        service.scheduleSimpleJob(dto);

        verify(quartzScheduler).addJob(any(JobDetail.class), eq(true));
        verify(quartzScheduler).scheduleJob(any(Trigger.class));
    }

    @Test
    @DisplayName("scheduleSimpleJob(): выбрасывает RuntimeException при ошибке Quartz")
    void scheduleSimpleJob_schedulerError() throws Exception {
        SimpleScheduleDto dto =
                new SimpleScheduleDto("simpleJob", LocalDate.now(), LocalTime.NOON, null, null, 3, 1000L);
        doThrow(new SchedulerException("fail")).when(quartzScheduler).addJob(any(JobDetail.class), eq(true));

        assertThrows(RuntimeException.class, () -> service.scheduleSimpleJob(dto));
    }

    @Test
    @DisplayName("setJobActive(): активирует задачу, если актив=true")
    void setJobActive_resumeJob() throws Exception {
        when(quartzScheduler.checkExists(any(JobKey.class))).thenReturn(true);

        service.setJobActive("job_test", true);

        verify(quartzScheduler).resumeJob(any(JobKey.class));
    }

    @Test
    @DisplayName("setJobActive(): приостанавливает задачу, если актив=false")
    void setJobActive_pauseJob() throws Exception {
        when(quartzScheduler.checkExists(any(JobKey.class))).thenReturn(true);

        service.setJobActive("job_test", false);

        verify(quartzScheduler).pauseJob(any(JobKey.class));
    }

    @Test
    @DisplayName("setJobActive(): выбрасывает, если задача не существует")
    void setJobActive_jobNotFound() throws Exception {
        when(quartzScheduler.checkExists(any(JobKey.class))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.setJobActive("missing", true));
    }

    @Test
    @DisplayName("setJobActive(): выбрасывает при ошибке Scheduler")
    void setJobActive_schedulerError() throws Exception {
        when(quartzScheduler.checkExists(any(JobKey.class))).thenThrow(new SchedulerException("fail"));

        assertThrows(RuntimeException.class, () -> service.setJobActive("job", true));
    }

    @Test
    @DisplayName("isJobScheduled(): возвращает true, если задача существует")
    void isJobScheduled_exists() throws Exception {
        when(quartzScheduler.checkExists(any(JobKey.class))).thenReturn(true);
        assertTrue(service.isJobScheduled("job1"));
    }

    @Test
    @DisplayName("isJobScheduled(): выбрасывает при ошибке Scheduler")
    void isJobScheduled_schedulerError() throws Exception {
        when(quartzScheduler.checkExists(any(JobKey.class))).thenThrow(new SchedulerException("fail"));
        assertThrows(RuntimeException.class, () -> service.isJobScheduled("job1"));
    }

    @Test
    @DisplayName("getAllJobsByStatus(): возвращает пустой список при отсутствии задач")
    void getAllJobsByStatus_empty() throws Exception {
        when(quartzScheduler.getJobKeys(GroupMatcher.anyGroup())).thenReturn(Set.of());
        assertTrue(service.getAllJobsByStatus("active").isEmpty());
    }

    @Test
    @DisplayName("getAllJobsByStatus(): корректно обрабатывает SchedulerException")
    void getAllJobsByStatus_schedulerError() throws Exception {
        when(quartzScheduler.getJobKeys(GroupMatcher.anyGroup())).thenThrow(new SchedulerException("fail"));
        assertThrows(RuntimeException.class, () -> service.getAllJobsByStatus("active"));
    }

    @Test
    @DisplayName("scheduleCronJob(): корректно формирует JobDetail и CronTrigger")
    void scheduleCronJob_buildJobAndTrigger() throws Exception {
        DailyScheduleDto dto = new DailyScheduleDto("job1", LocalDate.now(), LocalTime.NOON, null, null);
        when(expressionService.toCron(dto)).thenReturn("0 0 12 * * ?");

        service.scheduleCronJob(dto);

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<CronTrigger> triggerCaptor = ArgumentCaptor.forClass(CronTrigger.class);
        verify(quartzScheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());

        JobDetail job = jobCaptor.getValue();
        CronTrigger trigger = triggerCaptor.getValue();

        assertNotNull(job);
        assertNotNull(trigger);
        assertTrue(job.getKey().getName().startsWith("job_CRON_"));
        assertTrue(trigger.getKey().getName().endsWith("_trigger"));
    }
}
