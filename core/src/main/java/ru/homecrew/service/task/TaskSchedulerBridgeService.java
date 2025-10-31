package ru.homecrew.service.task;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;
import ru.homecrew.dto.task.TaskCreateDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.enums.TaskTypeTrigger;
import ru.homecrew.service.notification.TaskNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSchedulerBridgeService {

    private final TaskService taskService;
    private final TaskNotificationService notificationService;

    /**
     * Вызывается Quartz'ом при срабатывании триггера.
     */
    public void handleTriggeredJob(JobExecutionContext context) {
        var jobDetail = context.getJobDetail();
        var trigger = context.getTrigger();

        String jobName = jobDetail.getKey().getName();
        String description = jobDetail.getDescription() != null ? jobDetail.getDescription() : "(без описания)";

        TaskTypeTrigger type = TaskTypeTrigger.from(trigger);

        log.info("⚙️ Триггер [{}] сработал: {}", jobName, type);

        TaskCreateDto dto = new TaskCreateDto(
                jobName, description, null, LocalDateTime.now().toString(), type);

        TaskDto created = taskService.create(dto);
        notificationService.notifyNewTask(created);
    }
}
