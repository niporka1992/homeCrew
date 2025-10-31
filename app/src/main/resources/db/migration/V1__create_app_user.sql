CREATE TABLE app_user
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(200),
    full_name     VARCHAR(200),
    role          VARCHAR(20) NOT NULL,
    phone         VARCHAR(30),
    email         VARCHAR(200),
    is_Blocked        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_user_role CHECK (role IN ('OWNER', 'WORKER','GUEST'))
);
