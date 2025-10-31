package ru.homecrew.bot.strategy;

import ru.homecrew.bot.model.BotContext;

public interface BotUserStrategy {

    /** Отображает стартовое меню пользователя */
    void showMenu(BotContext ctx);

    /** Обработка текстовых команд и сообщений */
    void handleMessage(String text, BotContext ctx);

    /** Обработка callback-кнопок */
    void handleCallback(String data, BotContext ctx);
}
