package ru.homecrew.service.userappservice;

import java.util.List;
import ru.homecrew.dto.appuser.AppUserCreateDto;
import ru.homecrew.dto.appuser.AppUserDto;
import ru.homecrew.dto.appuser.PasswordChangeDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;

/**
 * Сервис управления пользователями системы.
 * <p>
 * Отвечает за создание, получение, обновление, удаление и блокировку пользователей.
 *
 */
public interface UserAppService {

    /**
     * Создаёт нового пользователя.
     *
     * @param userDto DTO с параметрами для создания пользователя
     * @return созданная сущность {@link AppUser}
     */
    AppUser create(AppUserCreateDto userDto);

    /**
     * Возвращает пользователя по имени учётной записи.
     *
     * @param username имя пользователя
     * @return найденный {@link AppUser}
     * @throws java.util.NoSuchElementException если пользователь с таким именем не найден
     */
    AppUser getByUsername(String username);

    /**
     * Возвращает список всех пользователей системы.
     *
     * @return список сущностей {@link AppUser}
     */
    List<AppUser> getAll(Role role);

    /**
     * Обновляет данные пользователя по его имени.
     * Не затрагивает поля {@code username} и {@code passwordHash}.
     *
     * @param username имя пользователя
     * @param appUserDto  новые данные для обновления
     * @return обновлённая сущность {@link AppUser}
     */
    AppUser updateByUserName(String username, AppUserDto appUserDto);

    /**
     * Удаляет пользователя по имени учётной записи.
     *
     * @param username имя пользователя
     */
    void deleteByName(String username);

    /**
     * Изменяет статус блокировки пользователя.
     *
     * @param username  имя пользователя
     * @param isBlocked {@code true}, если пользователь должен быть заблокирован; {@code false} — если активен
     */
    void setIsBlocked(String username, boolean isBlocked);

    /**
     * Меняет пароль пользователя с проверкой сложности.
     * <p>
     * Если указан старый пароль — проверяет его совпадение.
     * Если новый пароль не соответствует политике безопасности, выбрасывает {@link IllegalArgumentException}.
     *
     * @param dto DTO с полями username/newPassword
     */
    void changePassword(PasswordChangeDto dto);
}
