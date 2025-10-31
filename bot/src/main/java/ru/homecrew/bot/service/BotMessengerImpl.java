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
import ru.homecrew.service.BotMessenger;
import ru.homecrew.service.bot.ui.UiButton;
import ru.homecrew.service.bot.ui.UiKeyboard;
import ru.homecrew.service.bot.ui.UiKeyboardRow;

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
    public void sendMessageWithKeyboard(Long chatId, String text, UiKeyboard keyboard) {
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

    private InlineKeyboardMarkup convertKeyboard(UiKeyboard keyboard) {
        List<InlineKeyboardRow> rows = keyboard.rows().stream()
                .map(this::convertRow)
                .map(InlineKeyboardRow::new)
                .toList();

        return new InlineKeyboardMarkup(rows);
    }

    private List<InlineKeyboardButton> convertRow(UiKeyboardRow row) {
        return row.buttons().stream().map(this::convertButton).toList();
    }

    private InlineKeyboardButton convertButton(UiButton button) {
        InlineKeyboardButton tgBtn = new InlineKeyboardButton(button.label());
        tgBtn.setCallbackData(button.callbackData());
        return tgBtn;
    }
}
