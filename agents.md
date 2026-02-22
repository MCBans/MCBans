# MCBans — Agents & Architecture Guide

## Project Overview

MCBans is a global banning solution for Minecraft, implemented as a Bukkit/Spigot server plugin. It connects to the MCBans API server via a persistent TCP connection to enforce bans across multiple Minecraft servers using a reputation-based system. Server administrators can issue local or global bans; global bans affect all servers running the plugin.

- **Plugin version:** 5.1.1
- **Bukkit API target:** 1.18 (compiled against 1.15.2-R0.1-SNAPSHOT)
- **Java version:** 8
- **Build system:** Gradle
- **Authors:** Firestar, Syamn, Draco/Corpdraco

---

## Repository Structure

```
MCBans/
├── build.gradle                  # Gradle build configuration
├── version.txt                   # Current version string
├── src/
│   └── main/
│       ├── java/com/mcbans/
│       │   ├── banlist/          # Offline ban list persistence
│       │   ├── client/           # TCP client & connection pool for MCBans API
│       │   ├── domain/models/    # Data models (Ban, Player, Server, Plugin)
│       │   ├── plugin/           # Core Bukkit plugin code
│       │   │   ├── actions/      # Background action processors
│       │   │   ├── api/          # Public API surface for other plugins
│       │   │   ├── bukkitListeners/ # Bukkit event handlers
│       │   │   ├── callBacks/    # Periodic async callback threads
│       │   │   ├── commands/     # In-game command implementations
│       │   │   ├── events/       # Custom Bukkit events
│       │   │   ├── exception/    # Custom exceptions
│       │   │   ├── org/json/     # Bundled JSON library
│       │   │   ├── permission/   # Permission system abstraction
│       │   │   ├── request/      # API request objects
│       │   │   ├── rollback/     # Block-logger integration
│       │   │   └── util/         # Plugin utility classes
│       │   ├── test/             # Standalone integration test runners
│       │   └── utils/            # Generic utilities (IO, encryption, IP, time)
│       └── resources/
│           ├── config.yml        # Default configuration
│           ├── plugin.yml        # Plugin descriptor (commands & permissions)
│           └── languages/        # i18n message files
├── libs/                         # Local dependency JARs (CoreProtect, HawkEye, LogBlock, PEX)
├── Dockerfile.main               # Docker image: API connection test
├── Dockerfile.bantest            # Docker image: full ban/unban cycle test
├── kubernetes-jobs.yaml          # Kubernetes CronJob definitions
└── README.md
```

---

## Core Components

### 1. `MCBans` (Plugin Entry Point)
**File:** `src/main/java/com/mcbans/plugin/MCBans.java`

The main `JavaPlugin` class. On enable it:
1. Loads the offline ban list from disk.
2. Reads `sync.ini` to restore the last synced ban ID.
3. Initializes `ConfigurationManager` and i18n.
4. Registers the `PlayerListener` Bukkit event handler.
5. Sets up the permission handler (`Perms`).
6. Registers all commands via `MCBansCommandHandler`.
7. Starts three background threads: `MainCallBack`, `BanSync`, `PendingActions`.
8. Runs `ServerChoose` to select the optimal API endpoint.
9. Sets up `RollbackHandler` for block-logger integration.

Key state held on the instance:
- `playerCache` — cached `BanResponse` objects for players currently joining.
- `offlineBanList` — local copy of bans used when the API is unreachable.
- `lastID` / `lastSync` — cursor for incremental ban sync.
- `syncRunning` — mutex flag to prevent concurrent syncs.
- `encryptAPI` — global flag for encrypted API communication.

---

### 2. TCP Client Layer
**Package:** `com.mcbans.client`

MCBans communicates with its API server over a persistent binary TCP protocol.

| Class | Role |
|---|---|
| `Client` | Base TCP socket. Handles session registration, optional RSA encryption handshake, and raw command I/O. |
| `ConnectionPool` | Thread-safe pool of up to 10 `Client` connections. Releases connections with a keep-alive timer (ping every 30 s). |
| `BanClient` | Sends ban requests (local, global, temp). |
| `UnbanClient` | Sends unban requests. |
| `BanIpClient` | Sends IP ban requests. |
| `UnBanIpClient` | Sends IP unban requests. |
| `BanStatusClient` | Queries ban status by player UUID or name. Returns a `BanResponse`. |
| `BanSyncClient` | Downloads incremental ban list from the API. |
| `BanLookupClient` | Looks up a specific ban by numeric ID. |
| `PlayerLookupClient` | Looks up player ban history. |
| `InformationCallbackClient` | Sends server state (player count, versions, online-mode) to the MCBans master. |
| `PendingActionSyncClient` | Long-polls the API for pending server-side actions (e.g., remote unbans). |

