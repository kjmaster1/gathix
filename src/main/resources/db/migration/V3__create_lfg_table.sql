CREATE TABLE lfg_posts (
    id          BIGSERIAL PRIMARY KEY,
    guild_id    BIGINT NOT NULL REFERENCES guilds(id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL,
    game        VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    players_needed INT NOT NULL DEFAULT 1,
    players_joined INT NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP NOT NULL
);

CREATE INDEX idx_lfg_guild_status ON lfg_posts(guild_id, status);