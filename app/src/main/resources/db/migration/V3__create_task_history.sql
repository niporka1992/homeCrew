-- ===========================================================
--  V7__create_task_history.sql (актуализировано)
--  История действий по задачам (без текстовых комментариев)
-- ===========================================================

CREATE TABLE task_history
(
    id           BIGSERIAL PRIMARY KEY,

    task_id      BIGINT NOT NULL,
    user_id      BIGINT,

    action_type  VARCHAR(50) NOT NULL DEFAULT 'CREATED',

    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status_after   VARCHAR(30),

    CONSTRAINT fk_history_task
        FOREIGN KEY (task_id) REFERENCES task (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_history_user
        FOREIGN KEY (user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

-- Индексы для ускорения выборок истории по задаче
CREATE INDEX idx_history_task_id ON task_history (task_id);
CREATE INDEX idx_history_user_id ON task_history (user_id);
CREATE INDEX idx_history_action_type ON task_history (action_type);
CREATE INDEX idx_history_created_at ON task_history (created_at);
