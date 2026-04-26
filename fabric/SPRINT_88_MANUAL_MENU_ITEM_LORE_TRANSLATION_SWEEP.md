# Sprint 88 — Manual validation: menu item/lore translation sweep (vanilla Java)

| Field | Value |
|--------|--------|
| **Sprint** | **88** |
| **Build** | **`FabricAuraSkills-2.3.119.jar`** (`projectVersion` **2.3.119**) |
| **Reason** | Sprint **83** fixed command/menu **titles** and **“opened”** feedback; deeper **built-in menu GUIs** still used **`Text.translatable`** for **item names/lore** and controls, so **vanilla Java** clients could show raw **`auraskills.granite.*`** keys. Sprint **88** routes those paths through **`FabricGraniteText`**. |
| **Bedrock / Geyser** | **Not tested** — **87** remains **deferred** (no Bedrock tester). |

## Menus swept (code)

- **Skills** — **`FabricSkillsMenuGui`**
- **Stats** — **`FabricStatsMenuGui`**
- **Level progression** — **`FabricLevelProgressionMenuGui`**
- **Stat info** — **`FabricStatInfoMenuGui`**
- **Sources (GUI)** — **`FabricSourcesMenuGui`**
- **Unclaimed items** — **`FabricUnclaimedItemsMenuGui`**
- **Leaderboard** — **`FabricLeaderboardMenuGui`**
- **Abilities** — **`FabricAbilitiesMenuGui`**
- **Shared** — **`FabricListMenuPaging`** (page subtitle), **`FabricMenuRegistry`** (disabled notice)
- **Commands (menu feedback)** — **`FabricOpenMenuCommands`**, **`FabricOpenMenuAbilities`**, **`FabricOpenMenuLeaderboard`**
- **Sources GUI failure line** — **`FabricSourcesOpenSupport`** (`sources.menu_fallback` only)

## Known exclusions

- **`FabricSourcesOpenSupport#sendChatListing`** — **`/sources`** (and console) **paged chat** output still uses **`Text.translatable`** for header/continuation/lines. Vanilla may still see raw Granite keys there until a future sweep.
- **YAML-overridden format keys** — menus resolve keys via **`FabricMenuYamlSlice#resolveFormatKey`**; custom keys must exist in server lang (or the helper falls back to showing the key string).

## Java operator checklist (vanilla client)

Deploy **`FabricAuraSkills-2.3.119.jar`**. Use a **vanilla Java** client (no FabricAuraSkills on the client).

Open and inspect **titles**, **item names**, and **lore** (hover):

| Step | Command / action | Expect |
|------|------------------|--------|
| 1 | `/skills` | No raw **`auraskills.granite.*`** in title, skill rows, prev/next |
| 2 | `/stats` | Same; stat rows and open-detail line |
| 3 | `/openmenu level_progression farming` | Main book, back, **View XP sources** hopper |
| 4 | `/openmenu stat_info strength` | Main, back |
| 5 | `/sources farming` | GUI title, sort control, rows, paging, back |
| 6 | `/openmenu sources farming` | Same |
| 7 | `/openmenu unclaimed_items` | (if non-empty) claim all, paging, rows |
| 8 | `/openmenu leaderboard` | Power mode title, rows, refresh, paging, back |
| 9 | `/openmenu leaderboard average` | Average title |
| 10 | `/openmenu leaderboard farming` | Per-skill title |
| 11 | `/openmenu abilities` / `… farming` | Title, ability/mana rows, back |

Also check: **no raw MiniMessage tags** in Granite menu strings; **placeholders** (level, XP, rank, etc.) render as values.

## Screenshot checklist (optional)

- One **Skills** page with lore visible  
- **Sources** with sort label in title  
- **Abilities** with locked and unlocked rows  

## Pass/fail table

**In-repo agent:** **NRP** — not run in-game; **`.\gradlew -PfabricOnly=true :fabric:test :fabric:build`** **PASS** at doc time.

| Area | Pass | Fail | Notes |
|------|------|------|-------|
| Skills menu | | | |
| Stats menu | | | |
| Level progression | | | |
| Stat info | | | |
| Sources GUI | | | |
| Unclaimed items | | | |
| Leaderboard | | | |
| Abilities | | | |
| Openmenu feedback (chat) | | | |

## Files changed (reference)

Java: **`fabric/.../menu/`** (`Fabric*MenuGui`, **`FabricListMenuPaging`**, **`FabricMenuRegistry`**), **`FabricOpenMenuCommands`**, **`FabricOpenMenuAbilities`**, **`FabricOpenMenuLeaderboard`**, **`FabricSourcesOpenSupport`**. Tests: **`FabricGraniteEnUsKeysTest`**, **`FabricMenuGuiTranslatableScanTest`**. Root **`gradle.properties`**, **`Changelog.md`**, **`fabric/SPRINT_*.md`**, **`fabric/PARITY_*.md`**, **`fabric/MENU_AND_COMMANDS_PORT.md`**.

## Keys

No new **`en_us.json`** keys required for defaults; sprint adds **test coverage** for existing menu/sort/openmenu keys. Custom menu YAML may introduce new keys — add them to **`en_us.json`** and **`FabricGraniteEnUsKeysTest`** if shipped.
