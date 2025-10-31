package ru.homecrew.service.userappservice;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.dto.appuser.AppUserCreateDto;
import ru.homecrew.dto.appuser.AppUserDto;
import ru.homecrew.dto.appuser.PasswordChangeDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;
import ru.homecrew.mapper.AppUserMapper;
import ru.homecrew.repository.AppUserRepository;

/**
 * Реализация сервиса управления пользователями.
 * Предоставляет CRUD-операции и базовую логику управления активностью учётных записей.
 */
@Service
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {

    private static final String USER_NOT_FOUND_MSG = "User not found: ";

    private final AppUserRepository appUserRepository;
    private final AppUserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppUser create(AppUserCreateDto dto) {
        AppUser user = mapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        return appUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MSG + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> getAll(Role role) {
        if (role == null) {
            return appUserRepository.findAll();
        }
        return appUserRepository.findAllByRole(role);
    }

    @Override
    @Transactional
    public AppUser updateByUserName(String username, AppUserDto dto) {
        AppUser existing = appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MSG + username));

        mapper.updateEntityFromDto(dto, existing);
        return appUserRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteByName(String username) {
        AppUser user = appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MSG + username));
        appUserRepository.delete(user);
    }

    @Override
    @Transactional
    public void setIsBlocked(String username, boolean isBlocked) {
        AppUser user = appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MSG + username));
        user.setBlocked(isBlocked);
        appUserRepository.save(user);
    }

    @Transactional
    @Override
    public void changePassword(PasswordChangeDto dto) {
        String username = dto.username();
        AppUser user = appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MSG + username));

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        appUserRepository.save(user);
    }
}
