# Sprint 99 — Manual: GraniteLib template runtime consolidation

| Field | Value |
|--------|--------|
| **Sprint** | **99** — shared helpers only (**no** new menu migration) |
| **JAR** | **`FabricAuraSkills-2.3.127.jar`** (+ **`granitelib-fabric`**) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Reason

**Sprints 96–98** proved **`stat_info`** and **`sources`** on the template-first path. Before migrating larger menus, duplicated load/expand/item patterns were consolidated so future work does not copy/paste the same strict fallback and **`ItemStack`** rules.

## Duplicated patterns removed or centralized

| Pattern | Where it lived | Consolidation |
|--------|----------------|---------------|
| `{key}` expansion + unresolved `{...}` regex | **`FabricStatInfoPlaceholders`**, **`FabricSourcesPlaceholders`** | **`GraniteTemplatePlaceholders`** (GraniteLib); menu modules keep **key order** lists where empty defaults matter |
| Classpath YAML open | **`FabricStatInfoTemplateSupport`**, **`FabricSourcesTemplateSupport`** | **`GraniteTemplateResources.openClasspath`** |
| First slot by item key or action alias | **`FabricStatInfoTemplateSupport`**, **`FabricSourcesTemplateSupport`** | **`GraniteTemplateItems.firstSlotByPreferredKeyOrActions`** |
| Source grid item discovery | **`FabricSourcesTemplateSupport`** | **`GraniteTemplateItems.findSourceGridDefinition`** |
| Nav action string compare | **`FabricSourcesTemplateSupport`** | **`GraniteTemplateItems.actionEquals`** |
| Strict item id resolution, fill + item **`ItemStack`**, **`custom_model_data`** | **`FabricStatInfoTemplateSupport`**, **`FabricSourcesTemplateSupport`** | **`FabricGraniteTemplateStacks`** (Fabric — **requires** **`Registries.ITEM`**) |

## Helpers added

### GraniteLib (`dev.granitelib.fabric.template`)

- **`GraniteTemplatePlaceholders`** — `expandKeys`, `expandAllKeysLongestFirst`, `hasUnresolvedBraceSyntax`
- **`GraniteTemplateActionNames`** — documented action string constants (v0 contract)
- **`GraniteTemplateItems`** — slot/action lookups, `findSourceGridDefinition`
- **`GraniteTemplateResources`** — `openClasspath(Class<?>, path)`

### Fabric (`dev.aurelium.auraskills.fabric.menu`)

- **`FabricGraniteTemplateStacks`** — `resolveStrict`, `toLiteralNoUnresolved`, `buildFillStack`, `buildTemplateItemStack`

## What stayed menu-specific (and why)

| Area | Rationale |
|------|-----------|
| Placeholder **maps** (`stat_*`, `skill*`, `source_*`) | AuraSkills **`MessageProvider`**, **`User`**, **`XpSource`** |
| Trait lore append | **`FabricStatsMenuGui.addTraitLoreIfPresent`**, stat context |
| Sources sort / paging / back targets | **`FabricSourceSortType`**, **`FabricListMenuPaging`**, **`FabricSourcesMenuGui`** |
| Template id / rows / grid validation | Menu-specific invariants (**`stat_info`** vs **`sources`**) |
| Click handlers | Still wired per menu |

## Action contract (v0)

Canonical strings in **`GraniteTemplateActionNames`** (must match bundled YAML):

| Constant | YAML value | Meaning |
|----------|--------------|--------|
| **`BACK`** | `back` | Return to previous screen (wired in Fabric) |
| **`STAT_PANEL`** | `stat_panel` | Stat detail item; may get trait lore append |
| **`SOURCE_ROW`** | `source_row` | One sources grid row |
| **`PREVIOUS_PAGE`** | `previous_page` | Sources prev page |
| **`NEXT_PAGE`** | `next_page` | Sources next page |
| **`CYCLE_SORT`** | `cycle_sort` | Sources sort cycle |

Not a dynamic action engine — **constants + documentation** only.

## Placeholder helper behavior

- **`expandKeys(raw, map, orderedKeys)`** — replaces only listed keys; missing map entries → empty string (supports **`stat_description`** optional behavior when key is listed).
- **`expandAllKeysLongestFirst`** — for maps where every relevant key is present (used inside **`FabricGraniteTemplateStacks`** for item/fill lines).
- **`hasUnresolvedBraceSyntax`** — any remaining `{...}` after expansion → template path should abort (Fabric logs and falls back).

## Item rendering helper behavior

- **`FabricGraniteTemplateStacks`** uses **`GraniteTemplatePlaceholders.expandAllKeysLongestFirst`** for fill/name/lore strings, then **`toLiteralNoUnresolved`** → **`Text.literal`**.
- Unknown **`minecraft:`** / namespaced ids → warn + empty **`Optional`** → menu code falls back to built-in.
- **`FabricStatInfoItemComponents.applyCustomModelData`** applied on every template item stack built through this helper.

## Tests

**GraniteLib:** `GraniteTemplatePlaceholdersTest`, `GraniteTemplateItemsTest`, `GraniteTemplateActionNamesTest`

**Fabric:** existing **`stat_info`** / **`sources`** tests; bundled parse tests assert actions match **`GraniteTemplateActionNames`**.

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Known limitations

- **No** disk override, hot reload, global placeholder registry, or full action engine.
- **No** nested placeholders / PlaceholderAPI-style features.
- **Item** resolution remains **Fabric-only** (Minecraft registries).

## Operator validation

**Pending** on **`FabricAuraSkills-2.3.127.jar`**: smoke **`stat_info`** and **`sources`** once to confirm no regression (template + fallback).

## Recommended next migration

**Bounded template** on another list-heavy menu (e.g. **`level_progression`** surface) or deepen **placeholder/action** incrementally — still **not** claiming **`MenuFileManager`** parity.