**API endpoint:** `api.v4.direct.mcbans.com:8082`
**Override (for testing):** set environment variables `OVERRIDE_API` and `OVERRIDE_PORT`.

**Encryption:** Optional RSA key exchange using a hardcoded MCBans server public key. Enabled via `encryption: true` in `config.yml` or the `encryptAPI` static flag.

**Authentication:** Each `Client` sends the server's `apiKey` and a static `"secret"` string during session registration.

---

### 3. Background Threads / Callbacks

#### `MainCallBack`
**File:** `src/main/java/com/mcbans/plugin/callBacks/MainCallBack.java`

Runs on a configurable timer (minimum 15 minutes, default 15). Calls `InformationCallbackClient.updateState()` to report the server's current player list, version strings, and online-mode status to the MCBans master server.

#### `BanSync`
**File:** `src/main/java/com/mcbans/plugin/callBacks/BanSync.java`

Runs on a configurable timer (minimum 30 seconds, configurable via `autoSyncInterval`). Performs two tasks:
1. **Upload unsynced offline bans** — any bans/unbans that were recorded while the API was unreachable are replayed.
2. **Download incremental bans** — fetches all bans from the API with ID > `lastID`, populates the `OfflineBanList`, and persists the new `lastID` to `sync.ini`.

Also provides `downloadBannedPlayersJSON()` to export all bans to a `banned-players.json` file.

#### `PendingActions`
**File:** `src/main/java/com/mcbans/plugin/actions/PendingActions.java`

Runs every 30 seconds. Opens a connection to the API and long-polls for pending commands (e.g., `UnbanSync`). Dispatches received commands via `ClientMCBansCommands` enum.

#### `ServerChoose`
**File:** `src/main/java/com/mcbans/plugin/callBacks/ServerChoose.java`

Runs once at startup to select the best API server endpoint.

---

### 4. Player Event Handling
**File:** `src/main/java/com/mcbans/plugin/bukkitListeners/PlayerListener.java`

Handles three Bukkit events:

**`AsyncPlayerPreLoginEvent` (HIGHEST priority)**
- Calls `BanStatusClient.banStatusByPlayerUUID()` to check the connecting player's ban status.
- On ban: denies login with a formatted message.
- On low reputation (below `minRep`): denies login.
- On success: stores the `BanResponse` in `plugin.playerCache`.
- On API timeout/error: falls back to the local `OfflineBanList`.

**`PlayerJoinEvent` (HIGHEST priority)**
- Retrieves the cached `BanResponse` and notifies staff of:
  - Previous ban history (requires `mcbans.view.bans` permission).
  - MCBans staff status (requires `mcbans.view.staff`).
- For offline-mode (BungeeCord) servers, runs a deferred connection data lookup via `HandleConnectionData`.

**`PlayerQuitEvent`**
- Removes the player from `plugin.mcbStaff` if applicable.

---

### 5. Offline Ban List
**Package:** `com.mcbans.banlist`

| Class | Role |
|---|---|
| `OfflineBanList` | Serialized map of player UUID → `BannedPlayer`. Used when the API is unreachable and as a local mirror of the global ban list. |
| `BannedPlayer` | Stores ban ID, type (local/global/temp), player name/UUID, reason, admin name/UUID, date, and expiry. |

The list is persisted as a serialized file in the plugin data folder and rebuilt on each full sync (`lastID == -1`).

---

### 6. Commands
**Package:** `com.mcbans.plugin.commands`

All commands extend `BaseCommand` and are registered via `MCBansCommandHandler`.

