package ru.homecrew.bot.service.media;

/**
 * Универсальный интерфейс для обработки пользовательских входных данных
 * (медиа, текст, команды) независимо от конкретного мессенджера.
 *
 * Реализации: Telegram, WhatsApp, etc.
 */
public interface MediaInputProcessor {

    /**
     * Попытка обработать входные данные пользователя (медиа, текст и т.п.).
     *
     * @param rawEvent — исходный объект события мессенджера (Update, Message, Event и т.д.)
     * @return true — если событие было обработано и не требует дальнейшей маршрутизации.
     */
    boolean process(Object rawEvent);
}
