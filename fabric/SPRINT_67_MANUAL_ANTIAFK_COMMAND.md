# Sprint 67 — manual validation — `/antiafk logs` (Fabric)

**Build:** `projectVersion` **2.3.95** (see `gradle.properties`).

## Command roots to exercise

- `/auraskills antiafk logs <player> [page] [perPage]`
- `/skills antiafk logs …` — same for `/sk`, `/skill`

**Intentional:** no top-level `/antiafk` (Bukkit only registers under `%skills_alias%` + `/auraskills` mirror on Fabric).

## Permission

- `auraskills.command.antiafk.logs` — reference Bukkit `plugin.yml` default **op**; Fabric `FabricCommandRequirements` uses default **deny** (match other staff tools; grant via LuckPerms / fabric-perms).

## Dedicated server (Java) — expected behavior

1. **Staff with node:** `antiafk logs <OtherPlayer>` prints a header (count, page) + log lines; timestamps use `yyyy-MM-dd_HH:mm:ss` in the server’s default time zone (same as Bukkit).
2. **Defaults:** page **1**, per-page **10**; optional `page` and `perPage` integers, **min 1** each.
3. **In-game sender:** must **not** use own name (other-only, like Bukkit ACF `@Flags("other")`) — error key `auraskills.granite.command.antiafk.other_target_only`.
4. **Console / RCON:** may query any name that resolves via **online match** or **`NameToIdCache`** (same path as `/profile`); unknown name → `auraskills.granite.command.profile.player_not_found`.
5. **Online target, empty in-memory cache:** first load runs **async** `loadAntiAfkLogs` then **sync** chat.
6. **Offline target:** async load from storage, sync chat.

## Relation to `AntiAfkManager`

- **Gameplay** still creates **`AntiAfkLog`** entries via common **`AntiAfkManager`** (session list on **`User`**).
- This **command** only **reads** `StorageProvider#loadAntiAfkLogs` and merges with **`User#getSessionAntiAfkLogs`** for display. It does **not** change Anti-AFK checks, exempt state, or detection rules.

## Bedrock / Geyser

- **Not** separately tested this sprint. Output is a plain multi-line `String` in chat; if anything wraps oddly, file an issue. **No** special Bedrock code path in this command.

## Pass / fail checklist

- [ ] Permission denied without `auraskills.command.antiafk.logs`
- [ ] In-game: self-name rejected
- [ ] Console: can query another player by name
- [ ] Paging: second page with `… logs <name> 2 5` (or similar)
- [ ] Storage I/O error surfaces `auraskills.granite.command.antiafk.load_failed` and log line (mock/hard to force without breaking DB)

## Risks / caveats

- **Name resolution ≠ Bukkit `hasPlayedBefore`:** if a player never joined this server, they may be **unresolvable** on Fabric even when Bukkit would list them. Documented as **Partial** parity.
- **User mutation from async (online empty-cache path):** matches Bukkit: `setStoredAntiAfkLogs` after `loadAntiAfkLogs` on the async worker (not a gameplay anti-cheat change).

## Automated test

- `.\gradlew -PfabricOnly=true :fabric:test` (includes `FabricCommandPermissionsTest` for `ANTIAFK_LOGS`).
