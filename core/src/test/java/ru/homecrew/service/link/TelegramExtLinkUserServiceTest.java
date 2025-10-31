package ru.homecrew.service.link;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.UserExternalIds;
import ru.homecrew.enums.Role;
import ru.homecrew.repository.AppUserRepository;
import ru.homecrew.repository.UserExternalIdsRepository;

/**
 * Юнит-тесты для {@link TelegramExtLinkUserService}.
 */
class TelegramExtLinkUserServiceTest {

    @Mock
    private UserExternalIdsRepository externalRepo;

    @Mock
    private AppUserRepository userRepo;

    @InjectMocks
    private TelegramExtLinkUserService service;

    private AppUser user;
    private UserExternalIds link;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = AppUser.builder()
                .username("tg_123")
                .role(Role.GUEST)
                .isBlocked(true)
                .build();

        link = UserExternalIds.builder().telegramChatId(123L).user(user).build();
    }

    @Test
    @DisplayName("findOrCreateByExternalId(): возвращает пользователя, если связь уже существует")
    void findOrCreate_existingLink_returnsUser() {
        when(externalRepo.findByTelegramChatId(123L)).thenReturn(Optional.of(link));

        AppUser result = service.findOrCreateByExternalId("123");

        assertEquals(user, result);
        verify(externalRepo).findByTelegramChatId(123L);
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    @DisplayName("findOrCreateByExternalId(): создаёт нового пользователя и связь, если не найден")
    void findOrCreate_createsNewUserAndLink() {
        when(externalRepo.findByTelegramChatId(123L)).thenReturn(Optional.empty());
        when(userRepo.findByUsername("tg_123")).thenReturn(Optional.empty());
        when(userRepo.save(any(AppUser.class))).thenAnswer(inv -> inv.<AppUser>getArgument(0));
        when(externalRepo.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(externalRepo.save(any(UserExternalIds.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = service.findOrCreateByExternalId("123");

        assertNotNull(result);
        assertEquals("tg_123", result.getUsername());
        assertTrue(result.isBlocked());
        assertEquals(Role.GUEST, result.getRole());

        verify(userRepo).save(any(AppUser.class));
        verify(externalRepo).save(any(UserExternalIds.class));
    }

    @Test
    @DisplayName("findOrCreateByExternalId(): использует существующего AppUser при совпадении username")
    void findOrCreate_existingUser_usesIt() {
        when(externalRepo.findByTelegramChatId(123L)).thenReturn(Optional.empty());
        when(userRepo.findByUsername("tg_123")).thenReturn(Optional.of(user));
        when(externalRepo.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(externalRepo.save(any(UserExternalIds.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = service.findOrCreateByExternalId("123");

        assertSame(user, result);
        verify(userRepo, never()).save(any());
        verify(externalRepo).save(any(UserExternalIds.class));
    }

    @Test
    @DisplayName("linkUser(): обновляет telegramChatId, если связь уже существует")
    void linkUser_updatesExistingLink() {
        when(externalRepo.findByUser_Id(null)).thenReturn(Optional.of(link));

        service.linkUser(user, "999");

        assertEquals(999L, link.getTelegramChatId());
        verify(externalRepo).save(link);
    }

    @Test
    @DisplayName("linkUser(): создаёт новую связь, если не найдено существующей")
    void linkUser_createsNewLink() {
        when(externalRepo.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(externalRepo.save(any(UserExternalIds.class))).thenAnswer(inv -> inv.getArgument(0));

        service.linkUser(user, "555");

        verify(externalRepo).save(any(UserExternalIds.class));
    }
}
