package ru.homecrew.config.quarts;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import ru.homecrew.service.scheduler.listener.JobExecutionListener;

@Component
@RequiredArgsConstructor
public class QuartzListenerRegistrar {

    private final Scheduler scheduler;
    private final JobExecutionListener jobExecutionListener;

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.getListenerManager().addJobListener(jobExecutionListener);
    }
}
