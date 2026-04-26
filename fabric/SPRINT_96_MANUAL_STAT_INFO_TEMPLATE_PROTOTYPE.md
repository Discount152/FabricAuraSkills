# Sprint 96 — Manual: `stat_info` GraniteLib template prototype + built-in fallback

| Field | Value |
|--------|--------|
| **Sprint** | **96** — first **template-backed** menu (bounded prototype) |
| **JAR** | **`FabricAuraSkills-2.3.124.jar`** (+ separate **`granitelib-fabric-2.3.124.jar`** per two-JAR policy) |
| **Bedrock / Geyser** | **Deferred / not tested** |

## Purpose

Prove **Path A**: Granite Skills can open **`stat_info`** from a **bundled GraniteLib v0 YAML** template, using **`GraniteMenuTemplateYamlLoader`** + **`GraniteContainerMenus`**, while **preserving** the historical **`FabricStatInfoMenuGui` built-in** path whenever anything fails.

## Template path (bundled)

```text
assets/auraskills/granite/templates/stat_info.yml
```

Classpath load via **`FabricStatInfoTemplateSupport.TEMPLATE_RESOURCE_PATH`**. **No** user disk override or hot reload in Sprint 96.

## YAML schema

Sprint **95** v0 schema: **`id`**, **`title`**, **`rows`**, optional **`fill`**, **`items`** with **`material`**, **`slot` / `slots`**, optional **`name`**, **`lore`**, **`action`**, etc.

**Sprint 96 constraints:**

- **`id`** must be **`stat_info`** (case-insensitive).
- **`rows`** must be **`6`** (matches **6×9** handler).

## Placeholders (minimal, local)

| Token | Source |
|--------|--------|
| `{stat}` | `stat.getId().getSimpleName()` |
| `{stat_display}` | `MessageProvider#getStatDisplayName(stat, locale, true)` |
| `{stat_description}` | stat description, newlines flattened to spaces |
| `{stat_value}` | `FabricStatsMenuGui.formatStatLevel(...)` |

Expansion is **`FabricStatInfoPlaceholders`** only — **not** a global Slate-style engine. If any `{...}` remains after expansion, the **template path aborts** and the **built-in** menu opens.

## Rendering path

1. **`FabricStatInfoMenuGui.open`** — existing gates (**stat in context**, **`menus.stat_info.enabled`**, slice not disabled).
2. **`FabricStatInfoTemplateSupport.tryOpen`** — load YAML → validate → build **`Text.literal`** title/items → fill grid → place template items → **`GraniteContainerMenus.openClickable9x6`** with **Back** → **Stats** (same **`listPage`** as built-in).
3. On **`false`** or exception → **`openBuiltIn`** (original Sprint 33–38 behavior: **`FabricGraniteText`**, trait lore, config-driven slots).

## Fallback triggers

Template path **does not** run (built-in used) when **any** of:

- Resource **missing** from classpath
- YAML **parse** / **validation** error
- Wrong **`id`** or **`rows`**
- No **back** control (**item key `back`** or **`action: back`**)
- **Unresolved `{...}`** after placeholder pass
- **Unknown** or **invalid** `minecraft:` item id (strict resolution — **no silent substitute** in template path)
- **Runtime** exception during open/populate

All cases **log** a **warning**; **no** dedicated-server crash; player should still get **Stats** or **`stat_info`** via built-in.

## Files touched (implementation)

| File | Role |
|------|------|
| `fabric/.../assets/auraskills/granite/templates/stat_info.yml` | Bundled template |
| `FabricStatInfoPlaceholders.java` | Token expansion + unresolved detection |
| `FabricStatInfoTemplateSupport.java` | Load, validate, render, Back wiring |
| `FabricStatInfoMenuGui.java` | Try template → **fallback** built-in |
| `fabric/.../FabricStatInfoPlaceholdersTest.java` | Unit tests (placeholders) |
| `fabric/.../FabricStatInfoBundledTemplateParseTest.java` | Bundled YAML parses |

## Tests / build

```text
.\gradlew -PfabricOnly=true :granitelib-fabric:test :fabric:test :fabric:build
```

**Operator:** not run in-repo; use checklist below.

## Java validation checklist (vanilla client)

On a **Java** dedicated server with **`FabricAuraSkills-2.3.124+`** and **GraniteLib** installed (**two-JAR** canonical):

1. Ensure **`menus.stat_info.enabled`** is **true** (default in normal configs).
2. Run **`/openmenu stat_info <statId>`** for enabled stats, e.g.:
   - **`/openmenu stat_info strength`**
   - **`/openmenu stat_info health`**
   - **`/openmenu stat_info wisdom`**  
   (Use IDs from your **`stats.yml`** / registry — examples vary by pack.)
3. **Expect (template path):**
   - Menu **opens** (6×9).
   - **Title** readable (e.g. “Stat Info: …”) — **`Text.literal`**, no raw **`auraskills.granite.*`** from template text.
   - **Stat** book (~slot **22**) shows **display name** + **description** + **value** lines; no raw **`{stat_*}`** tokens.
   - **Back** (~slot **45**) returns to **Stats** with **list page** preserved.
4. **Negative test (local only, do not commit):** temporarily replace bundled YAML with invalid content in a **dev** tree → confirm **built-in** menu still opens and **warn** appears in logs → **revert** before commit.

## Known limitations

- **Trait lore** lines from the built-in menu are **not** duplicated on the template path (prototype focuses on YAML + placeholders).
- **`custom_model_data`** in v0 YAML is **ignored** on Fabric stacks until a follow-up wires **`DataComponentTypes.CUSTOM_MODEL_DATA`** safely.
- **No** hot reload, **no** disk override, **no** full action DSL — **`action: stat_panel`** is **ignored**; only **Back** is wired.
- **Slate / `MenuFileManager` parity** remains **open**.

## Bedrock / Geyser

**Unchanged:** **not** validated this sprint.

## Recommended Sprint 97

- Extend template path to **`sources`** **or** re-add **trait** lore to **`stat_info`** template path for closer built-in parity; optionally wire **`custom_model_data`**.
