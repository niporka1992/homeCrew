package ru.homecrew.service.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.homecrew.dto.task.TaskCommentDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskComment;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.mapper.TaskCommentMapper;
import ru.homecrew.repository.task.TaskHistoryRepository;
import ru.homecrew.repository.task.TaskRepository;

@DisplayName("TaskCommentServiceImpl — добавление комментариев к задачам")
class TaskCommentServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskHistoryRepository historyRepository;

    @Mock
    private TaskCommentMapper mapper;

    @InjectMocks
    private TaskCommentServiceImpl service;

    private AppUser user;
    private Task task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = AppUser.builder().username("text").build();
        task = Task.builder().description("Test task").build();
    }

    @Test
    @DisplayName("addComment(): успешно сохраняет историю и комментарий, возвращает DTO")
    void addComment_success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        TaskCommentDto expectedDto = new TaskCommentDto(null, "text", "text", null);
        when(mapper.toDto(any(TaskComment.class))).thenReturn(expectedDto);

        TaskCommentDto result = service.addComment(1L, user, "text");

        // Проверка результата
        assertNotNull(result);
        assertEquals("text", result.authorName());
        assertEquals("text", result.text());

        // Проверка сохранённой истории
        ArgumentCaptor<TaskHistory> captor = ArgumentCaptor.forClass(TaskHistory.class);
        verify(historyRepository).save(captor.capture());

        TaskHistory savedHistory = captor.getValue();
        assertEquals(TaskActionType.COMMENT_ADDED, savedHistory.getActionType());
        assertSame(task, savedHistory.getTask());
        assertSame(user, savedHistory.getAppUser());
        assertNotNull(savedHistory.getComments());
        assertEquals(1, savedHistory.getComments().size());

        TaskComment savedComment = savedHistory.getComments().getFirst();
        assertEquals("text", savedComment.getText());
        assertSame(user, savedComment.getAuthor());
    }

    @Test
    @DisplayName("addComment(): выбрасывает EntityNotFoundException, если задача не найдена")
    void addComment_taskNotFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addComment(999L, user, "test comment"));

        verify(historyRepository, never()).save(any());
        verify(mapper, never()).toDto(any());
    }
}
