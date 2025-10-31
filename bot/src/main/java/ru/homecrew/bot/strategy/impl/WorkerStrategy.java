package ru.homecrew.bot.strategy.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.homecrew.bot.annotation.RoleMapping;
import ru.homecrew.bot.model.BotContext;
import ru.homecrew.bot.service.UserInputWaiter;
import ru.homecrew.bot.strategy.BotUserStrategy;
import ru.homecrew.bot.util.CallbackFactory;
import ru.homecrew.dto.task.TaskCommentDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.UserExternalIds;
import ru.homecrew.enums.Role;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.enums.TaskStatus;
import ru.homecrew.repository.UserExternalIdsRepository;
import ru.homecrew.service.BotMessenger;
import ru.homecrew.service.bot.ui.UiButton;
import ru.homecrew.service.bot.ui.UiKeyboard;
import ru.homecrew.service.bot.ui.UiKeyboardRow;
import ru.homecrew.service.task.TaskAttachmentService;
import ru.homecrew.service.task.TaskCommentService;
import ru.homecrew.service.task.TaskHistoryService;
import ru.homecrew.service.task.TaskService;
import ru.homecrew.util.DateTimeUtils;

@Component
@RoleMapping(Role.WORKER)
@RequiredArgsConstructor
@Slf4j
public class WorkerStrategy implements BotUserStrategy {

    private final BotMessenger messenger;
    private final OkHttpTelegramClient client;
    private final TaskService taskService;
    private final UserExternalIdsRepository externalIdsRepository;
    private final TaskCommentService commentService;
    private final UserInputWaiter inputWaiter;
    private final TaskAttachmentService attachmentService;
    private final TaskHistoryService historyService;

    @Value("${app.telegram.group-chat-id}")
    private Long groupChatId;

    private final Map<String, BiConsumer<String, BotContext>> callbackHandlers = new LinkedHashMap<>();

    @PostConstruct
    private void initHandlers() {
        callbackHandlers.put(CallbackFactory.PREFIX_TASKS_TAKE, this::handleTakeTask);
        callbackHandlers.put(CallbackFactory.PREFIX_TASKS, (data, ctx) -> showMyTasks(ctx));
        callbackHandlers.put(CallbackFactory.PREFIX_TASK_DONE, this::handleTaskDone);
        callbackHandlers.put(CallbackFactory.PREFIX_TASK_COMMENT, this::handleTaskComment);
        callbackHandlers.put(CallbackFactory.PREFIX_TASK_MEDIA, this::handleTaskMedia);
        callbackHandlers.put(CallbackFactory.PREFIX_TASK, this::showTaskDetails);
    }

    // ====================== MENU ======================
    @Override
    public void showMenu(BotContext ctx) {
        UiKeyboard keyboard =
                UiKeyboard.ofRows(UiKeyboardRow.of(new UiButton("📋 Мои задачи", CallbackFactory.PREFIX_TASKS)));

        messenger.sendMessageWithKeyboard(
                ctx.getChatId(),
                """
                👷 *Меню работника*
                Выберите действие:
                """,
                keyboard);
    }

    @Override
    public void handleMessage(String text, BotContext ctx) {
        switch (text.toLowerCase()) {
            case "/start", "/menu" -> showMenu(ctx);
            case "/tasks" -> showMyTasks(ctx);
            default -> messenger.sendMessage(ctx.getChatId(), "❓ Неизвестная команда. Попробуй /menu или /tasks");
        }
    }

    // ====================== CALLBACK DISPATCH ======================
    @Override
    public void handleCallback(String data, BotContext ctx) {
        try {
            log.info("📦 CALLBACK DATA = '{}'", data);

            for (var entry : callbackHandlers.entrySet()) {
                if (data.startsWith(entry.getKey())) {
                    entry.getValue().accept(data, ctx);
                    return;
                }
            }

            messenger.sendMessage(ctx.getChatId(), "⚙️ Неизвестный callback: " + data);

        } catch (Exception e) {
            log.error("❌ Ошибка при обработке callback: {}", data, e);
            messenger.sendMessage(ctx.getChatId(), "⚠️ Ошибка при обработке кнопки: " + e.getMessage());
        }
    }

    // ====================== HANDLERS ======================
    private void showMyTasks(BotContext ctx) {
        List<TaskDto> tasks = taskService.getByAssignee(ctx.getChatId()).stream()
                .filter(x -> x.status().equals(TaskStatus.IN_PROGRESS))
                .toList();

        if (tasks.isEmpty()) {
            messenger.sendMessage(ctx.getChatId(), "📭 У тебя пока нет активных задач.");
            return;
        }

        String sb = "📋 *Мои задачи:*\n\n";
        UiKeyboard keyboard = new UiKeyboard();

        for (TaskDto t : tasks) {
            keyboard.addRow(new UiButton("➡️ " + t.description(), CallbackFactory.PREFIX_TASK + t.id()));
        }

        messenger.sendMessageWithKeyboard(ctx.getChatId(), sb, keyboard);
    }

