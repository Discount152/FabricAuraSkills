# Sprint 34 — manual validation (built-in `granite.layout` slot positions)

**Scope:** `granite.layout` under each built-in `menus/*.yml` file — `list_start_slot`, `list_end_slot_exclusive` (list menus), `main_slot`, `back_slot` (detail menus). **Runtime:** Fabric dedicated server; **clients:** Java and Bedrock (Geyser + Floodgate) as available.

## Prereq

- Granite Skills + GraniteLib; server folder contains generated `config/AuraSkills/menus/*.yml` (or use defaults from the JAR after delete/regenerate for a clean test).
- Commands: open **Skills** (`/skills` or via `/auraskills` if wired), **Stats**; from Skills, open **Level Progression** on a skill; from Stats (with `menus.stat_info.enabled`), open **Stat Info** on a stat.

## Java

1. **Skills:** List books appear starting at the configured `list_start_slot` (default 0) and fill up to, but not including, `list_end_slot_exclusive` (default 54). Clicking a book opens Level Progression for that skill; slot index matches the skill order for the visible range.
2. **Stats:** Same list semantics; clicking a row opens Stat Info when stat info is enabled.
3. **Level Progression / Stat Info:** Main item (book) is at `main_slot` (default 4). Back (arrow) is at `back_slot` (default 49). **Back** returns to **Skills** or **Stats** respectively.

## Bedrock / Geyser

Repeat the same flows. Confirm chest GUI opens, items appear in the expected visual positions (slot mapping is still top 0–53), and **Back** and list clicks work. Document any Geyser-specific quirks in the parity backlog if new.

## Bad slot / fallback (server log + behavior)

1. **Invalid list range** (e.g. `list_end_slot_exclusive: 10`, `list_start_slot: 20`): server logs a **warn**; list reverts to **code defaults** (0 and 54 for Skills/Stats defaults). Open **Skills** — full row range should behave as with defaults.
2. **Out-of-range int** (e.g. `list_start_slot: 99`): warn; that field uses its **default** from the built-in `FabricMenuYamlSlice` default for that menu.
3. **main_slot == back_slot** (e.g. both `4`): warn; main and back revert to **defaults** (4 and 49). **Level Progression** and **Stat Info** must still show two distinct items and **Back** must still return.

After editing YAML, run **`/auraskills reload`** (or equivalent) so **built-in menu YAML** reloads, then re-test.

## Checklist

| Check | Java | Bedrock |
|-------|------|---------|
| Skills list + click | [ ] | [ ] |
| Stats list + click (if stat info on) | [ ] | [ ] |
| Level Progression main/back + Back | [ ] | [ ] |
| Stat Info main/back + Back | [ ] | [ ] |
| Bad list range → fallback + warn | [ ] | [ ] (optional) |
| main == back → fallback + warn | [ ] | [ ] (optional) |
