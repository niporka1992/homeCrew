
CREATE TABLE task_comment
(
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at  TIMESTAMP DEFAULT NOW() NOT NULL,

    history_id  BIGINT      NOT NULL REFERENCES task_history (id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,

    text        VARCHAR(2000) NOT NULL
);

CREATE INDEX idx_task_comment_history_id ON task_comment (history_id);
CREATE INDEX idx_task_comment_user_id ON task_comment (user_id);
CREATE INDEX idx_task_comment_created_at ON task_comment (created_at);

ALTER TABLE task_history
DROP COLUMN IF EXISTS comment;

ALTER TABLE task_history
    ADD CONSTRAINT fk_task_history_task
        FOREIGN KEY (task_id) REFERENCES task (id) ON DELETE CASCADE;

ALTER TABLE task_history
    ADD CONSTRAINT fk_task_history_user
        FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE SET NULL;
