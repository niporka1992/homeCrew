package ru.homecrew.mapper;

import org.mapstruct.*;
import ru.homecrew.dto.appuser.AppUserCreateDto;
import ru.homecrew.dto.appuser.AppUserDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;

/**
 * AppUserMapper — отвечает за преобразование между DTO и сущностью {@link AppUser}.
 * Используется при создании, обновлении и отображении данных пользователя.
 */
@Mapper(componentModel = "spring", imports = Role.class)
public interface AppUserMapper {

    /**
     * Преобразует DTO в сущность при создании нового пользователя.
     * <ul>
     *   <li>Поле {@code role} (строка) преобразуется в enum {@link Role}.</li>
     *   <li>Поле {@code passwordHash} игнорируется — задаётся в сервисе после хеширования.</li>
     *   <li>Поле {@code isBlocked} устанавливается по умолчанию как активное (false = не заблокирован).</li>
     * </ul>
     */
    @Mapping(target = "role", expression = "java(Role.valueOf(dto.role().toUpperCase()))")
    @Mapping(target = "isBlocked", constant = "false")
    @Mapping(target = "passwordHash", ignore = true)
    AppUser toEntity(AppUserCreateDto dto);

    /**
     * Преобразует сущность {@link AppUser} в DTO для отображения пользователю.
     */
    AppUserDto toDto(AppUser entity);

    /**
     * Обновляет существующую сущность {@link AppUser} на основе DTO.
     * Игнорирует поля, которые не должны изменяться напрямую:
     * <ul>
     *   <li>{@code id}</li>
     *   <li>{@code username}</li>
     *   <li>{@code passwordHash}</li>
     * </ul>
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntityFromDto(AppUserDto dto, @MappingTarget AppUser entity);
}
