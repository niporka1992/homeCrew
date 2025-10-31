
CREATE TABLE task_attachment
(
    id         BIGSERIAL PRIMARY KEY,
    history_id BIGINT                   NOT NULL,
    file_url   VARCHAR(500)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_attachment_history FOREIGN KEY (history_id) REFERENCES task_history (id)
);