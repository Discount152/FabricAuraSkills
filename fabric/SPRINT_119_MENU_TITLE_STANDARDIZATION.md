# Sprint 119 — Granite menu title standardization (Fabric)

## Rule

**Inventory menu titles** (not row/item accents unless a change is required to wire the title helper):

- **Primary label:** dark gray + bold — Minecraft `Formatting.DARK_GRAY` + bold (legacy `§8§l`, MiniMessage `<dark_gray><bold>`).
- **Secondary / context:** gray — `Formatting.GRAY` (`§7`, `<gray>`).
- **Page suffix:** gray, from `auraskills.granite.menu.list_paging_subtitle`.
- **Do not** use **gold** for primary menu titles on built-in or Sprint 119–touched template paths.

## Implementation

- Helper: **`dev.aurelium.auraskills.fabric.menu.FabricMenuTitles`** — `primaryPlain`, `secondaryPlain`, `combinePrimaryThenSecondary`, `combineSecondaryThenPrimary`, `fromLeaderboardContextLine`, `appendPageSuffixGray`, hub/registry `granitePrimaryTitleFromKey`, and menu-specific builders (`levelProgressionTitle`, `statInfoTitle`, `abilitiesMenuTitle`, `sourcesMenuTitle`, `unclaimedItemsTitle`).

- **Skills/Stats hub:** Registry titles use **`granitePrimaryTitleFromKey`** over plain `en_us` strings **Your Skills** / **Your Stats** so styling matches bundled template intent without raw `§` in Java.

- **Bundled YAML:** Sprint 119 menu `title` fields are placeholders or ignored where Java fully owns the inventory title; item/row styling may still use legacy codes where YAML explicitly formats names/lore.

## Lang keys (Fabric `en_us.json`)

- `auraskills.granite.menu.level_progression_levels_suffix`
- `auraskills.granite.menu.stat_info_title_prefix`
- `auraskills.granite.menu.sources_title_secondary`
- `auraskills.granite.menu.abilities_title_suffix`
- `auraskills.granite.menu.unclaimed_items_title`

## Changelog / version

See root **`Changelog.md`** and **`gradle.properties`** `projectVersion` for the release that contains this sprint.
