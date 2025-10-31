package ru.homecrew.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.homecrew.dto.appuser.AppUserCreateDto;
import ru.homecrew.dto.appuser.AppUserDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;

@DisplayName(" AppUserMapper — преобразование DTO ↔ Entity")
class AppUserMapperTest {

    private final AppUserMapper mapper = Mappers.getMapper(AppUserMapper.class);

    @Test
    @DisplayName(" toEntity: корректно преобразует AppUserCreateDto → AppUser")
    void toEntity_shouldMapCreateDtoCorrectly() {
        var dto =
                new AppUserCreateDto("testUser", "Иван Тестов", "owner", "+79998887766", "test@mail.com", "Secret123!");

        AppUser entity = mapper.toEntity(dto);

        assertThat(entity.getUsername()).isEqualTo("testUser");
        assertThat(entity.getFullName()).isEqualTo("Иван Тестов");
        assertThat(entity.getEmail()).isEqualTo("test@mail.com");
        assertThat(entity.getPhone()).isEqualTo("+79998887766");
        assertThat(entity.getRole()).isEqualTo(Role.OWNER);
        assertThat(entity.getPasswordHash()).isNull();
    }

    @Test
    @DisplayName("✅ updateEntityFromDto: обновляет только ненулевые поля, не трогая username/passwordHash")
    void updateEntityFromDto_shouldUpdateOnlyNonNullFields() {
        // given
        var existing = new AppUser();
        existing.setUsername("oldUser");
        existing.setFullName("Старое Имя");
        existing.setPhone("+71112223344");
        existing.setEmail("old@mail.com");
        existing.setPasswordHash("hash");
        existing.setRole(Role.WORKER);

        var dto = new AppUserDto(null, "Новое Имя", Role.OWNER, null, "new@mail.com", false);

        // when
        mapper.updateEntityFromDto(dto, existing);

        // then
        assertThat(existing.getUsername()).isEqualTo("oldUser");
        assertThat(existing.getPasswordHash()).isEqualTo("hash");
        assertThat(existing.getFullName()).isEqualTo("Новое Имя");
        assertThat(existing.getRole()).isEqualTo(Role.OWNER);
        assertThat(existing.getPhone()).isEqualTo("+71112223344");
        assertThat(existing.getEmail()).isEqualTo("new@mail.com");
    }
}
