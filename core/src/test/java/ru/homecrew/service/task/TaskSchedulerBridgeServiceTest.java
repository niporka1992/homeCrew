package ru.homecrew.service.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.*;
import ru.homecrew.dto.task.TaskCreateDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.enums.TaskStatus;
import ru.homecrew.enums.TaskTypeTrigger;
import ru.homecrew.service.notification.TaskNotificationService;

@DisplayName("TaskSchedulerBridgeService — обработка срабатывания Quartz-триггера")
class TaskSchedulerBridgeServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskNotificationService notificationService;

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private CronTrigger cronTrigger;

    @InjectMocks
    private TaskSchedulerBridgeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("handleTriggeredJob(): создаёт задачу и отправляет уведомление (CRON-триггер)")
    void handleTriggeredJob_success_cron() {
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getTrigger()).thenReturn(cronTrigger);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("job_test"));
        when(jobDetail.getDescription()).thenReturn("описание задачи");

        TaskDto created =
                new TaskDto(1L, "job_test", "описание задачи", TaskStatus.DONE, "Иван Иванов", LocalDateTime.now());
        when(taskService.create(any(TaskCreateDto.class))).thenReturn(created);

        service.handleTriggeredJob(context);

        ArgumentCaptor<TaskCreateDto> dtoCaptor = ArgumentCaptor.forClass(TaskCreateDto.class);
        verify(taskService).create(dtoCaptor.capture());
        verify(notificationService).notifyNewTask(created);

        TaskCreateDto dto = dtoCaptor.getValue();
        assertEquals("описание задачи", dto.description());
        assertEquals(TaskTypeTrigger.CRON, dto.type());
    }

    @Test
    @DisplayName("handleTriggeredJob(): корректно обрабатывает null-описание задачи")
    void handleTriggeredJob_noDescription() {
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getTrigger()).thenReturn(cronTrigger);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("job_no_desc"));
        when(jobDetail.getDescription()).thenReturn(null);

        TaskDto dummy =
                new TaskDto(2L, "job_no_desc", "(без описания)", TaskStatus.NEW, "Система", LocalDateTime.now());
        when(taskService.create(any(TaskCreateDto.class))).thenReturn(dummy);

        service.handleTriggeredJob(context);

        ArgumentCaptor<TaskCreateDto> dtoCaptor = ArgumentCaptor.forClass(TaskCreateDto.class);
        verify(taskService).create(dtoCaptor.capture());
        TaskCreateDto dto = dtoCaptor.getValue();

        assertEquals("(без описания)", dto.description());
        assertEquals(TaskTypeTrigger.CRON, dto.type());
    }

    @Test
    @DisplayName("handleTriggeredJob(): выбрасывает IllegalArgumentException при неизвестном типе триггера")
    void handleTriggeredJob_unknownTriggerType() {
        Trigger unknownTrigger = mock(Trigger.class);
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getTrigger()).thenReturn(unknownTrigger);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("weird_job"));
        when(jobDetail.getDescription()).thenReturn("что-то странное");

        assertThrows(IllegalArgumentException.class, () -> service.handleTriggeredJob(context));

        verify(taskService, never()).create(any());
        verify(notificationService, never()).notifyNewTask(any());
    }
}
