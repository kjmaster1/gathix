# Gathix

A free, open-source Discord bot built for gaming communities. Gathix provides server moderation, game stat lookups, looking-for-group management, and tournament organisation — all through Discord slash commands.

**[Add to your server](https://discord.com/oauth2/authorize?client_id=1483230389513490584&permissions=1102061857926&integration_type=0&scope=bot+applications.commands)** • **[top.gg listing](https://top.gg)** • **[Support server](https://discord.gg)**

---

## Features

### Moderation
Full moderation suite with persistent audit logging. Every action is recorded in the database with the moderator, target, reason, and timestamp.

| Command | Description |
|---|---|
| `/warn` | Issue a warning to a member |
| `/warnings` | View all warnings for a member |
| `/kick` | Kick a member from the server |
| `/ban` | Ban a member from the server |
| `/unban` | Unban a user by ID |
| `/timeout` | Timeout a member for a specified duration |
| `/untimeout` | Remove a timeout from a member |

### Game Stats
Look up player profiles from major game platforms directly in Discord.

| Command | Description |
|---|---|
| `/stats steam <username>` | Steam profile — status, games owned, total playtime, recently played |

### Looking for Group
Post LFG requests for any game and find teammates. Posts auto-expire after 2 hours.

| Command | Description |
|---|---|
| `/lfg post` | Post a new LFG request |
| `/lfg list` | View open LFG posts in the server |
| `/lfg join` | Join an LFG post by ID |
| `/lfg close` | Close your own LFG post |

### Tournaments
Run single-elimination tournaments with automatic bracket generation and result tracking.

| Command | Description | Permission required |
|---|---|---|
| `/tournament-admin create` | Create a new tournament | Manage Server |
| `/tournament-admin start` | Start a tournament and generate Round 1 | Manage Server |
| `/tournament-admin result` | Report a match result | Manage Server |
| `/tournament register` | Register your team | Everyone |
| `/tournament bracket` | View the current bracket | Everyone |
| `/tournament list` | List all tournaments in the server | Everyone |

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Core language |
| Spring Boot 3.5 | Application framework |
| JDA 5 | Java Discord API — event handling, slash commands |
| PostgreSQL | Persistent storage |
| Flyway | Database schema versioning |
| Spring WebFlux | Async HTTP client for game API calls |
| Docker | Containerisation |
| Render | Cloud hosting |

---

## Architecture

Gathix combines JDA's event-driven Discord integration with Spring Boot's dependency injection and data layer. JDA maintains a persistent WebSocket connection to Discord and fires events for slash commands. Spring services handle business logic and JPA repositories manage persistence.

```
com.gathix/
├── bot/                  # JDA setup, command registration, startup listener
├── commands/
│   ├── moderation/       # Slash command handlers for moderation
│   ├── stats/            # Slash command handlers for game stats
│   ├── lfg/              # Slash command handlers for LFG
│   └── tournament/       # Slash command handlers for tournaments
├── moderation/           # Warning and mod action domain + service
├── stats/                # Steam API integration
├── lfg/                  # LFG post domain + service with auto-expiry
├── tournament/           # Tournament, team, and match domain + service
└── guild/                # Per-server configuration
```

### Database Schema

```
guilds              — registered Discord servers
warnings            — issued warnings with moderator and reason
mod_actions         — full audit log of all moderation actions
lfg_posts           — LFG requests with expiry tracking
tournaments         — tournament metadata and status
tournament_teams    — registered teams per tournament
tournament_matches  — bracket matches with round and winner tracking
```

### Key Design Decisions

**Event-driven architecture** — JDA fires events for every Discord interaction. All `ListenerAdapter` beans are auto-discovered by Spring and registered with JDA at startup. Adding a new feature requires only creating a new `@Component` that extends `ListenerAdapter` — no manual registration.

**Slash commands split by permission level** — moderation commands use `DefaultMemberPermissions` to restrict access to members with appropriate Discord permissions. Tournament admin commands are split into a separate `/tournament-admin` command so players can register and view brackets without needing elevated permissions.

**Scheduled LFG expiry** — `LfgService` runs a scheduled task every 5 minutes that bulk-expires posts past their `expires_at` timestamp. This is more efficient than checking expiry on every read.

**Persistent audit trail** — every moderation action writes to both `warnings`/`mod_actions` tables. This gives server admins a complete, queryable history of all moderation activity.

---

## Running Locally

**Prerequisites:** Java 21, Maven, Docker

```bash
git clone https://github.com/kjmaster1/gathix
cd gathix
```

Create a `.env` file in the project root:

```
DISCORD_TOKEN=your_discord_bot_token
DB_PASSWORD=gathix123
JWT_SECRET=your_64_char_random_hex_string
STEAM_API_KEY=your_steam_api_key
```

Start the database:

```bash
docker compose up -d db
```

Add environment variables to your IntelliJ run configuration, then run `GathixApplication`. The bot will connect to Discord and Flyway will create all database tables automatically.

For slash command testing, add your test server ID to `application.properties`:

```properties
gathix.dev-guild-id=your_guild_id
```

This registers commands to your server instantly rather than waiting up to 1 hour for global propagation.

---

## Deployment

Gathix is deployed on [Render](https://render.com) using Docker. The `Dockerfile` in the project root produces a minimal JRE image.

**Required environment variables on Render:**

```
DISCORD_TOKEN
DB_PASSWORD
JWT_SECRET
STEAM_API_KEY
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
```

Flyway runs automatically on startup and applies any pending migrations.

---

## Adding Gathix to Your Server

1. Click the invite link above
2. Select your server
3. Grant the requested permissions
4. Type `/help` to see all available commands

Gathix requires the following permissions to function correctly: View Audit Log, Manage Roles, Kick Members, Ban Members, Manage Nicknames, View Channels, Moderate Members, Send Messages, Manage Messages, Embed Links, Read Message History, Use Slash Commands.

---

## Contributing

Gathix is open source and contributions are welcome. Open an issue to report a bug or suggest a feature, or submit a pull request directly.

Planned features:
- Riot Games API integration (League of Legends, Valorant ranks)
- Bungie API integration (Destiny 2 stats)
- Web dashboard for server configuration
- Per-server mod log channel configuration
