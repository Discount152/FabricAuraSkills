# Sprint 97 — Manual: template-backed `stat_info` parity (trait lore + custom model data)

| Field | Value |
|--------|--------|
| **Sprint** | **97** — narrow **`stat_info`** template path only |
| **JAR** | **`FabricAuraSkills-2.3.125.jar`** (+ **`granitelib-fabric-2.3.125.jar`**) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Purpose

**Sprint 96** proved template-first **`stat_info`** with **strict fallback**. **Sprint 97** closes obvious gaps vs the **built-in** menu **before** stressing dynamic menus like **`sources`**.

## What Sprint 96 already proved

- Bundled YAML at **`assets/auraskills/granite/templates/stat_info.yml`**
- **`FabricStatInfoTemplateSupport`** + placeholders **`{stat}`, `{stat_display}`, `{stat_description}`, `{stat_value}`**
- Strict **unresolved `{...}`** → fallback
- Strict **unknown item id** → fallback
- **Back** wired
- Built-in **`FabricStatInfoMenuGui`** unchanged as fallback

## Trait / stat lore approach (Option 2)

**Generated append lore** (no global `{trait_lore}` placeholder):

1. Static YAML **`lore:`** lines are rendered first (placeholders expanded).
2. For the **stat panel** item only — YAML key **`stat`** **or** **`action: stat_panel`** — the template path appends the same **trait lines** as the built-in menu: **`FabricStatsMenuGui.addTraitLoreIfPresent`** for each **`stat.getTraits()`**, stopping at **16** total lore lines (same cap as built-in).
3. If trait generation **throws**, the **entire template path aborts** → **built-in** menu (warn log).

Bundled template adds a static **“Related traits:”** line so the panel reads naturally before appended trait rows.

## `custom_model_data` behavior

- Parsed on **`GraniteTemplateItem`** (Sprint 95 schema) and applied in Sprint 97 via **`FabricStatInfoItemComponents.applyCustomModelData`**.
- Uses **`CustomModelDataComponent(List.of(), List.of(), List.of(), List.of(int))`** — integer is stored in the component’s **color** list index **0**, consistent with **`FabricLootItemMatcher`** / resource-pack **CMD** usage on **1.21.11**.
- **No silent skip:** if the field is absent, nothing is set; if present, the component is applied to that template item’s stack.

**Automated test note:** plain **`fabric:test`** JUnit does **not** bootstrap Minecraft registries, so **no** unit test instantiates **`ItemStack`** for CMD verification — use **in-game** or dev-client check (below).

## Template path (unchanged contract)

Same as Sprint 96: try template after **`stat_info`** enabled + slice enabled; any failure → **`openBuiltIn`**.

## Fallback (unchanged + trait)

Fallback still applies for: missing resource, YAML errors, wrong **id/rows**, no back, unresolved placeholders, unknown items, **runtime** errors — **including trait lore generation failures**.

## Files changed (Sprint 97)

| File | Change |
|------|--------|
| `FabricStatInfoTemplateSupport.java` | Trait append; **`FabricStatInfoItemComponents`** for CMD |
| `FabricStatInfoItemComponents.java` | **New** — CMD application |
| `stat_info.yml` | **“Related traits:”** lore line + header comment |
| Tests | **`FabricStatInfoTemplateSupportStatPanelTest`**, **`FabricStatInfoYamlCustomModelDataParseTest`**, bundled parse assertion |

## Tests run

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

## Java validation checklist (vanilla client)

1. **`/openmenu stat_info <stat>`** for enabled stats (e.g. **strength**, **health**, **wisdom** — use your **`stats.yml`** IDs).
2. Confirm **template path**: title + stat book lore show **description**, **value**, **“Related traits:”**, then **trait** lines (when traits exist for that stat).
3. **No** raw **`auraskills.granite.*`** from template literals; **no** raw **`{stat_*}`** tokens.
4. **Back** → **Stats** with page preserved.
5. **Optional:** add **`custom_model_data:`** with a numeric value to a **local dev** copy of the template (do **not** commit if experimental), re-run — resource pack should pick up CMD on the book; on failure, use **built-in** fallback test from Sprint 96 manual.
6. **Negative:** corrupt YAML locally → confirm **built-in** menu → revert.

## Known limitations

- Not **full Slate** / **MenuFileManager** / global placeholders.
- **`action: stat_panel`** still does not run a separate action handler (only marks the item for **trait append** + matches Sprint 96 naming).
- **No** disk override / hot reload.
- **Trait lore** uses the **same** helper as built-in; modifier grids / rich **Bukkit** `stat_info.yml` remain **out of scope**.

## Bedrock / Geyser

**Unchanged:** not validated this sprint.

## Recommended Sprint 98

**`sources`** template prototype (sort + paging), or further **`stat_info`** polish (e.g. optional **`{trait_lore}`** placeholder later if needed).
