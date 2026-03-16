CREATE TABLE guilds (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    mod_log_channel BIGINT,
    joined_at       TIMESTAMP NOT NULL DEFAULT NOW()
);