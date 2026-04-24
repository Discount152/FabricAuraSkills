# Sprint 55 — manual validation: `/item register`, `/item unregister`, `/item give`

**Scope:** Fabric only; Bukkit is reference. Cross-check **Java** and **Bedrock (Geyser/Floodgate)** if possible — commands run server-side; feedback is chat.

## Permissions (LuckPerms / fabric-perms)

- `auraskills.command.item.register` — `/item register`, `/item unregister`
- `auraskills.command.item.give` — `/item give`

## Java — register / unregister (player, main hand)

1. Op or grant `item.register`. Hold a **non-empty** main-hand stack (e.g. diamond with custom name).
2. `/item register test_key`  
   - Expect success; config folder **`config/auraskills/items.yml`** (or the server’s `auraskills` data path) should gain an **`items.…`** entry.
3. Run again with the same key (still holding an item) — expect “already registered” (no second write).
4. Key with a space in the name — should fail (“no spaces”).
5. **Empty hand** + `/item register other_key` — expect empty-hand error (`item_armor_mod.empty_hand` translation).
6. `/item unregister test_key` — expect success; entry removed from in-memory registry and persisted via **`saveToDisk`**.

## Java — give

1. Grant `item.give` (and ensure a key exists, e.g. `test_key` from above).
2. Target online player **A**; run `/item give A test_key`  
   - **Omit** amount → count matches the **template** stack’s count (default **-1** behavior, like Bukkit).
3. `/item give A test_key 1` — explicit count ≥ 1.
4. **Full inventory** — overflow should go to **unclaimed** (same as other `giveItem` paths) + unclaimed **message** to target.
5. **Console:** `/item give A test_key` (player name = online) — should work; sender/receiver **ITEM_GIVE_*** lines use the **source** locale (console → default language).

## Bedrock / Geyser

- Same server-side steps; use chat to enter commands. Tab completion is **best-effort** on Bedrock.
- If permissions differ for Floodgate suffix names, use an account that has `item.give` / `item.register`.

## Reload / persistence

- After **register**/**unregister**, disk should update without restart.
- Run **`/auraskills reload`** (or full restart) and confirm **registered** items still **load** from **`items.yml`**.

## Known gaps (by design for this sprint)

- No Bukkit **ExternalItemProvider** / namespace plugins.
- **Offline** give targets: not supported (online resolution only, same as other Fabric admin commands that take a name).
