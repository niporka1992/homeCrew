package ru.homecrew.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.homecrew.dto.task.TaskCommentDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.TaskComment;
import ru.homecrew.entity.task.TaskHistory;

@DisplayName("💬 TaskCommentMapper — преобразование TaskComment → TaskCommentDto")
class TaskCommentMapperTest {

    private final TaskCommentMapper mapper = Mappers.getMapper(TaskCommentMapper.class);

    @Test
    @DisplayName("✅ toDto: корректно преобразует сущность TaskComment → DTO")
    void toDto_shouldMapEntityCorrectly() {
        // given
        var author = AppUser.builder().username("ivan").fullName("Иван Тестов").build();

        var history = new TaskHistory();

        var comment = TaskComment.builder()
                .text("Всё готово, можно проверять.")
                .author(author)
                .history(history)
                .build();

        // when
        TaskCommentDto dto = mapper.toDto(comment);

        // then
        assertThat(dto.text()).isEqualTo("Всё готово, можно проверять.");
        assertThat(dto.authorName()).isEqualTo("ivan");
        // createdAt не маппится напрямую, поэтому должен быть null (или через BaseEntity, если там есть)
        assertThat(dto.createdAt()).isNull();
    }
}
