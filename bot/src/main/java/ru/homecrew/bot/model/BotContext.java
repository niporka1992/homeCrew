package ru.homecrew.bot.model;

import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.homecrew.enums.Role;

/**
 * BotContext — контекст взаимодействия пользователя с ботом.
 * Хранит минимальный набор данных для маршрутизации и логики.
 */
@Data
@Builder
public class BotContext {
    private Integer messageId;
    private Long chatId;
    private String username;
    private Role role;
    private Update update;
}
