package ru.homecrew.service.link;

import ru.homecrew.entity.AppUser;

/**
 * Универсальный сервис связи внешнего идентификатора (канала) с пользователем системы.
 * Например: Telegram chatId, Slack ID, Discord ID и т.п.
 */
public interface ExternalLinkUserService {

    /**
     * Ищет пользователя по внешнему идентификатору (например, chatId).
     */
    AppUser findOrCreateByExternalId(String externalId);

    /**
     * Связывает пользователя с внешним идентификатором.
     */
    void linkUser(AppUser user, String externalId);
}
