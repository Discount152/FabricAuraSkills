# Sprint 59 — manual validation: `/profile` (UserState inspection)

**Scope:** Fabric **Granite Skills** only. **Java** and **Bedrock (Geyser/Floodgate)** for **chat** command feedback. This command is a **read-only `UserState` snapshot** — the same fields **Sprint 58** **`/transfer`** moves — **not** jobs, unclaimed items, full locale/action-bar preferences, or other data outside `UserState`.

**Reference:** Bukkit **`ProfileCommand`** — `skills` / `stats` subcommands under `%skills_alias%` (no Bukkit top-level `/profile`).

## Preconditions

- Build **≥ 2.3.87** with **Sprint 59**; prior **Sprint 58** **`/transfer`** behavior is a useful mental model.
- Grant **`auraskills.command.profile`** (Bukkit default **true**; use LuckPerms / **fabric-permissions-api** provider in production to restrict if needed).
- For **offline** name resolution, the server **`NameToIdCache`** must know the name (player has joined this server before), **or** use an **online** target.

## `profile skills <player>`

1. As **op** (or with permission), run **`/profile skills <OnlineName>`** (or **`/skills profile skills <OnlineName>`**).
2. Expect **`commands.profile.skills`** header/entries from **messages_*.yml** (via **`CommandMessage`**) with **level** and **XP** per **loaded** skill.
3. A final **Granite** line: **UserState** disclaimer (not a full account).
4. **Console:** same command; messages use the plugin **default** locale for formatting.

## `profile stats <player>`

1. Run **`/profile stats <OnlineName>`** (or **`/auraskills profile stats …`**).
2. Expect **stats** block matching Bukkit’s **base / modified / total** pattern for **enabled** stats (*derived* from **skill levels** in `UserState` + **persistent stat modifiers**).
3. **Granite** additions after the stats list: **UserState mana**, **raw persistent stat modifier** list, **raw persistent trait modifier** list, then the **disclaimer** line.
4. **Bukkit** does **not** print the mana + modifier **footer** — this is an intentional **Sprint 59** extension for **transfer** parity.

## Name resolution and offline (YAML)

- **Online:** name match is **case-insensitive**.
- **Offline:** `NameToIdCache#findByName` — if the name is **unknown**, you get **unknown or unresolved player name** (en_us: `…profile.player_not_found`).
- **File storage, offline, no** `config/auraskills/userdata/<uuid>.yml`: command **refuses** (aligned with **transfer** — en_us: `…profile.no_userdata_file`).
- **MySQL** (or other **non-file**): no YAML pre-check; **load** comes from the provider. If **no row** / blank load, output may look like a **default-shaped** `UserState` (same **caveat** as **`SPRINT_58_MANUAL_TRANSFER.md`**).

## Bedrock (Geyser / Floodgate)

- **Chat**-only; type full subcommands if **tab** differs.
- **Floodgate** **UUIDs** and **permission** setup follow the same rules as other **Granite** admin commands; resolve **names** like Java when the name cache is populated.

## Pass / fail

- [ ] **skills** + **stats** work for an **online** target with real progression data.
- [ ] **Permission** deny without **`auraskills.command.profile`**.
- [ ] **Unknown** offline name → **player_not_found** (or equivalent).
- [ ] **Offline** with **file** storage and **no** `userdata` file for that UUID → **no_userdata_file** (when applicable).
- [ ] **Disclaimer** line appears and states **UserState**-only scope.
- [ ] **Console** can run the command and receives **text** output.

## What is *not* validated here

- **Slate** menus; **backup** / **storage** / **preset** **mutation**; **Bukkit**-style **async** `loadState` (Fabric runs **synchronously** on the server thread, like **transfer** on Fabric).

See **`fabric/SPRINT_SUMMARY_SPEC.md`** for sprint handoff structure.
