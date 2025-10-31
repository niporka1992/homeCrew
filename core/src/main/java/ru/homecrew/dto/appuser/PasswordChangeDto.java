package ru.homecrew.dto.appuser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeDto(
        String username,
        @NotBlank(message = "Новый пароль обязателен")
                @Size(min = 8, max = 64, message = "Пароль должен быть от 8 до 64 символов")
                @Pattern(
                        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&()№]).+$",
                        message = "Пароль должен содержать заглавную, строчную буквы, цифру и спецсимвол")
                String newPassword) {}
