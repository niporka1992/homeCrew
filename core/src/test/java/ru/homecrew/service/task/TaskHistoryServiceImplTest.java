package ru.homecrew.service.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.repository.task.TaskHistoryRepository;
import ru.homecrew.repository.task.TaskRepository;

@DisplayName("TaskHistoryServiceImpl — добавление записей истории задач")
class TaskHistoryServiceImplTest {

    @Mock
    private TaskHistoryRepository historyRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskHistoryServiceImpl service;

    private AppUser user;
    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = AppUser.builder().username("Test").build();
        task = Task.builder().description("Test task").build();
    }

    @Test
    @DisplayName("addHistory(): успешно сохраняет запись истории для задачи")
    void addHistory_success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        when(historyRepository.save(any(TaskHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskHistory result = service.addHistory(1L, user, TaskActionType.CREATED);

        assertNotNull(result);
        assertSame(task, result.getTask());
        assertSame(user, result.getAppUser());
        assertEquals(TaskActionType.CREATED, result.getActionType());

        ArgumentCaptor<TaskHistory> captor = ArgumentCaptor.forClass(TaskHistory.class);
        verify(historyRepository).save(captor.capture());

        TaskHistory saved = captor.getValue();
        assertSame(task, saved.getTask());
        assertSame(user, saved.getAppUser());
        assertEquals(TaskActionType.CREATED, saved.getActionType());
    }

    @Test
    @DisplayName("addHistory(): выбрасывает EntityNotFoundException, если задача не найдена")
    void addHistory_taskNotFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addHistory(999L, user, TaskActionType.COMMENT_ADDED));

        verify(historyRepository, never()).save(any());
    }
}
