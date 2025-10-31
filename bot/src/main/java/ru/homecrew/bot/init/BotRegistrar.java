package ru.homecrew.bot.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.homecrew.bot.HomeCrewBot;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotRegistrar {

    private final TelegramBotsLongPollingApplication botsApp;
    private final HomeCrewBot homeCrewBot;

    @Value("${app.telegram.token}")
    private String token;

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void registerBot() throws TelegramApiException {
        botsApp.registerBot(token, homeCrewBot);
        log.info("\uD83E\uDD16 Bot registered successfully ({}...).", token.substring(0, Math.min(token.length(), 8)));
    }
}
