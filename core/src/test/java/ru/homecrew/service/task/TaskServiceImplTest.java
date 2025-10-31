package ru.homecrew.service.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.homecrew.dto.task.*;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.UserExternalIds;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskStatus;
import ru.homecrew.mapper.TaskMapper;
import ru.homecrew.repository.AppUserRepository;
import ru.homecrew.repository.UserExternalIdsRepository;
import ru.homecrew.repository.task.TaskHistoryRepository;
import ru.homecrew.repository.task.TaskRepository;

@DisplayName("TaskServiceImpl — бизнес-логика задач")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskHistoryRepository historyRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private TaskMapper mapper;

    @Mock
    private UserExternalIdsRepository externalIdsRepository;

    @InjectMocks
    private TaskServiceImpl service;

    private AppUser worker;
    private AppUser manager;
    private Task task;
    private TaskCreateDto createDto;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        worker = AppUser.builder()
                .username("worker77")
                .fullName("Андрей Смирнов")
                .build();
        manager =
                AppUser.builder().username("chief88").fullName("Марина Орлова").build();

        task = Task.builder()
                .description("Проверить отчётность")
                .status(TaskStatus.NEW)
                .assignee(worker)
                .build();

        createDto = new TaskCreateDto(
                "Проверка документации",
                "Проверить отчётность",
                "chief88",
                LocalDateTime.now().toString(),
                null);

        taskDto = new TaskDto(
                15L,
                "Проверка документации",
                "Проверить отчётность",
                TaskStatus.NEW,
                "Марина Орлова",
                LocalDateTime.now());
    }

    @Test
    @DisplayName("create(): создаёт задачу и сохраняет историю")
    void create_success() {
        when(mapper.toNewEntity(createDto)).thenReturn(task);
        when(userRepository.findByUsername("chief88")).thenReturn(Optional.of(manager));
        when(taskRepository.save(task)).thenReturn(task);
        when(mapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals("Проверка документации", result.title());
        assertEquals("Марина Орлова", result.assigneeFullName());

        verify(taskRepository).save(task);
        verify(historyRepository).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("create(): выбрасывает EntityNotFound, если назначенный пользователь не найден")
    void create_assigneeNotFound() {
        when(mapper.toNewEntity(createDto)).thenReturn(task);
        when(userRepository.findByUsername("chief88")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(createDto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById(): возвращает DTO найденной задачи")
    void getById_success() {
        when(taskRepository.findById(15L)).thenReturn(Optional.of(task));
        when(mapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = service.getById(15L);

        assertEquals(taskDto, result);
    }

    @Test
    @DisplayName("getById(): выбрасывает EntityNotFound, если задачи нет")
    void getById_notFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getById(999L));
    }

    @Test
    @DisplayName("update(): обновляет задачу и сохраняет изменения")
    void update_success() {
        when(taskRepository.findById(15L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(mapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = service.update(15L, createDto);

        assertNotNull(result);
        verify(mapper).updateEntityFromDto(createDto, task);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("delete(): удаляет задачу, если существует")
    void delete_success() {
        when(taskRepository.findById(15L)).thenReturn(Optional.of(task));
        service.delete(15L);
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("delete(): выбрасывает EntityNotFound при отсутствии задачи")
    void delete_notFound() {
        when(taskRepository.findById(15L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(15L));
    }

    @Test
    @DisplayName("changeStatusAndAssign(): изменяет статус и назначает пользователя")
    void changeStatusAndAssign_success() {
        when(taskRepository.findById(15L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        service.changeStatusAndAssign(15L, TaskStatus.IN_PROGRESS, manager);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(manager, task.getAssignee());
        verify(historyRepository).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("getByAssignee(): возвращает список задач по chatId")
    void getByAssignee_success() {
        UserExternalIds link =
                UserExternalIds.builder().user(worker).telegramChatId(777L).build();
        when(externalIdsRepository.findByTelegramChatId(777L)).thenReturn(Optional.of(link));
        when(taskRepository.findByAssignee_Id(any())).thenReturn(List.of(task));
        when(mapper.toDto(task)).thenReturn(taskDto);

        List<TaskDto> result = service.getByAssignee(777L);

        assertEquals(1, result.size());
        assertEquals("Проверка документации", result.getFirst().title());
    }

    @Test
    @DisplayName("getByAssignee(): выбрасывает EntityNotFound если пользователь не найден")
    void getByAssignee_notFound() {
        when(externalIdsRepository.findByTelegramChatId(777L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getByAssignee(777L));
    }

    @Test
    @DisplayName("getTasks(): возвращает все задачи без фильтра")
    void getTasks_all() {
        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(mapper.toDtoList(any())).thenReturn(List.of(taskDto));

        List<TaskDto> result = service.getTasks(null, null);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getTasks(): фильтрует по статусу и пользователю")
    void getTasks_byStatusAndUser() {
        when(taskRepository.findByStatusAndAssignee_Id(TaskStatus.NEW, 1L)).thenReturn(List.of(task));
        when(mapper.toDtoList(any())).thenReturn(List.of(taskDto));

        List<TaskDto> result = service.getTasks("NEW", 1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getTasks(): выбрасывает EntityNotFound при некорректном статусе")
    void getTasks_invalidStatus() {
        assertThrows(EntityNotFoundException.class, () -> service.getTasks("abracadabra", null));
    }
}
