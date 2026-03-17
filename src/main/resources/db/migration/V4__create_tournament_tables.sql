CREATE TABLE tournaments (
    id              BIGSERIAL PRIMARY KEY,
    guild_id        BIGINT NOT NULL REFERENCES guilds(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    game            VARCHAR(100) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'REGISTRATION',
    max_teams       INT NOT NULL,
    created_by      BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tournament_teams (
    id              BIGSERIAL PRIMARY KEY,
    tournament_id   BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    captain_id      BIGINT NOT NULL,
    registered_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tournament_matches (
    id              BIGSERIAL PRIMARY KEY,
    tournament_id   BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    round           INT NOT NULL,
    team_one_id     BIGINT REFERENCES tournament_teams(id),
    team_two_id     BIGINT REFERENCES tournament_teams(id),
    winner_id       BIGINT REFERENCES tournament_teams(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_at    TIMESTAMP
);

CREATE INDEX idx_tournaments_guild ON tournaments(guild_id);
CREATE INDEX idx_matches_tournament ON tournament_matches(tournament_id);