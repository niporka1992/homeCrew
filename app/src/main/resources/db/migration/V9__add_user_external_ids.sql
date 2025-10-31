CREATE TABLE user_external_ids
(
    id               BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP          DEFAULT NOW(),

    user_id          BIGINT    NOT NULL UNIQUE
        REFERENCES app_user (id)
            ON DELETE CASCADE,

    telegram_chat_id BIGINT
);

COMMENT
ON TABLE user_external_ids IS 'Связи пользователей с внешними сервисами (Telegram и др.)';
COMMENT
ON COLUMN user_external_ids.user_id IS 'Пользователь, к которому относится внешний идентификатор';
COMMENT
ON COLUMN user_external_ids.telegram_chat_id IS 'Telegram chatId';
