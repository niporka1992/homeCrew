package ru.homecrew.service.task;

import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.dto.task.*;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.BaseEntity;
import ru.homecrew.entity.UserExternalIds;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.enums.TaskStatus;
import ru.homecrew.mapper.TaskMapper;
import ru.homecrew.repository.AppUserRepository;
import ru.homecrew.repository.UserExternalIdsRepository;
import ru.homecrew.repository.task.TaskHistoryRepository;
import ru.homecrew.repository.task.TaskRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final AppUserRepository userRepository;
    private final TaskMapper mapper;
    private final UserExternalIdsRepository externalIdsRepository;

    @Override
    @Transactional
    public TaskDto create(TaskCreateDto dto) {
        Task task = mapper.toNewEntity(dto);

        if (dto.assigneeUsername() != null) {
            AppUser assignee = getUserByUsername(dto.assigneeUsername());
            task.setAssignee(assignee);
        }

        Task saved = taskRepository.save(task);
        addHistory(saved, saved.getAssignee(), TaskActionType.CREATED, null);

        log.info(
                "Создана задача [{}] пользователем [{}]",
                saved.getId(),
                saved.getAssignee() != null ? saved.getAssignee().getUsername() : "—");

        return mapper.toDto(saved);
    }

    @Override
    public TaskDto getById(Long id) {
        return mapper.toDto(getTaskOrThrow(id));
    }

    @Override
    @Transactional
    public TaskDto update(Long id, TaskCreateDto dto) {
        Task existing = getTaskOrThrow(id);
        mapper.updateEntityFromDto(dto, existing);
        Task updated = taskRepository.save(existing);

        log.info("Обновлена задача [{}]", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Task task = getTaskOrThrow(id);
        taskRepository.delete(task);

        log.info("Удалена задача [{}]", id);
    }

    @Override
    @Transactional
    public void changeStatusAndAssign(Long taskId, TaskStatus newStatus, AppUser user) {
        Task task = getTaskOrThrow(taskId);
        task.setStatus(newStatus);
        task.setAssignee(user);
        taskRepository.save(task);

        addHistory(task, user, TaskActionType.STATUS_CHANGED, newStatus);
        log.info("Задаче [{}] присвоен новый статус [{}] пользователем [{}]", taskId, newStatus, user.getUsername());
    }

    @Override
    public List<TaskDto> getByAssignee(Long telegramChatId) {
        AppUser user = externalIdsRepository
                .findByTelegramChatId(telegramChatId)
                .map(UserExternalIds::getUser)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с chatId не найден: " + telegramChatId));

        return taskRepository.findByAssignee_Id(user.getId()).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<TaskDto> getTasks(String status, Long userId) {
        TaskStatus parsedStatus = parseStatus(status);

        if (parsedStatus == null && userId == null) {
            return mapper.toDtoList(taskRepository.findAll());
        }

        if (parsedStatus != null && userId != null) {
            return mapper.toDtoList(taskRepository.findByStatusAndAssignee_Id(parsedStatus, userId));
        }

        if (parsedStatus != null) {
            return mapper.toDtoList(taskRepository.findByStatus(parsedStatus));
        }

        return mapper.toDtoList(taskRepository.findByAssignee_Id(userId));
    }

    @Override
    public TaskDetailsDto getTaskDetails(Long id) {
        Task task = getTaskOrThrow(id);
        List<TaskHistoryDto> history = mapHistoryList(task.getHistory());

        return mapTaskDetails(task, history);
    }

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Задача не найдена: " + id));
    }

    private AppUser getUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));
    }

    private void addHistory(Task task, AppUser actor, TaskActionType type, TaskStatus statusAfter) {
        historyRepository.save(TaskHistory.builder()
                .task(task)
                .appUser(actor)
                .actionType(type)
                .statusAfter(statusAfter)
                .build());
    }

    private TaskStatus parseStatus(String value) {
        try {
            return value != null ? TaskStatus.valueOf(value.toUpperCase()) : null;
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Некорректный статус: " + value);
        }
    }

    private List<TaskHistoryDto> mapHistoryList(List<TaskHistory> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            return List.of();
        }

        return historyList.stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .map(this::mapHistoryDto)
                .toList();
    }

    private TaskHistoryDto mapHistoryDto(TaskHistory history) {
        return new TaskHistoryDto(
                history.getId(),
                Optional.ofNullable(history.getAppUser())
                        .map(AppUser::getFullName)
                        .orElse("—"),
                history.getActionType(),
                history.getActionType().getDescription(),
                history.getCreatedAt(),
                mapComments(history),
                mapAttachments(history),
                history.getStatusAfter());
    }

    private List<TaskCommentDto> mapComments(TaskHistory history) {
        return Optional.ofNullable(history.getComments()).orElse(List.of()).stream()
                .map(c -> new TaskCommentDto(
                        c.getId(),
                        Optional.ofNullable(c.getAuthor())
                                .map(AppUser::getFullName)
                                .orElse("—"),
                        c.getText(),
                        c.getCreatedAt()))
                .toList();
    }

    private List<TaskMediaDto> mapAttachments(TaskHistory history) {
        return Optional.ofNullable(history.getAttachments()).orElse(List.of()).stream()
                .map(a -> new TaskMediaDto(a.getId(), a.getFileUrl(), a.getCreatedAt()))
                .toList();
    }

    private TaskDetailsDto mapTaskDetails(Task task, List<TaskHistoryDto> historyDtoList) {
        return new TaskDetailsDto(
                task.getId(),
                task.getDescription(),
                Optional.ofNullable(task.getAssignee())
                        .map(AppUser::getFullName)
                        .orElse(null),
                task.getStatus(),
                task.getCreatedAt(),
                historyDtoList);
    }
}
