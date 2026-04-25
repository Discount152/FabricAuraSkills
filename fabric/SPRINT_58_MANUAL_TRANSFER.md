# Sprint 58 — manual validation: `/transfer`

**Scope:** Fabric **Granite Skills** only. **Java** and **Bedrock (Geyser/Floodgate)** for command feedback.

## Preconditions

- Grant **`auraskills.command.transfer`** (Bukkit default **op**; use LuckPerms / Fabric permission provider on production servers).
- Prefer a **test world backup**; `/transfer` overwrites the **target** side’s `UserState` in storage and/or a loaded user.

## What moves (bounded)

- **`UserState`** from common: per-skill **level** and **XP**, **persistent** **stat** / **trait** modifiers, and **mana**.
- **Not** in `UserState` (and therefore **not** moved by this sprint): **jobs**, **unclaimed items**, per-**ability** data, **locale**, **action bar** settings, **anti-AFK** logs, etc. The **target** may keep prior values for those fields in YAML (offline merge) or in memory (online).

## Happy path (YAML, two accounts)

1. **Player A** (UUID `UA`) plays until they have non-default skills; stop the server or run `/auraskills save` so **`userdata/UA.yml`** exists.
2. **Player B** (UUID `UB`) with a different profile (or a fresh empty file after first join).
3. As **console** or in-game with permission, run: **`/transfer UA UB`** (full hyphenated UUIDs as two tokens).
4. Expect **success** (messages from **`messages_*.yml`** `command.transfer` success + a Granite line that the **source** file is unchanged on disk).
5. Log in as **B**: levels/mana/mods from **A**’s `UserState` should appear after normal stat recalculation.
6. **Reload** or restart: **B**’s data should still match (storage `applyState` / `save` path).

## Self-transfer

- **`/transfer UA UA`** must **fail** with the self-deny message from **`en_us.json`**.

## Missing source (YAML, offline)

- With **no** `userdata/<from>.yml` and that UUID **not** online, the command should **refuse** (Fabric-only pre-check).

## Source online, no `User` map (edge)

- Hard to hit in production; if it does, the command reports **source not loaded**. Usually fixed by waiting until after join + profile load.

## Target online, not loaded (edge)

- If **UB** is on the server but the AuraSkills user is not in the `UserMap` yet, expect **target not loaded**.

## MySQL / SQL

- There is **no** “file must exist” gate: **`loadState(from)`** may return a **blank-shaped** `UserState` for an unknown `from` in SQL. In that case the command may still “succeed” but effectively copy **defaults** — treat as a **caveat**; for safety, use **YAML** tests first or back up the DB.

## Source file unchanged

- The **old** identity’s `userdata/UA.yml` (or SQL row) is **not** auto-deleted; success text reminds you. Remove or keep manually after verifying **UB**.

## Build

```text
.\gradlew -PfabricOnly=true :fabric:test
```

Expected: **BUILD SUCCESSFUL** (includes **`FabricCommandPermissionsTest`**, **`TRANSFER`** constant).