    private void showTaskDetails(String data, BotContext ctx) {
        Long taskId = extractTaskId(data);
        TaskDto task = taskService.getById(taskId);

        StringBuilder sb = new StringBuilder();
        sb.append("📋 *Карточка задачи*\n").append("──────────────────────────────\n");

        appendIfPresent(sb, "🏷 *Название:* ", task.title());
        appendIfPresent(sb, "📝 *Описание:* ", task.description());
        appendIfPresent(sb, "📅 *Дата создания:* ", DateTimeUtils.formatRu(task.dateOfCreate()));

        sb.append("\n──────────────────────────────");

        UiKeyboard keyboard = UiKeyboard.ofRows(
                UiKeyboardRow.of(
                        new UiButton("✅ Завершить", CallbackFactory.PREFIX_TASK_DONE + task.id()),
                        new UiButton("💬 Коммент", CallbackFactory.PREFIX_TASK_COMMENT + task.id())),
                UiKeyboardRow.of(
                        new UiButton("📸 Добавить фото/видео", CallbackFactory.PREFIX_TASK_MEDIA + task.id())));

        messenger.sendMessageWithKeyboard(ctx.getChatId(), messenger.escapeMarkdown(sb.toString()), keyboard);
    }

    private void handleTaskDone(String data, BotContext ctx) {
        Long taskId = extractTaskId(data);
        AppUser user = findUserByChat(ctx.getChatId());
        taskService.changeStatusAndAssign(taskId, TaskStatus.DONE, user);

        try {
            if (ctx.getMessageId() != null) {
                client.execute(new DeleteMessage(ctx.getChatId().toString(), ctx.getMessageId()));
            }
        } catch (TelegramApiException e) {
            log.warn("⚠️ Не удалось удалить сообщение после завершения задачи #{}: {}", taskId, e.getMessage());
        }

        messenger.sendMessage(ctx.getChatId(), "✅ Задача #" + taskId + " завершена!\nОтличная работа 💪");
    }

    private void handleTakeTask(String data, BotContext ctx) {
        Long taskId = extractTaskId(data);

        try {
            if (ctx.getMessageId() != null) {
                client.execute(new DeleteMessage(groupChatId.toString(), ctx.getMessageId()));
            }
        } catch (TelegramApiException e) {
            log.warn("⚠️ Не удалось удалить сообщение из группы: {}", e.getMessage());
        }
        AppUser user = findUserByChat(ctx.getChatId());
        taskService.changeStatusAndAssign(taskId, TaskStatus.IN_PROGRESS, user);

        messenger.sendMessage(ctx.getChatId(), "✅ Ты взял задачу #" + taskId + ".\nПогнали выполнять 💪");
    }

    private void handleTaskComment(String data, BotContext ctx) {
        Long taskId = extractTaskId(data);
        Long chatId = ctx.getChatId();

        messenger.sendMessage(chatId, "💬 Введи комментарий к задаче #" + taskId + ":");
        inputWaiter.waitForInput(chatId, inputText -> {
            try {
                AppUser user = findUserByChat(chatId);
                TaskCommentDto comment = commentService.addComment(taskId, user, inputText);
                messenger.sendMessage(chatId, "✅ Комментарий добавлен:\n_" + comment.text() + "_");
            } catch (Exception e) {
                log.error("❌ Ошибка при добавлении комментария", e);
                messenger.sendMessage(chatId, "⚠️ Не удалось добавить комментарий: " + e.getMessage());
            }
        });
    }

    private void handleTaskMedia(String data, BotContext ctx) {
        Long taskId = extractTaskId(data);
        Long chatId = ctx.getChatId();

        messenger.sendMessage(chatId, "📷 Отправь одно или несколько фото (или видео) для задачи #" + taskId + ":");

        inputWaiter.waitForMediaInput(chatId, fileIds -> {
            try {
                AppUser user = findUserByChat(chatId);
                var history = historyService.addHistory(taskId, user, TaskActionType.ATTACHMENT_ADDED);
                fileIds.forEach(fileId -> attachmentService.addAttachment(history.getId(), fileId));
                messenger.sendMessage(chatId, "✅ Загружено файлов: " + fileIds.size());
            } catch (Exception e) {
                log.error("❌ Ошибка при добавлении медиа: {}", e.getMessage(), e);
                messenger.sendMessage(chatId, "⚠️ Не удалось сохранить медиа: " + e.getMessage());
            }
        });
    }

    // ====================== UTILS ======================
    private Long extractTaskId(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Callback не должен быть пустым");
        }
        String[] parts = data.split(":");
        String last = parts[parts.length - 1];
        if (!last.matches("\\d+")) {
            throw new IllegalArgumentException("В callback отсутствует числовой ID: " + data);
        }
        return Long.parseLong(last);
    }

    private AppUser findUserByChat(Long chatId) {
        return externalIdsRepository
                .findByTelegramChatId(chatId)
                .map(UserExternalIds::getUser)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с chatId не найден: " + chatId));
    }

    private void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append('\n').append(label).append(value);
        }
    }
}
