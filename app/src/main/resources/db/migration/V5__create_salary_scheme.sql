
CREATE TABLE salary_scheme
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL,
    amount     NUMERIC                  NOT NULL,
    currency   VARCHAR(3)               NOT NULL,
    type       VARCHAR(20)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_salary_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT chk_salary_type CHECK (type IN ('FIXED', 'PER_TASK', 'PER_HOUR')),
    CONSTRAINT chk_salary_currency CHECK (currency IN ('RUB', 'USD', 'EUR')) -- добавить другие валюты по необходимости
);