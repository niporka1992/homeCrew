package ru.homecrew.bot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.homecrew.model.interaction.ActionGroup;
import ru.homecrew.model.interaction.ActionLayout;
import ru.homecrew.model.interaction.ActionOption;
import ru.homecrew.service.BotMessenger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotMessengerImpl implements BotMessenger {

    private final OkHttpTelegramClient client;

    @Override
    public void sendMessage(Long chatId, String text) {
        try {
            SendMessage msg = new SendMessage(chatId.toString(), text);
            client.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageWithKeyboard(Long chatId, String text, ActionLayout keyboard) {
        try {
            InlineKeyboardMarkup markup = convertKeyboard(keyboard);
            SendMessage msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(markup)
                    .parseMode("Markdown")
                    .build();
            client.execute(msg);
        } catch (TelegramApiException e) {
            log.error(" Ошибка при отправке с клавиатурой: {}", e.getMessage(), e);
        }
    }

    private InlineKeyboardMarkup convertKeyboard(ActionLayout keyboard) {
        List<InlineKeyboardRow> rows = keyboard.groups().stream()
                .map(this::convertRow)
                .map(InlineKeyboardRow::new)
                .toList();

        return new InlineKeyboardMarkup(rows);
    }

    private List<InlineKeyboardButton> convertRow(ActionGroup row) {
        return row.actions().stream().map(this::convertButton).toList();
    }

    private InlineKeyboardButton convertButton(ActionOption button) {
        InlineKeyboardButton tgBtn = new InlineKeyboardButton(button.label());
        tgBtn.setCallbackData(button.actionCode());
        return tgBtn;
    }
}
