package ru.homecrew.service.scheduler.jobs;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import ru.homecrew.service.task.TaskSchedulerBridgeService;

@DisplayName("TaskCreationJob — обработчик создания задач по расписанию")
class TaskCreationJobTest {

    @Mock
    private TaskSchedulerBridgeService bridgeService;

    @Mock
    private JobExecutionContext context;

    @InjectMocks
    private TaskCreationJob job;

    TaskCreationJobTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("execute(): вызывает handleTriggeredJob() при нормальном выполнении")
    void execute_callsHandleTriggeredJob() throws Exception {
        job.execute(context);
        verify(bridgeService).handleTriggeredJob(context);
    }

    @Test
    @DisplayName("execute(): не пробрасывает исключения при ошибке")
    void execute_doesNotThrowOnException() throws Exception {
        doThrow(new RuntimeException("Ошибка")).when(bridgeService).handleTriggeredJob(context);
        job.execute(context);
        verify(bridgeService).handleTriggeredJob(context);
    }
}
