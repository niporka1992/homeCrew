package ru.homecrew.service.scheduler.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import ru.homecrew.service.task.TaskSchedulerBridgeService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreationJob implements Job {

    private final TaskSchedulerBridgeService bridgeService;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            bridgeService.handleTriggeredJob(context);
        } catch (Exception e) {
            log.error("Ошибка при создании задачи: {}", e.getMessage(), e);
        }
    }
}
