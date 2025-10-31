package ru.homecrew.bot;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.homecrew.bot.model.BotContext;
import ru.homecrew.bot.service.BotCommandRouterTG;
import ru.homecrew.bot.service.media.MediaInputProcessor;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;
import ru.homecrew.service.link.ExternalLinkUserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeCrewBot implements LongPollingUpdateConsumer {

    private final BotCommandRouterTG router;
    private final ExternalLinkUserService userService;
    private final MediaInputProcessor inputProcessor;

    @Value("${telegram.admin.chatId}")
    private Long mainAdminId;

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(this::safeHandle);
    }

    private void safeHandle(Update update) {
        try {
            handle(update);
        } catch (Exception e) {
            log.error("Ошибка при обработке update", e);
        }
    }

    private void handle(Update update) {
        if (update == null) {
            return;
        }

        if (inputProcessor.process(update)) {
            return;
        }

        TelegramEvent event = TelegramEvent.of(update);
        if (event == null) {
            return;
        }

        AppUser user = userService.findOrCreateByExternalId(event.chatId().toString());
        if (user.isBlocked()) {
            log.info(" Пользователь {} ({}): заблокирован.", event.username(), event.chatId());
            return;
        }

        BotContext ctx = buildContext(event.chatId(), event.username(), user, event.messageId());

        if (event.isCallback()) router.routeCallback(update, ctx);
        else router.routeMessage(update, ctx);

        log.debug(
                "{} from {} (role={}): {}",
                event.isCallback() ? "Callback" : "Message",
                event.username(),
                user.getRole(),
                event.payload());
    }

    private BotContext buildContext(Long chatId, String username, AppUser user, Integer messageId) {
        Role role = chatId.equals(mainAdminId)
                ? Role.OWNER
                : Optional.ofNullable(user.getRole()).orElse(Role.WORKER);

        return BotContext.builder()
                .chatId(chatId)
                .username(username)
                .messageId(messageId)
                .role(role)
                .build();
    }

    private record TelegramEvent(Long chatId, String username, String payload, boolean isCallback, Integer messageId) {

        static TelegramEvent of(Update update) {
            if (update == null) {
                return null;
            }

            if (update.hasMessage() && update.getMessage().getFrom() != null) {
                var msg = update.getMessage();
                return new TelegramEvent(
                        msg.getChatId(),
                        Optional.ofNullable(msg.getFrom().getUserName()).orElse("unknown"),
                        Optional.ofNullable(msg.getText()).orElse(""),
                        false,
                        msg.getMessageId());
            }

            if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
                var cb = update.getCallbackQuery();
                return new TelegramEvent(
                        cb.getFrom().getId(),
                        Optional.ofNullable(cb.getFrom().getUserName()).orElse("unknown"),
                        Optional.ofNullable(cb.getData()).orElse(""),
                        true,
                        cb.getMessage().getMessageId());
            }

            log.debug("Пропускаем update без полезных данных: {}", update);
            return null;
        }
    }
}
