package ru.homecrew.service.scheduler.listener;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobExecutionListener extends JobListenerSupport {

    @Override
    public String getName() {
        return "globalJobListener";
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        try {
            JobDetail job = context.getJobDetail();
            JobDataMap dataMap = job.getJobDataMap();
            long executedAt = context.getFireTime().getTime();

            dataMap.put("lastExecutedAt", executedAt);
            context.getScheduler().addJob(job, true);

            log.debug(" Сохранено lastExecutedAt={} для {}", executedAt, job.getKey().getName());
        } catch (SchedulerException e) {
            log.error("Ошибка при сохранении времени последнего запуска", e);
        }
    }
}
