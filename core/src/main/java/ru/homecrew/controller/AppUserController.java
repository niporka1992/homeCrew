package ru.homecrew.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.homecrew.dto.appuser.AppUserCreateDto;
import ru.homecrew.dto.appuser.AppUserDto;
import ru.homecrew.dto.appuser.PasswordChangeDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.Role;
import ru.homecrew.mapper.AppUserMapper;
import ru.homecrew.service.userappservice.UserAppService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_OWNER')")
public class AppUserController {

    private final UserAppService userService;
    private final AppUserMapper mapper;

    @GetMapping
    public ResponseEntity<List<AppUserDto>> getAllUsers(@RequestParam(name = "role", required = false) Role role) {
        return ResponseEntity.ok(
                userService.getAll(role).stream().map(mapper::toDto).toList());
    }

    @GetMapping("/{username}")
    public ResponseEntity<AppUserDto> getUserByUsername(@PathVariable(name = "username") String username) {
        AppUser user = userService.getByUsername(username);
        return ResponseEntity.ok(mapper.toDto(user));
    }

    @PostMapping
    public ResponseEntity<AppUserDto> createUser(@RequestBody AppUserCreateDto dto) {
        AppUser created = userService.create(dto);
        return ResponseEntity.ok(mapper.toDto(created));
    }

    @PutMapping("/{username}")
    public ResponseEntity<AppUserDto> updateUser(
            @PathVariable("username") String username, @RequestBody AppUserDto dto) {
        AppUser updated = userService.updateByUserName(username, dto);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "username") String username) {
        userService.deleteByName(username);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("username") String username, @RequestParam("isBlocked") boolean isBlocked) {
        userService.setIsBlocked(username, isBlocked);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeDto dto) {
        userService.changePassword(dto);
        return ResponseEntity.ok().build();
    }
}
