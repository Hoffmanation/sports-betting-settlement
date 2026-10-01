CREATE TABLE bets
(
    id                 UUID           PRIMARY KEY,
    user_id            VARCHAR(255)   NOT NULL,
    event_id           VARCHAR(255)   NOT NULL,
    event_market_id    VARCHAR(255)   NOT NULL,
    selected_winner_id VARCHAR(255)   NOT NULL,
    amount             DECIMAL(19, 4) NOT NULL,
    status             VARCHAR(16)    NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    settled_at         TIMESTAMP WITH TIME ZONE,
    version            BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_bets_event_status_created ON bets (event_id, status, created_at, id);
