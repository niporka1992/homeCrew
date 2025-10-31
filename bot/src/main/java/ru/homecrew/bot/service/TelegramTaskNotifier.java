package ru.homecrew.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.homecrew.bot.util.CallbackFactory;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.service.BotMessenger;
import ru.homecrew.service.bot.ui.UiButton;
import ru.homecrew.service.bot.ui.UiKeyboard;
import ru.homecrew.service.bot.ui.UiKeyboardRow;
import ru.homecrew.service.notification.TaskNotificationService;

@Service
@RequiredArgsConstructor
public class TelegramTaskNotifier implements TaskNotificationService {

    private final BotMessenger messenger;

    @Value("${telegram.group-chat-id}")
    private Long groupChatId;

    @Override
    public void notifyNewTask(TaskDto task) {
        StringBuilder text = new StringBuilder("🆕 *Новая задача по расписанию!*\n\n");

        if (task.description() != null && !task.description().isBlank()) {
            text.append("📝 *Описание:* ")
                    .append(messenger.escapeMarkdown(task.description()))
                    .append("\n\n");
        }

        text.append("Кто возьмёт задачу?");

        UiKeyboard keyboard =
                UiKeyboard.ofRows(UiKeyboardRow.of(new UiButton("✅ Взять", CallbackFactory.takeTask(task.id()))));

        messenger.sendMessageWithKeyboard(groupChatId, text.toString(), keyboard);
    }
}
