package ru.homecrew.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Конфигурация Telegram-клиента.
 */
@Configuration
public class TelegramBotConfig {

    @Bean
    @Profile("dev")
    public OkHttpTelegramClient devTelegramClient(@Value("${app.telegram.token}") String token) {
        return new OkHttpTelegramClient(token);
    }

    @Bean
    @Profile("prod")
    public OkHttpTelegramClient prodTelegramClient(@Value("${app.telegram.token}") String token) {
        return new OkHttpTelegramClient(token);
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApp() {
        return new TelegramBotsLongPollingApplication();
    }
}
