package ru.homecrew.dto.appuser;

import ru.homecrew.enums.Role;

public record AppUserDto(String username, String fullName, Role role, String phone, String email, Boolean blocked) {}