| Command | Class | Permission |
|---|---|---|
| `/ban` | `CommandBan` | `mcbans.ban.local` / `mcbans.ban.global` |
| `/globalban`, `/gban` | `CommandGlobalban` | `mcbans.ban.global` |
| `/tempban`, `/tban` | `CommandTempban` | `mcbans.ban.temp` |
| `/rban` | `CommandRban` | `mcbans.ban.rollback` |
| `/banip`, `/ipban` | `CommandBanip` | `mcbans.ban.ip` |
| `/unban` | `CommandUnban` | `mcbans.unban` |
| `/kick` | `CommandKick` | `mcbans.kick` |
| `/lookup`, `/lup` | `CommandLookup` | `mcbans.lookup.player` |
| `/banlookup`, `/blup` | `CommandBanlookup` | `mcbans.lookup.ban` |
| `/altlookup`, `/alup`, `/alt` | `CommandAltlookup` | `mcbans.lookup.alt` |
| `/namelookup`, `/nlup` | `CommandPrevious` | — |
| `/mcbans` | `CommandMCBans` | — |
| `/mcbs` | `CommandMCBansSettings` | `mcbans.admin` |

---

### 7. Public Plugin API
**File:** `src/main/java/com/mcbans/plugin/api/MCBansAPI.java`

Other Bukkit plugins can obtain an `MCBansAPI` handle via `plugin.getAPI(yourPlugin)` and call:

```java
api.localBan(targetName, targetUUID, senderName, senderUUID, reason);
api.globalBan(targetName, targetUUID, senderName, senderUUID, reason);
api.tempBan(targetName, targetUUID, senderName, senderUUID, reason, duration, measure);
api.unBan(targetName, targetUUID, senderName, senderUUID);
api.ipBan(ip, senderName, senderUUID, reason, callback);
api.kick(targetName, targetUUID, senderName, senderUUID, reason);
api.lookupPlayer(targetName, targetUUID, senderName, senderUUID, callback);
api.lookupBan(banID, callback);
api.lookupAlt(playerName, callback);
```

Each API instance is registered per calling plugin (singleton per `Plugin` instance). All ban/unban calls are dispatched via the `Ban` / `Kick` request classes, which communicate with the MCBans API asynchronously.

---

### 8. Permission System
**Package:** `com.mcbans.plugin.permission`

`PermissionHandler` abstracts over four backends:

| Backend | Config value |
|---|---|
| Bukkit SuperPerms (default) | `SuperPerms` |
| Vault | `Vault` |
| PermissionsEx | `PEX` or `PermissionsEx` |
| Ops-only | `OPs` |

`Perms` enum maps permission nodes to friendly names and provides `.has(sender)`, `.message(text)`, and `.getPlayers()` helpers.

---

### 9. Rollback Integration
**Package:** `com.mcbans.plugin.rollback`

`RollbackHandler` auto-detects and wraps one of three block-logging plugins in priority order:
1. **LogBlock** → `LbRollback`
2. **HawkEye** → `HeRollback`
3. **CoreProtect** → `CpRollback`

Used by `/rban` to roll back a player's block changes before applying the ban.

---

### 10. Configuration
**File:** `src/main/resources/config.yml` | Class: `ConfigurationManager`

| Key | Default | Description |
|---|---|---|
| `apiKey` | (required) | Server API key from the MCBans dashboard |
| `encryption` | `false` | Encrypt API TCP communications |
| `language` | `default` | i18n language file |
| `permission` | `SuperPerms` | Permission backend |
| `minRep` | `3` | Minimum player reputation; lower = denied entry |
| `enableMaxAlts` | `false` | Block players with too many alt accounts |
| `maxAlts` | `2` | Maximum allowed alt accounts |
| `enableAutoSync` | `true` | Sync bans from the API periodically |
| `autoSyncInterval` | `30` | Sync interval in seconds |
| `callBackInterval` | `15` | Master callback interval in minutes (min 15) |
| `timeout` | `10` | API connection timeout in seconds |
| `failsafe` | `false` | Deny all logins if the API is unreachable |
| `isDebug` | `false` | Verbose debug logging |
| `logEnable` | `false` | Write actions to log file |
| `backDaysAgo` | `20` | Days of history for block rollbacks |

---

## External Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| Bukkit/Spigot API | 1.15.2-R0.1-SNAPSHOT | Minecraft server plugin API |
| LogBlock | 1.16.5.2-SNAPSHOT | Block logging for rollbacks |
| VaultAPI | 1.7 | Permission abstraction |
| Log4j | 2.17.1 | Logging |
| CoreProtect | 2.0.9 (local JAR) | Block logging for rollbacks |
| HawkEye | (local JAR) | Block logging for rollbacks |
| PermissionsEx | (local JAR) | Alternative permission system |

---

## Integration Testing (Docker / Kubernetes)

### Standalone Test Classes
**Package:** `com.mcbans.test`

