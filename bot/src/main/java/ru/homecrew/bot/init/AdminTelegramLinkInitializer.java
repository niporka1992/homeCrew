package ru.homecrew.bot.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.UserExternalIds;
import ru.homecrew.repository.UserExternalIdsRepository;
import ru.homecrew.service.link.ExternalLinkUserService;
import ru.homecrew.service.userappservice.UserAppService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTelegramLinkInitializer {

    private final UserAppService userAppService;
    private final ExternalLinkUserService linkService;
    private final UserExternalIdsRepository linkRepo;

    @Value("${telegram.admin.chatId}")
    private Long mainAdminId;

    @PostConstruct
    public void linkAdminToTelegram() {
        AppUser admin = userAppService.getByUsername("admin");

        log.info(" Проверяем связь администратора с Telegram ID {}", mainAdminId);

        boolean linkExists = linkRepo.findByTelegramChatId(mainAdminId)
                .map(UserExternalIds::getUser)
                .filter(user -> user.getId().equals(admin.getId()))
                .isPresent();

        if (linkExists) {
            log.info(
                    "ℹ Связь администратора '{}' с chatId={} уже существует, пропускаем.",
                    admin.getUsername(),
                    mainAdminId);
            return;
        }

        linkService.linkUser(admin, String.valueOf(mainAdminId));
        log.info(" Администратор '{}' теперь связан с Telegram chatId={}", admin.getUsername(), mainAdminId);
    }
}
