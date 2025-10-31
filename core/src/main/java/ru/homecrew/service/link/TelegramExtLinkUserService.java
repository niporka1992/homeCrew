package ru.homecrew.service.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.UserExternalIds;
import ru.homecrew.enums.Role;
import ru.homecrew.repository.AppUserRepository;
import ru.homecrew.repository.UserExternalIdsRepository;

/**
 * Сервис связывает пользователей с их внешними (Telegram) идентификаторами.
 * Отвечает за поиск или создание пользователя при первом взаимодействии с ботом.
 */
@Service
@RequiredArgsConstructor
public class TelegramExtLinkUserService implements ExternalLinkUserService {

    private static final String TELEGRAM_PREFIX = "tg_";

    private final UserExternalIdsRepository repo;
    private final AppUserRepository userRepo;

    @Transactional
    @Override
    public AppUser findOrCreateByExternalId(String chatId) {
        Long chatIdLong = Long.parseLong(chatId);

        return repo.findByTelegramChatId(chatIdLong)
                .map(UserExternalIds::getUser)
                .orElseGet(() -> createLinkedUser(chatIdLong));
    }

    /**
     * Создаёт нового пользователя и связывает его с Telegram chatId.
     */
    private AppUser createLinkedUser(Long chatId) {
        AppUser user = findOrCreateBaseUser(chatId);
        linkUserInternal(user, chatId);
        return user;
    }

    /**
     * Ищет существующего пользователя по username или создаёт нового "заглушку".
     */
    private AppUser findOrCreateBaseUser(Long chatId) {
        String username = TELEGRAM_PREFIX + chatId;

        return userRepo.findByUsername(username).orElseGet(() -> {
            AppUser newUser = AppUser.builder()
                    .username(username)
                    .role(Role.GUEST)
                    .isBlocked(true)
                    .build();
            return userRepo.save(newUser);
        });
    }

    /**
     * Создаёт связь между пользователем и Telegram chatId (если её ещё нет).
     */
    private void linkUserInternal(AppUser user, Long chatId) {
        repo.findByUser_Id(user.getId())
                .ifPresentOrElse(
                        existing -> {},
                        () -> repo.save(UserExternalIds.builder()
                                .user(user)
                                .telegramChatId(chatId)
                                .build()));
    }

    @Transactional
    @Override
    public void linkUser(AppUser user, String chatId) {
        Long chatIdLong = Long.parseLong(chatId);
        UserExternalIds link = repo.findByUser_Id(user.getId())
                .orElse(UserExternalIds.builder().user(user).build());
        link.setTelegramChatId(chatIdLong);
        repo.save(link);
    }
}