| Class | Purpose |
|---|---|
| `Main` | Connects to the MCBans API, verifies the connection, and disconnects. |
| `BanTest` | Full 8-step cycle: connect → verify → ban player → check ban status → simulate login attempt → unban → verify unban → disconnect. |

Both read configuration from environment variables:
- `MCBANS_API_KEY` — API key for the test server
- `MCBANS_PLAYER_TO_BAN` — player name to use in ban tests
- `OVERRIDE_API` / `OVERRIDE_PORT` — override the API endpoint

### Docker Images
| Image | Dockerfile | Runs |
|---|---|---|
| `ghcr.io/mcbans/mcbans-main` | `Dockerfile.main` | `com.mcbans.test.Main` |
| `ghcr.io/mcbans/mcbans-bantest` | `Dockerfile.bantest` | `com.mcbans.test.BanTest` |

Both images build with Gradle and use offline mode on subsequent runs.

### Kubernetes CronJobs
Defined in `kubernetes-jobs.yaml`. Two CronJobs run every 5 minutes in the `mcbans` namespace:
- `mcbans-main-cronjob` — API connectivity check
- `mcbans-bantest-cronjob` — Full ban/unban cycle test

Apply with:
```bash
kubectl apply -f kubernetes-jobs.yaml
kubectl get cronjobs -n mcbans
kubectl get jobs -n mcbans
```

---

## Data Flow: Player Login

```
Player connects
    └─► AsyncPlayerPreLoginEvent (PlayerListener)
            └─► ConnectionPool.getConnection(apiKey)
                    └─► BanStatusClient.banStatusByPlayerUUID(uuid, ip, enforce=true)
                            ├─► BanResponse.getBan() != null  →  DENY (banned)
                            ├─► BanResponse.getReputation() < minRep  →  DENY (low rep)
                            └─► OK  →  cache BanResponse in playerCache
    └─► PlayerJoinEvent (PlayerListener)
            └─► pop playerCache for player
                    ├─► notify staff of previous bans (mcbans.view.bans)
                    └─► notify staff of MCBans staff presence (mcbans.view.staff)
```

## Data Flow: Ban Command

```
/ban <player> <reason>
    └─► CommandBan.run()
            └─► Ban(plugin, "local", ...).run()
                    └─► ConnectionPool.getConnection(apiKey)
                            └─► BanClient.localBan(...)
                                    └─► ServerMCBansCommands.LocalBan  →  API server
                                            ├─► ack  →  kick player from server
                                            └─► err  →  log error / queue in OfflineBanList
```

## Data Flow: Ban Sync

```
BanSync timer fires
    └─► Upload unsynced offline bans to API (BanClient / UnbanClient)
    └─► BanSyncClient.getBanSync(lastID)
            └─► receive batches of Ban objects
                    └─► populate OfflineBanList
                    └─► update lastID
    └─► syncSave()  →  write lastID to sync.ini
    └─► OfflineBanList.save()  →  persist to disk
```

---

## i18n

Language files live in `src/main/resources/languages/`. The `I18n` class loads the selected language at startup using `config.getLanguage()`. Available languages:

`default`, `dutch`, `french`, `german`, `ja-jp`, `norwegian`, `portuguese`, `spanish`, `sv-se`, `zh_TW`

---

## Common Development Patterns

**Adding a new command:**
1. Create a class in `com.mcbans.plugin.commands` extending `BaseCommand`.
2. Set `this.name` to the command name (must match `plugin.yml`).
3. Override `run()` for execution logic and `permission()` for the permission check.
4. Register it in `MCBans.registerCommands()`.
5. Add the command and its permission node to `plugin.yml`.

**Adding a new API client operation:**
1. Create a subclass of `Client` (or use the cast pattern: `class FooClient extends Client { public static FooClient cast(Client c) {...} }`).
2. Use `sendCommand(ServerMCBansCommands.X)` / `getCommand()` / `ReadFromInputStream` / `WriteToOutputStream` for the binary protocol.
3. Obtain a connection via `ConnectionPool.getConnection(apiKey)` and release it via `ConnectionPool.release(client)`.

**Accessing the plugin API from another plugin:**
```java
MCBans mcbans = (MCBans) Bukkit.getPluginManager().getPlugin("MCBans");
MCBansAPI api = mcbans.getAPI(this); // 'this' is your Plugin instance
api.localBan(targetName, targetUUID, senderName, senderUUID, reason);
```
