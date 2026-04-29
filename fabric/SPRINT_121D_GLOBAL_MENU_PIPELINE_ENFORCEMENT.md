# Sprint 121D — Global Menu Pipeline Enforcement

## Purpose

Stop the loop where only menus that appeared in screenshots were fixed. **One** player-facing presentation pipeline applies to **every** Granite Skills inventory menu (built-in + template-backed render hooks): formatted text, **42-character** description wrap, **tooltip suppression**, **FabricMenuTitles**, iron door back controls, arrows **only** for paging.

## Why Sprint 120 was not enough

Sprint 120 repaired specific QA callouts, but other menus still used **local** `Text.literal`, **glass** fillers, **`PlatformUtil`** flattening, or skipped shared helpers. Fixes did not **constrain** future code via a **single** choke point and **scan tests**.

## Shared helpers (introduced or relied on)

| Helper | Role |
|--------|------|
| **`FabricMenuLoreFormatting`** | `wordWrapPlain`, `formatMenuLine` (MiniMessage/legacy → **`FabricTextFormatter` / `FabricTextAdapter`**), `appendWrappedFormattedLore` (default width **42**, optional soft cap on input length). |
| **`FabricMenuItemPresentation`** | `applyQuietFiller` (space display name + suppression), `pageNavArrow` (paging arrows only). |
| **`FabricMenuTooltipPresentation`** | Strip leak-prone components + hide mechanics in `TOOLTIP_DISPLAY`. |
| **`FabricMenuTitles`** | Inventory titles: primary **dark_gray + bold**, secondary **gray** (Sprint 119 baseline). |
| **`FabricGraniteTemplateStacks`** | Template fill without display name → quiet filler via **`FabricMenuItemPresentation`**. |

## Hard rule: 42-character wrap

- **Description / body** lore: wrap at **42** characters (`DEFAULT_MENU_LORE_WRAP_WIDTH`), preserve newlines, do not split words unless a single token exceeds the width; paragraphs that look like raw MiniMessage (`<...>`) are passed through as one line to avoid tearing tags.
- **Short** header/value lines are not wrapped unnecessarily.

## Menu-by-menu paths (audit summary)

| Menu | Title | Stacks / lore | Filler | Back / controls | Suppression |
|------|--------|---------------|--------|-----------------|-------------|
| **Skills** | `FabricMenuTitles` / registry | Built-in: **`formatMenuLine`** name, Granite keys lore; template: **`FabricGraniteTemplateStacks`** | Empty slots in grid; template quiet fill when defined | Paging: **`pageNavArrow`** | Rows + nav |
| **Stats** | Same | **`formatMenuLine`** name; **`appendWrappedFormattedLore`** for description; trait lines **`formatMenuLine`** | Same pattern | Paging: **`pageNavArrow`** | Rows + nav |
| **Stat Info** | **`FabricMenuTitles.statInfoTitle`** | Built-in + template: Granite keys + **`appendWrappedFormattedLore`** for description | Template fill / empty per layout | **`IRON_DOOR`** (configurable id) | Main + back |
| **Level progression** | **`FabricMenuTitles.levelProgressionTitle`** | Row lore **`formatMenuLine`**; controls MiniMessage via **`FabricGraniteTemplateStacks.toLiteralNoUnresolved`** where used | **Built-in:** `ItemStack.EMPTY` for non-path slots (no glass); template: YAML fill | Back **`IRON_DOOR`**; paging **`pageNavArrow`**; Sources/Abilities custom icons | All stacks |
| **Sources** | **`FabricMenuTitles.sourcesMenuTitle`** (sort **not** in title) | **`formatMenuLine`** name; **`FabricSourceTypeLabels`** for type line; XP lines from keys | Empty grid slots | Back **`IRON_DOOR`**; sort **COMPARATOR**; paging arrows | All |
| **Abilities** | **`FabricMenuTitles.abilitiesMenuTitle`** | Body descriptions via **`FabricAbilitiesTemplateSupport` → `appendWrappedFormattedLore` @ 42** | Empty | **`IRON_DOOR`** | All |
| **Leaderboard** | **`FabricMenuTitles.fromLeaderboardContextLine`** + paging | **Paper** rows (built-in); keys for name/value | Empty top grid | **`IRON_DOOR`**, **`COMPASS`** refresh, arrow paging | All |
| **Unclaimed** | **`FabricMenuTitles.unclaimedItemsTitle`** | Built-in / template aligned with branch | Per template / empty | Controls per YAML | Intended on all menu stacks |

**Leaderboard:** Power-only vs per-skill behavior remains a **product** decision (see PARITY backlog / future Sprint **121C** discovery); this sprint only enforces **presentation** cleanliness.

**Abilities:** Icon grid / layout refresh is **deferred**; text must not overflow (42-char wrap).

## Template YAML

**Sprint 122 update:** bundled **`assets/auraskills/granite/templates/*.yml`** no longer declare a global **`fill`** using glass panes — empty slots are left empty. If a future menu needs a fill, use **quiet** presentation (no visible pane id as the player-facing identity) per **`FabricMenuItemPresentation`**. **No** `Items.GLASS_PANE` in `fabric/.../menu/*.java` (enforced by **`FabricMenuJavaPipelineScanTest`**).

## Tests and build

- **`FabricMenuLoreFormattingTest`** — wrap width and long-token split.
- **`FabricMenuJavaPipelineScanTest`** — banned patterns in listed menu sources (`GLASS_PANE`, stained glass pane item, raw `"<#` / `"<underline` in Java string literals, **`GOLD_NUGGET`** in **`FabricLeaderboardMenuGui`**).
- Command: `.\gradlew.bat -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build verifyFabricReleaseBundle` — **PASS** on ship commit.

## Screenshot checklist (operator)

After installing **`FabricAuraSkills-2.3.150.jar`**:

- `/skills`, `/stats`, `/stats` → a stat (Stat Info), `/openmenu level_progression fighting`, `/sources fishing`, `/openmenu abilities fighting`, `/openmenu leaderboard`, `/openmenu unclaimed_items` if applicable.

**PASS:** no named glass panes where empty slots are used; no raw `&7` / `§` / `<#…>` / internal keys in **visible** lore; no vanilla mechanics on icons; iron door back on submenus; arrows only for paging; ability descriptions wrap; titles match **`FabricMenuTitles`** style.

## Bedrock / Geyser

**Not tested** in this sprint (unchanged policy).

## Known limitations (still open)

- **Abilities** — icon set / layout parity with design.
- **Leaderboard** — per-skill mode vs Power default; full product parity TBD.
- **Unclaimed** — may be hard to validate with zero pending items (still migrate code path).
- **Full Slate / MenuFileManager** — not claimed complete.

## Sprint 120 vs 121D

Sprint **120** was a **repair gate** on reported screenshots. Sprint **121D** adds **enforcement**: shared helpers, **42** wrap as default, empty progression slots, removal of **`PlatformUtil`** menu flattening on stats rows, and **regression scans** so untouched menus cannot silently diverge.

## Ship record

- **JAR:** FabricAuraSkills-2.3.150.jar (`verifyFabricReleaseBundle` OK). Exact **git** hash: run `git log -1 --oneline` on branch `feature/granitelib-module-ids` at your checkout.
