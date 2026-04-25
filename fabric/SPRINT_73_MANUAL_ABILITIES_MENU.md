# Sprint 73 — manual validation: Abilities menu (Fabric)

**Build:** `projectVersion` **2.3.101** (see `gradle.properties`).

**Scope:** one lane — **abilities menu** + **`/openmenu abilities`**; **no** ability gameplay / cooldown / mana logic changes, **no** Slate **`MenuFileManager`** parity.

## Bukkit reference (in-repo)

- **`bukkit/.../AbilitiesMenu.java`** — Slate: **`skill`** menu property, **locked_** / **unlocked_** templates for **Ability** and **ManaAbility**, **back** via **`GlobalItems#backToLevelProgression`** (previous menu **level_progression**).
- **`MenuRegistrar`** registers **`abilities`** with that builder.
- **`abilities.yml`** is a **runtime user** file (like other configs), not checked into this repo.

## Fabric implementation

- **`FabricAbilitiesMenuGui`** — 6×9 **`openClickable9x6`**: one row (and overflow row) of **read-only** stacks for enabled **passive** abilities + **one** **mana ability** slot when the skill defines one. **Back** (slot **45**) → **`FabricAuraSkillsMenus#openLevelProgression`** with **`returnToSkillsListPage` = 0** from **`/openmenu`** (no hopper from level progression in this sprint).
- **`FabricOpenMenuAbilities`** — Brigadier subtree under **`openmenu`**.

## Entry paths

- **`/openmenu abilities`** — in-game **only**: opens **first enabled** skill (lexicographic **skill id key**). **Console:** error (use explicit **player + skill** or **`/openmenu abilities <player>`** with default skill).
- **`/openmenu abilities <skill>`** — self, that skill (in-game).
- **`/openmenu abilities <token>`** — if **token** parses as an **enabled skill** → self; else if **token** is an **online player name** → open for that player with **default** first enabled skill.
- **`/openmenu abilities <player> <skill>`** — **online** target receives the menu for that **skill** (console + in-game).

Mirrors: **`/auraskills openmenu abilities …`**, **`/skills`**, **`/sk`**, **`/skill`** (same shared **`openmenu`** tree). Permission: **`auraskills.command.openmenu`**.

## Player behavior (Java)

1. Join with **`openmenu`** permission; pick a skill with abilities (e.g. **Farming**).
2. **`/openmenu abilities farming`** — menu title **Abilities — &lt;skill&gt;**; rows show **Locked** vs level; mana line if the skill has a **mana ability**.
3. **`/openmenu abilities`** — same as **(1)** using **default** first skill (check which skill is first alphabetically by key — document for testers).
4. **Back** returns to **level progression** for that skill (list page **0**).

## Console / RCON

- **`/openmenu abilities <OnlinePlayer> <skillkey>`** — must succeed when target is online.
- **No-arg** and **single-token skill** forms are **in-game**-oriented; use **`context.console_specify`** / **`abilities_console`** messages when applicable.

## Skill / data

- Uses **`User#getAbilityLevel`**, **`getManaAbilityLevel`**, **`AbilityManager`**, **`ManaAbilityManager`**, **Roman numerals** per **`config.yml`** **`enable_roman_numerals`**.

## YAML

- **No** `menus/abilities.yml` **layout** on Fabric for this menu — **Partial** vs Slate.

## Bedrock / Geyser

- **Not run-time tested** (inventory UI). **NRP**; treat as high-risk for click parity. **`/abtoggle`** / chat unaffected.

## Tests

- **`.\gradlew -PfabricOnly=true :fabric:test`** — record result in CI / notes.

## Pass / fail checklist

- [ ] **`FabricCommandBuiltInMenuId`** includes **`abilities`**
- [ ] No **NullPointer** when a skill has **no** mana ability
- [ ] **Enabled** / **disabled** skills rejected consistently with other **`openmenu`** commands

## Risks

- **Default skill** for no-arg / player-only arg is **deterministic** (sorted key) — may differ from a player’s expectation; document.
- **Description** text may be long; lore is **truncated** (see **`abilities_lore_truncated`**).
