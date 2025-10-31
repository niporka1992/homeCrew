package ru.homecrew.dto.appuser;

public record AppUserCreateDto(
        String username, String fullName, String role, String phone, String email, String password) {}
