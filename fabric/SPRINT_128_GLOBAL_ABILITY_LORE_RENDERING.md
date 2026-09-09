# Sprint 128 - Global ability lore rendering (Fabric)

**Version:** 2.3.157

## Root cause

Fabric abilities menus called `getBaseDescription` (and partial `TextUtil.replace` in some paths) but did not apply the **full** placeholder set that stock AuraSkills uses in `AbilitiesMenu` / `LevelUpMessenger`: `{value}`, `{value_2}`, `{chance_value}`, `{guaranteed_value}` for passives; `{value}`, `{duration}` for mana; plus **mana-specific** static strings such as `{haste_level}` and `{radius}`. Numbered segment markers (`<1>`, `<2>`, ...) were left as raw text because Bukkit replaces them with Adventure color tags; plain Fabric lore has no equivalent, so they leaked into tooltips. `!!REMOVE!!` sentinels could also leak if not stripped.

## Pipeline (before / after)

**Before:** message load -> partial replace -> wrap / MiniMessage -> tooltip (often with unreplaced curly placeholders and numbered markers).

**After:** message load -> `AbilityManager#getBaseDescription` / `ManaAbilityManager#getBaseDescription` (including `replaceDescPlaceholders` and Fabric static mana substitutions) -> **menu-level** placeholder expansion (`FabricAbilityMenuLore`, rules aligned with Bukkit `AbilitiesMenu`) -> **`FabricAbilityLoreNormalization`** (`!!REMOVE!!`, numbered markers become newlines) -> wrap (`splitDesc` / template append) -> MiniMessage where applicable -> tooltip.

## AuraSkills reference

- Bukkit: `dev.aurelium.auraskills.bukkit.menus.AbilitiesMenu` (locked / unlocked / upgrading / maxed description replacements).
- Level-up copy: `LevelUpMessenger` (mana unlock / level-up tokens and patterns).
- Fabric parity: `FabricManaAbilityDescPlaceholders` for provider-static mana fields; `FabricArcheryAbilities#replaceDescPlaceholders` for `{time}` / `{reduction_percent}`.

## Numbered tokens

AuraSkills uses `<n>` as activation-hint segment markers (Bukkit maps `<1>` to white for MiniMessage). For Fabric plain lore they become **newlines** so hints separate cleanly after wrapping.

## Tests

- `FabricAbilityLorePipelineS128Test`: normalization, unresolved-token detector, Terraform static radius and Speed Mine style strings.
- **Not covered:** full embedded-Minecraft GUI for `openmenu abilities`.

## Scope

`/sources`: not modified. Leaderboard menus: not modified.