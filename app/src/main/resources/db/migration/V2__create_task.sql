
CREATE TABLE task
(
    id          BIGSERIAL PRIMARY KEY,
    description VARCHAR(1000),
    type        VARCHAR(20)              NOT NULL,
    status      VARCHAR(20)              NOT NULL,
    assignee_id BIGINT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES app_user (id),
    CONSTRAINT chk_task_type CHECK (type IN ('SIMPLE', 'CRON')),
    CONSTRAINT chk_task_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'DONE', 'EXPIRED'))
);
