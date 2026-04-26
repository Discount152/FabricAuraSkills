# Sprint 98 — Manual: template-backed `sources` (dynamic grid, sort, paging)

| Field | Value |
|--------|--------|
| **Sprint** | **98** — **`sources`** template path only (first dynamic GraniteLib menu) |
| **JAR** | **`FabricAuraSkills-2.3.126.jar`** (+ **`granitelib-fabric`** per **`SPRINT_93`**) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Purpose

**Sprint 96–97** proved template-first **`stat_info`** with strict built-in fallback and parity tightening. **Sprint 98** is the first **dynamic** stress test: **source rows** fill a **YAML-defined 28-slot grid**, while **sort**, **paging**, and **back** reuse the same behavior as **`FabricSourcesMenuGui`**.

## Template path (classpath)

Constant: **`FabricSourcesTemplateSupport.TEMPLATE_RESOURCE_PATH`** →

`assets/auraskills/granite/templates/sources.yml`

Bundled under **`fabric/src/main/resources/`** (same pattern as **`stat_info.yml`**).

## Placeholders supported

Local expander only (**`FabricSourcesPlaceholders`** — not a global registry):

| Token | Meaning |
|--------|---------|
| `{skill}` | Skill id simple name |
| `{skill_display}` | Localized skill display name |
| `{source_name}` | Source display name |
| `{source_xp}` | Formatted XP; if the source has a **unit** name, appended as ` / {unit}` after the number |
| `{source_type}` | Source type id string |
| `{sort}` | Current sort label (same string as built-in title uses for sort) |
| `{page}` | 1-based page index |
| `{total_pages}` | 1-based total pages |

Any remaining `{...}` after expansion aborts the template path → **built-in menu**.

## Dynamic row behavior

- Template item: YAML key **`source`** **or** any item with **`action: source_row`**.
- **`slots:`** on that item define the **source grid**; must be **exactly 28** slots in the **same order** as **`FabricSourcesMenuGui.builtinSourceGridSlots()`** (built-in 7×4).
- For each **XP source** on the current page: material/name/lore from the template row; placeholders expanded per source; **no** click action on rows (same as built-in).
- **`custom_model_data`** on template items is applied like **`stat_info`** (**`FabricStatInfoItemComponents`**).

## Sort / paging behavior

Unchanged semantics vs built-in:

- Page size = **28** (template slot count must match).
- **Sort** button cycles with **`FabricSourceSortType#nextCyclic()`** (resets to page **0** when sort changes, as before).
- **Previous / next** only place stacks when **multi-page** and the target page is valid; clicks **no-op** when the arrow is absent (same as built-in).
- **Back** returns to **level progression** with the same **`returnToSkillsListPage`** as built-in.

## Fallback behavior

Template path **aborts** (warn log, **`false`** from **`FabricSourcesTemplateSupport.tryOpen`**) and **`FabricSourcesMenuGui`** opens the **existing built-in** implementation when any of the following hold:

- Template **missing** or **YAML invalid** / validation errors
- **`id`** not **`sources`**, **`rows`** not **6**
- No **source** row definition, wrong slot count, or slot order ≠ built-in grid
- Missing **back / previous / next / sort** items or wrong **action** strings
- **Unresolved `{...}`** in title, fill name, or any item name/lore
- **Unknown** `material` id (strict registry check)
- **Runtime** exception while building or opening

**Chat fallback:** **`FabricSourcesOpenSupport`** is unchanged — in-game **try GUI**; on throw → **`menu_fallback`** + chat listing; console/RCON → chat only.

## Files changed (implementation)

| Area | Files |
|------|--------|
| Template | **`fabric/src/main/resources/assets/auraskills/granite/templates/sources.yml`** |
| Menu | **`FabricSourcesMenuGui.java`** (template-first + **`builtinSourceGridSlots()`**), **`FabricSourcesTemplateSupport.java`**, **`FabricSourcesPlaceholders.java`** |
| Tests | **`FabricSourcesBundledTemplateParseTest.java`**, **`FabricSourcesPlaceholdersTest.java`** |

## Tests run

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist

On a **Java** client, with a skill that has sources:

1. `/sources <skill>`
2. `/sources <skill> ascending` (and other sorts: `descending`, `alphabetical`, `reverse_alphabetical`)
3. `/openmenu sources <skill>` (and with sort arg if supported)

**Expected:**

- Menu opens; title readable; **source rows** in the templated grid; names/lore readable.
- No raw **`auraskills.granite.*`** keys in items; no raw **`{source_*}`** / **`{skill}`** placeholders in visible text.
- **Sort** cycles; **previous/next** when multiple pages; **back** returns to level progression.
- Server log: no errors.
- **Negative test (local):** break YAML or use unknown material → **built-in** menu still opens.

**Console / RCON:**

- `sources <skill>` → paged **chat** listing; readable; no raw keys.

## Known limitations (keep documented)

- **No** global placeholder registry; **no** disk override / hot reload.
- **`action`** values are **markers + fixed wiring** only — not a full action engine.
- **Not** full **Slate / `MenuFileManager`** parity.
- **Page size** is fixed to **28** matching built-in; template must match that grid.
- Plain JUnit does **not** bootstrap Minecraft registries — **item** resolution is covered in-game; tests cover **YAML**, **slots**, **placeholders**, **nav** shape.

## Bedrock / Geyser

**Deferred** — do **not** claim validation here.

## Operator validation

**Pending** (fill when run): pass/fail, client version, notes.
