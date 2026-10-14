CREATE TABLE event_outcomes
(
    id         UUID PRIMARY KEY,
    event_id   VARCHAR(255) NOT NULL UNIQUE,
    event_name VARCHAR(255) NOT NULL,
    winner_id  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    version    BIGINT NOT NULL DEFAULT 0
);
