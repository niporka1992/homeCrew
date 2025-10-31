package ru.homecrew.bot.strategy.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.homecrew.bot.annotation.RoleMapping;
import ru.homecrew.bot.model.BotContext;
import ru.homecrew.bot.strategy.BotUserStrategy;
import ru.homecrew.enums.Role;
import ru.homecrew.service.BotMessenger;

@Component
@RoleMapping(Role.OWNER)
@RequiredArgsConstructor
public class AdminStrategy implements BotUserStrategy {

    private final BotMessenger messenger;

    @Override
    public void showMenu(BotContext ctx) {

        messenger.sendMessage(
                ctx.getChatId(),
                """
                👑 *Меню администратора*
                Выберите действие:
                """);
    }

    @Override
    public void handleMessage(String text, BotContext ctx) {
        //
    }

    @Override
    public void handleCallback(String data, BotContext ctx) {
        //
    }
}
