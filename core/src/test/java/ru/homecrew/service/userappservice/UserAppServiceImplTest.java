package ru.homecrew.service.userappservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.homecrew.dto.appuser.AppUserCreateDto;
import ru.homecrew.dto.appuser.AppUserDto;
import ru.homecrew.dto.appuser.PasswordChangeDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;
import ru.homecrew.mapper.AppUserMapper;
import ru.homecrew.repository.AppUserRepository;

@DisplayName("UserAppServiceImpl — управление пользователями системы")
class UserAppServiceImplTest {

    @Mock
    private AppUserRepository repository;

    @Mock
    private AppUserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAppServiceImpl service;

    private AppUser user;
    private AppUserCreateDto createDto;
    private AppUserDto updateDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = AppUser.builder()
                .username("boss77")
                .fullName("Никита Громов")
                .role(Role.OWNER)
                .passwordHash("encoded123")
                .isBlocked(false)
                .build();

        createDto = new AppUserCreateDto(
                "boss77", "Никита Громов", Role.OWNER.name(), "+79990001122", "boss@mail.ru", "SuperPass123");

        updateDto = new AppUserDto("boss77", "Никита Громов", Role.OWNER, "+79995556677", "new@mail.ru", false);
    }

    @Test
    @DisplayName("create(): создаёт нового пользователя с зашифрованным паролем")
    void create_success() {
        when(mapper.toEntity(createDto)).thenReturn(user);
        when(passwordEncoder.encode("SuperPass123")).thenReturn("hashed_pass");
        when(repository.save(user)).thenReturn(user);

        AppUser result = service.create(createDto);

        assertNotNull(result);
        assertEquals("hashed_pass", result.getPasswordHash());
        verify(repository).save(user);
    }

    @Test
    @DisplayName("getByUsername(): возвращает найденного пользователя")
    void getByUsername_success() {
        when(repository.findByUsername("boss77")).thenReturn(Optional.of(user));

        AppUser found = service.getByUsername("boss77");

        assertEquals(user, found);
    }

    @Test
    @DisplayName("getByUsername(): выбрасывает EntityNotFound, если пользователя нет")
    void getByUsername_notFound() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getByUsername("ghost"));
    }

    @Test
    @DisplayName("getAll(): возвращает всех пользователей, если роль не указана")
    void getAll_noRole() {
        when(repository.findAll()).thenReturn(List.of(user));

        List<AppUser> result = service.getAll(null);

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("getAll(): фильтрует пользователей по роли")
    void getAll_withRole() {
        when(repository.findAllByRole(Role.OWNER)).thenReturn(List.of(user));

        List<AppUser> result = service.getAll(Role.OWNER);

        assertEquals(1, result.size());
        verify(repository).findAllByRole(Role.OWNER);
    }

    @Test
    @DisplayName("updateByUserName(): обновляет данные существующего пользователя")
    void updateByUserName_success() {
        when(repository.findByUsername("boss77")).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        AppUser updated = service.updateByUserName("boss77", updateDto);

        assertNotNull(updated);
        verify(mapper).updateEntityFromDto(updateDto, user);
        verify(repository).save(user);
    }

    @Test
    @DisplayName("updateByUserName(): выбрасывает EntityNotFound, если пользователь не найден")
    void updateByUserName_notFound() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateByUserName("ghost", updateDto));
    }

    @Test
    @DisplayName("deleteByName(): удаляет пользователя, если найден")
    void deleteByName_success() {
        when(repository.findByUsername("boss77")).thenReturn(Optional.of(user));

        service.deleteByName("boss77");

        verify(repository).delete(user);
    }

    @Test
    @DisplayName("deleteByName(): выбрасывает EntityNotFound при отсутствии пользователя")
    void deleteByName_notFound() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteByName("ghost"));
    }

    @Test
    @DisplayName("setIsBlocked(): блокирует или разблокирует пользователя")
    void setIsBlocked_success() {
        when(repository.findByUsername("boss77")).thenReturn(Optional.of(user));

        service.setIsBlocked("boss77", true);

        assertTrue(user.isBlocked());
        verify(repository).save(user);
    }

    @Test
    @DisplayName("setIsBlocked(): выбрасывает EntityNotFound, если пользователь не найден")
    void setIsBlocked_notFound() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.setIsBlocked("ghost", true));
    }

    @Test
    @DisplayName("changePassword(): обновляет пароль и сохраняет пользователя")
    void changePassword_success() {
        PasswordChangeDto dto = new PasswordChangeDto("boss77", "NewPass555");
        when(repository.findByUsername("boss77")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass555")).thenReturn("encoded_pass");

        service.changePassword(dto);

        assertEquals("encoded_pass", user.getPasswordHash());
        verify(repository).save(user);
    }

    @Test
    @DisplayName("changePassword(): выбрасывает EntityNotFound, если пользователь не найден")
    void changePassword_notFound() {
        PasswordChangeDto dto = new PasswordChangeDto("unknown", "whatever");
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.changePassword(dto));
    }
}
