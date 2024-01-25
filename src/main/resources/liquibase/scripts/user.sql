-- liquibase formatted sql

-- changeset ivan:1
CREATE TABLE notification_task (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGSERIAL NOT NULL,
    notification_text TEXT NOT NULL,
    time TIMESTAMP NOT NULL
);