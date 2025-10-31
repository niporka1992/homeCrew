package ru.homecrew.bot.service;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.homecrew.bot.model.BotContext;
import ru.homecrew.bot.strategy.BotUserStrategy;
import ru.homecrew.enums.Role;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotCommandRouterTG {

    private final Map<Role, BotUserStrategy> strategies;

    public void routeMessage(Update update, BotContext ctx) {
        if (update == null || update.getMessage() == null) {
            return;
        }

        String text = update.getMessage().getText();
        BotUserStrategy strategy = resolve(ctx);

        if ("/start".equalsIgnoreCase(text)) {
            strategy.showMenu(ctx);
        } else {
            strategy.handleMessage(text, ctx);
        }
    }

    public void routeCallback(Update update, BotContext ctx) {
        if (update == null || update.getCallbackQuery() == null) {
            return;
        }

        String data = update.getCallbackQuery().getData();
        Objects.requireNonNull(resolve(ctx)).handleCallback(data, ctx);
    }

    private BotUserStrategy resolve(BotContext ctx) {
        BotUserStrategy strategy = strategies.get(ctx.getRole());
        if (strategy == null) {
            log.warn(" Нет стратегии для роли {}, нужно назначить другую  роль", ctx.getRole());
        }
        return strategy;
    }
}
