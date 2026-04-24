# Sprint 44 — manual validation: **`/sk`**, **`/skill`**, **`openmenu`**

**Scope:** compatibility-style **alias roots** and **bounded openmenu** for built-in **skills** / **stats** menus on Fabric (Java + Geyser chat).

## Preconditions

- Build **≥ 2.3.72** (Sprint 44).
- Two online players for **openmenu** targeting another.
- Permissions: `auraskills.command.skills`, `auraskills.command.openmenu`, `auraskills.command.stats` (for **`/stats`** unchanged).

## Alias roots (`/sk`, `/skill`)

1. With **`auraskills.command.skills`**, run **`/sk`** and **`/skill`** with no args — expect **Skills** menu (same as **`/skills`**).
2. Revoke **`skills`** — default execute should show **permission_denied** (`auraskills.granite.command.permission_denied`).
3. With **`skills`** but without **`openmenu`**, run **`/sk openmenu skills`** — expect **openmenu** denied (node `openmenu` requires **`openmenu`**).
4. Grant **`openmenu`** — **`/sk openmenu skills`** should open **Skills** for the executor.

Repeat **`/skill openmenu stats`** (and **`/auraskills openmenu stats`**).

## Top-level **`/openmenu`**

1. **`/openmenu skills`** (as a player) — opens **Skills**, feedback like **`/skills`**.
2. **`/openmenu stats`** — opens **Stats**.
3. **Console:** **`/openmenu skills someOnlinePlayer`** — opens for target; executor gets **`opened_for`**.
4. Invalid id (e.g. **`level_progression`**) — **`openmenu.unknown_built_in`**.

## Regression

- **`/stats`** and **`/auraskills stats`** still open **Stats** as before.
- **`/auraskills skills`** unchanged.
- **Disabled** built-in in YAML: menu open should still show **`built_in_disabled`** from the registry (same as menu buttons).

## Geyser / Bedrock

- Type commands in full; verify permission gates and messages appear as normal chat/system feedback.

## Pass / fail

- [ ] **Aliases** match **`/skills`** for default; **`openmenu`** subnode respects **`openmenu`** perm.
- [ ] **Built-in** allowlist is **skills** and **stats** only; no Slate JSON.
