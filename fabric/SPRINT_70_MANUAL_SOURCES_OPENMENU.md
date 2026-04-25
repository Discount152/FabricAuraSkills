# Sprint 70 — manual validation: sources + `openmenu` integration

**Build:** `projectVersion` **2.3.98** (`gradle.properties`).

**Scope:** integrate the Sprint 69 **sources** GUI with the existing **bounded** `openmenu` surface; improve **Back** list-page behavior when entering sources from **level progression**; **no** full Slate / `sources.yml` parser.

## Command forms added

| Form | Console / RCON | In-game |
|------|------------------|---------|
| `/openmenu sources <skill>` | Rejected — use target form | Self: opens sources (sort **ascending**) |
| `/openmenu sources <skill> <sort>` | Rejected | Self: sort must be a valid token |
| `/openmenu sources <player> <skill>` | Target required | Opens **on the target’s** client |
| `/openmenu sources <player> <skill> <sort>` | Same | Same + sort |

Same tree under **`/auraskills openmenu`**, **`/skills openmenu`**, **`/sk openmenu`**, **`/skill openmenu`**, and top-level **`/openmenu`**.

## Permission

- **`auraskills.command.openmenu`** (unchanged; default matches other `openmenu` nodes — see `FabricCommandPermissions`).

## `/sources` (Sprint 69) — unchanged behavior

- **Player:** GUI when the skill has ≥1 version-valid source; **Back** from **command** paths still uses return page **0** (no remembered Skills page).
- **Console/RCON:** paged **chat** only.
- **Empty sources:** `auraskills.granite.command.sources.empty` — **no** empty chest; **no** “opened” line on `openmenu` either.

## Player self-open (`openmenu`)

- In-game: **`/openmenu sources <skill> [sort]`** — executor must be a **player**.
- On **success** (GUI opened): `auraskills.granite.menu.sources_opened`.
- On **empty** or **failed GUI** (fallback to chat for self): no “opened” line; empty or `menu_fallback` + chat as applicable.

## Open for another player

- **Online only** (`FabricCommandPlayerTarget`); not offline.
- Executor gets **`opened_for_context`** when the screen actually opens for the target (not on empty / failure).

## Console / RCON

- Must use **`/openmenu sources <player> <skill> [sort]`**.
- If a context menu is specified without a player when the game requires it, **`openmenu.context.console_specify`** (updated in `en_us` to list **sources**).

## Sort

- `ascending`, `descending`, `alphabetical`, `reverse_alphabetical` — same as `/sources` / Sprint 57.
- Omitted sort → **ascending** (by enum / `fromTokenStrict("")`).
- Invalid sort → `auraskills.granite.command.sources.bad_sort`.

## Unknown / disabled skill

- **`openmenu.unknown_skill`** (same as other **openmenu** context menus).

## Back button behavior (final)

1. **`/sources`**, **`/openmenu sources …`**, **`/skills sources …`:** **Back** on the sources screen calls **`openLevelProgression(skill, player, plugin, 0)`** — Skills return page **0** when re-opening level progression.
2. **From level progression (hopper, slot 50, Sprint 70):** **Back** on sources uses **`context.listPage()`** from that level progression open, so the player returns to **level progression** with the same **Skills** list page as when they opened it from the paged **Skills** menu.

## Java dedicated server — quick checks

- [ ] ` /openmenu sources mining` — opens menu (if mining has sources)
- [ ] ` /openmenu sources mining descending`
- [ ] ` /openmenu sources <OtherPlayer> mining` (operator / permission)
- [ ] ` /openmenu sources` with no args — invalid (Brigadier)
- [ ] Console: `openmenu sources <onlineName> <skill> [sort]`
- [ ] From **Skills** (page 2) → **Level progression** → **hopper (sources)** → **Back** → same level progression context; **Back** again → **Skills** page 2
- [ ] **Empty** skill: empty message, no GUI, no “opened” line on `openmenu`

## Bedrock / Geyser

- **Not tested** in this sprint. **NRP** — same 6×9 inventory as other Granite menus. Document limitations if click routing differs.

## YAML

- **Still no** `menus/sources.yml` **load** on Fabric; **not** in **`FabricMenuLoaders`**. **Partial** until Slate/MenuFileManager work.

## Pass / fail

- [ ] `.\gradlew -PfabricOnly=true :fabric:test` passes
- [ ] `FabricCommandBuiltInMenuId` tests include **`sources`**

## Risks / caveats

- **Slot 50** on level progression is **hard-coded**; if YAML `back_slot` / custom layouts ever use **50**, the hopper may overlap — rare with defaults (main 4, back 49).
- **openmenu** does not pass a non-zero **return Skills page**; only the **level progression** hopper path does.
