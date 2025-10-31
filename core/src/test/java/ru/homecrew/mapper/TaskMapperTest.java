package ru.homecrew.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.homecrew.dto.task.TaskCreateDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.Task;
import ru.homecrew.enums.TaskStatus;
import ru.homecrew.enums.TaskTypeTrigger;

@DisplayName(" TaskMapper — преобразование между Task и DTO")
class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    @DisplayName(" toNewEntity: создаёт новую задачу со статусом NEW и без assignee")
    void toNewEntity_shouldMapCorrectly() {
        var dto = new TaskCreateDto(
                "Проверить отчёт", "Нужно сверить цифры в таблице", "ivan", "2025-10-31T12:00", TaskTypeTrigger.CRON);

        Task entity = mapper.toNewEntity(dto);

        assertThat(entity.getDescription()).isEqualTo("Нужно сверить цифры в таблице");
        assertThat(entity.getType()).isEqualTo(TaskTypeTrigger.CRON);
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(entity.getAssignee()).isNull();
    }

    @Test
    @DisplayName(" toDto: корректно маппит Task → TaskDto c ФИО исполнителя и датой создания")
    void toDto_shouldMapCorrectly() {
        var assignee = AppUser.builder().fullName("Иван Тестов").build();

        var task = Task.builder()
                .description("Собрать отчёт")
                .assignee(assignee)
                .status(TaskStatus.IN_PROGRESS)
                .type(TaskTypeTrigger.CRON)
                .build();

        TaskDto dto = mapper.toDto(task);

        assertThat(dto.description()).isEqualTo("Собрать отчёт");
        assertThat(dto.assigneeFullName()).isEqualTo("Иван Тестов");
        assertThat(dto.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName(" toDtoList: корректно маппит список задач")
    void toDtoList_shouldMapAllTasks() {
        var user = AppUser.builder().fullName("Иван").build();
        var t1 = Task.builder()
                .description("Первая")
                .assignee(user)
                .status(TaskStatus.NEW)
                .build();
        var t2 = Task.builder()
                .description("Вторая")
                .assignee(user)
                .status(TaskStatus.DONE)
                .build();

        List<TaskDto> dtoList = mapper.toDtoList(List.of(t1, t2));

        assertThat(dtoList).hasSize(2);
        assertThat(dtoList.get(0).description()).isEqualTo("Первая");
        assertThat(dtoList.get(0).assigneeFullName()).isEqualTo("Иван");
        assertThat(dtoList.get(1).status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName(" updateEntityFromDto: обновляет поля, не трогая связи и id")
    void updateEntityFromDto_shouldUpdateOnlyMutableFields() {
        var existing = Task.builder()
                .description("Старое описание")
                .type(TaskTypeTrigger.CRON)
                .status(TaskStatus.NEW)
                .build();

        var dto = new TaskCreateDto(
                "Новое задание", "Новое описание", "worker", "2025-11-01T12:00", TaskTypeTrigger.CRON);

        mapper.updateEntityFromDto(dto, existing);

        assertThat(existing.getDescription()).isEqualTo("Новое описание");
        assertThat(existing.getType()).isEqualTo(TaskTypeTrigger.CRON);
        assertThat(existing.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(existing.getAssignee()).isNull();
        assertThat(existing.getHistory()).isNull();
    }

    @Test
    @DisplayName(" getAssigneeFullName: безопасно возвращает null, если assignee = null")
    void getAssigneeFullName_shouldHandleNull() {
        assertThat(mapper.getAssigneeFullName(null)).isNull();
    }
}
